package co.infoclinic.term.common.loader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.zip.*;

/**
 * SNOMED CT RF2 Delta ZIP → PostgreSQL 증분 적재기
 *
 * 적재 순서:
 *   1. concept, description, description_tp  → INSERT (이력 테이블)
 *   2. inferred_relationship, stated_relationship → INSERT (이력 테이블)
 *   3. inferred_relationship_snap            → DELETE old + INSERT new (스냅샷 갱신)
 *   4. referenceset                          → INSERT ON CONFLICT (referenceset_id) DO UPDATE
 *   5. scheme                                → INSERT v{date}
 *   6. tc                                    → TransitiveClosureLoader.load()
 *   7. referenceset_active                   → ReferenceSetActiveLoader.load(conn, versionDate)
 *   8. mrcm_constraints                      → referenceset에서 MRCM 파일 추출 후 MrcmAttributeRangeLoader 호출
 *
 * 사용법:
 *   java -cp ...:postgresql-*.jar co.infoclinic.term.common.loader.SnomedDeltaLoader \
 *        /path/to/SnomedCT_InternationalRF2_PRODUCTION_20260701T120000Z_1.zip
 */
public class SnomedDeltaLoader {

    private static final String JDBC_URL      = "jdbc:postgresql://localhost:5432/term";
    private static final String JDBC_USER     = "postgres";
    private static final String JDBC_PASSWORD = "julab123!";

    private static final int BATCH_SIZE = 2000;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: SnomedDeltaLoader <path-to-delta-zip>");
            System.exit(1);
        }
        Path zipPath = Paths.get(args[0]).toAbsolutePath().normalize();
        if (!zipPath.toFile().exists()) {
            throw new IllegalArgumentException("Delta ZIP not found: " + zipPath);
        }

        String releaseDate = extractReleaseDate(zipPath.getFileName().toString());
        System.out.println("=== SNOMED CT Delta 적재 시작 (releaseDate=" + releaseDate + ") ===");

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);
            try (Statement s = conn.createStatement()) {
                s.execute("SET search_path TO term, public");
            }

            // 1~4: 원시 테이블
            loadCoreAndRefset(conn, zipPath);
            conn.commit();
            System.out.println("[OK] 원시 테이블 적재 완료.");

            // 5: scheme
            insertScheme(conn, releaseDate);
            conn.commit();
            System.out.println("[OK] SCHEME 갱신 완료.");

            // 6: tc (IS-A diff)
            System.out.println("[INFO] TC 증분 적재 시작...");
            TransitiveClosureLoader.load(conn, releaseDate);
            conn.commit();
            System.out.println("[OK] TC 완료.");

            // 7: referenceset_active (TRUNCATE+재적재, VERSION_DATE=releaseDate)
            System.out.println("[INFO] REFERENCESET_ACTIVE 재적재 시작...");
            ReferenceSetActiveLoader.load(conn, releaseDate);
            conn.commit();
            System.out.println("[OK] REFERENCESET_ACTIVE 완료.");

            // 8: mrcm_constraints
            System.out.println("[INFO] MRCM_CONSTRAINTS 재생성 시작...");
            rebuildMrcmConstraints(conn, releaseDate);
            conn.commit();
            System.out.println("[OK] MRCM_CONSTRAINTS 완료.");
        }

        System.out.println("=== Delta 적재 완료 ===");
        System.out.println("다음 단계: SearchIndexLoader 를 수동으로 실행하세요.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1~4: concept, description, relationship, referenceset
    // ─────────────────────────────────────────────────────────────────────────

    private static void loadCoreAndRefset(Connection conn, Path zipPath) throws Exception {
        // relationship_id 수집 (inferred_relationship_snap 갱신용)
        Set<String> deltaRelIds = new LinkedHashSet<>();

        // 1차 패스: concept, description, relationship
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(
                new FileInputStream(zipPath.toFile())), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName().toLowerCase();
                if (!name.endsWith(".txt") || !name.contains("delta")) continue;

                byte[] bytes = readEntry(zis);
                String base = Paths.get(name).getFileName().toString();

                if (base.contains("sct2_concept_")) {
                    int n = loadConcepts(conn, bytes);
                    System.out.println("  concept: " + n);
                } else if (base.contains("sct2_description_")) {
                    int n = loadDescriptions(conn, bytes);
                    System.out.println("  description+description_tp: " + n);
                } else if (base.contains("sct2_relationship_") && !base.contains("stated") && !base.contains("concretevalues")) {
                    int n = loadRelationships(conn, bytes, false, deltaRelIds);
                    System.out.println("  inferred_relationship: " + n);
                } else if (base.contains("sct2_statedrelationship_")) {
                    int n = loadRelationships(conn, bytes, true, null);
                    System.out.println("  stated_relationship: " + n);
                }
            }
        }
        conn.commit();

        // inferred_relationship_snap 갱신 (스냅샷 최신화)
        if (!deltaRelIds.isEmpty()) {
            updateInferredSnap(conn, zipPath, deltaRelIds);
        }
        conn.commit();

        // 2차 패스: referenceset
        int refsetTotal = 0;
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(
                new FileInputStream(zipPath.toFile())), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName().toLowerCase();
                if (!name.endsWith(".txt") || !name.contains("delta")) continue;
                String base = Paths.get(name).getFileName().toString().toLowerCase();
                if (!base.startsWith("der2_")) continue;

                byte[] bytes = readEntry(zis);
                int n = loadRefset(conn, bytes);
                refsetTotal += n;
            }
        }
        conn.commit();
        System.out.println("  referenceset: " + refsetTotal);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // concept
    // ─────────────────────────────────────────────────────────────────────────

    private static int loadConcepts(Connection conn, byte[] bytes) throws Exception {
        String sql = "INSERT INTO term.concept " +
                     "(concept_id, effective_time, active, module_id, definition_status_id) " +
                     "VALUES (?,?,?,?,?)";
        return batchInsert(conn, sql, bytes, 5, (ps, cols) -> {
            ps.setString(1, cols[0]);
            ps.setString(2, cols[1]);
            ps.setInt   (3, Integer.parseInt(cols[2]));
            ps.setString(4, cols[3]);
            ps.setString(5, cols[4]);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // description + description_tp
    // ─────────────────────────────────────────────────────────────────────────

    private static int loadDescriptions(Connection conn, byte[] bytes) throws Exception {
        String sqlD  = "INSERT INTO term.description " +
                       "(description_id, effective_time, active, module_id, concept_id, " +
                       " language_code, type_id, term, case_significance_id) " +
                       "VALUES (?,?,?,?,?,?,?,?,?)";
        String sqlTP = "INSERT INTO term.description_tp " +
                       "(description_id, effective_time, active, module_id, concept_id, " +
                       " language_code, language_culture, type_id, term, case_significance_id) " +
                       "VALUES (?,?,?,?,?,?,NULL,?,?,?)";
        int count = 0;
        try (PreparedStatement psD  = conn.prepareStatement(sqlD);
             PreparedStatement psTP = conn.prepareStatement(sqlTP);
             BufferedReader r = reader(bytes)) {
            String line;
            while ((line = r.readLine()) != null) {
                if (isHeader(line) || line.trim().isEmpty()) continue;
                String[] c = line.split("\t", -1);
                if (c.length < 9) continue;
                psD.setString(1, c[0]); psD.setString(2, c[1]); psD.setInt(3, parseInt(c[2]));
                psD.setString(4, c[3]); psD.setString(5, c[4]); psD.setString(6, c[5]);
                psD.setString(7, c[6]); psD.setString(8, c[7]); psD.setString(9, c[8]);
                psD.addBatch();
                psTP.setString(1, c[0]); psTP.setString(2, c[1]); psTP.setInt(3, parseInt(c[2]));
                psTP.setString(4, c[3]); psTP.setString(5, c[4]); psTP.setString(6, c[5]);
                psTP.setString(7, c[6]); psTP.setString(8, c[7]); psTP.setString(9, c[8]);
                psTP.addBatch();
                if (++count % BATCH_SIZE == 0) { psD.executeBatch(); psTP.executeBatch(); }
            }
            psD.executeBatch(); psTP.executeBatch();
        }
        return count;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // relationship (inferred / stated)
    // ─────────────────────────────────────────────────────────────────────────

    private static int loadRelationships(Connection conn, byte[] bytes,
                                         boolean stated, Set<String> relIdCollector) throws Exception {
        String table = stated ? "stated_relationship" : "inferred_relationship";
        String sql = "INSERT INTO term." + table +
                     " (relationship_id, effective_time, active, module_id, source_id, destination_id," +
                     " relationship_group, type_id, characteristic_type_id, modifier_id) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?)";
        return batchInsert(conn, sql, bytes, 10, (ps, c) -> {
            ps.setString(1, c[0]); ps.setString(2, c[1]); ps.setInt(3, parseInt(c[2]));
            ps.setString(4, c[3]); ps.setString(5, c[4]); ps.setString(6, c[5]);
            ps.setInt   (7, parseInt(c[6]));
            ps.setString(8, c[7]); ps.setString(9, c[8]); ps.setString(10, c[9]);
            if (relIdCollector != null) relIdCollector.add(c[0]);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // inferred_relationship_snap 갱신 (DELETE old rows + INSERT Delta rows)
    // ─────────────────────────────────────────────────────────────────────────

    private static void updateInferredSnap(Connection conn, Path zipPath,
                                           Set<String> deltaRelIds) throws Exception {
        // 기존 스냅샷에서 Delta 포함 relationship_id 삭제
        String delSql = "DELETE FROM term.inferred_relationship_snap WHERE relationship_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(delSql)) {
            for (String rid : deltaRelIds) {
                ps.setString(1, rid);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        conn.commit();

        // Delta relationship 행을 스냅샷에 INSERT
        String sql = "INSERT INTO term.inferred_relationship_snap " +
                     "(relationship_id, effective_time, active, module_id, source_id, destination_id," +
                     " relationship_group, type_id, characteristic_type_id, modifier_id) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(
                new FileInputStream(zipPath.toFile())), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName().toLowerCase();
                if (!name.endsWith(".txt") || !name.contains("delta")) continue;
                String base = Paths.get(name).getFileName().toString().toLowerCase();
                if (!base.contains("sct2_relationship_") || base.contains("stated") || base.contains("concretevalues")) continue;

                byte[] bytes = readEntry(zis);
                int n = batchInsert(conn, sql, bytes, 10, (ps, c) -> {
                    ps.setString(1, c[0]); ps.setString(2, c[1]); ps.setInt(3, parseInt(c[2]));
                    ps.setString(4, c[3]); ps.setString(5, c[4]); ps.setString(6, c[5]);
                    ps.setInt   (7, parseInt(c[6]));
                    ps.setString(8, c[7]); ps.setString(9, c[8]); ps.setString(10, c[9]);
                });
                System.out.println("  inferred_relationship_snap: " + n);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // referenceset
    // ─────────────────────────────────────────────────────────────────────────

    private static int loadRefset(Connection conn, byte[] bytes) throws Exception {
        // 헤더에서 컬럼 수 판별
        String[] headerCols;
        try (BufferedReader r = reader(bytes)) {
            String h = r.readLine();
            if (h == null) return 0;
            headerCols = h.split("\t", -1);
        }
        int extra = headerCols.length - 6; // 6: id effectiveTime active moduleId refsetId referencedComponentId

        String fieldCols = "";
        String fieldPlaces = "";
        for (int i = 1; i <= extra && i <= 7; i++) {
            fieldCols   += ", field" + i;
            fieldPlaces += ", ?";
        }

        String sql = "INSERT INTO term.referenceset " +
                     "(referenceset_id, effective_time, active, module_id, refset_id, referenced_component_id" + fieldCols + ") " +
                     "VALUES (?,?,?,?,?,?" + fieldPlaces + ") " +
                     "ON CONFLICT (referenceset_id) DO UPDATE SET " +
                     "effective_time=EXCLUDED.effective_time, active=EXCLUDED.active, " +
                     "module_id=EXCLUDED.module_id, refset_id=EXCLUDED.refset_id, " +
                     "referenced_component_id=EXCLUDED.referenced_component_id" +
                     buildFieldUpdateClauses(extra);

        final int fieldCount = Math.min(extra, 7);
        return batchInsert(conn, sql, bytes, 6 + fieldCount, (ps, c) -> {
            ps.setString(1, c[0]); ps.setString(2, c[1]); ps.setInt(3, parseInt(c[2]));
            ps.setString(4, c[3]); ps.setString(5, c[4]); ps.setString(6, c[5]);
            for (int i = 0; i < fieldCount && (6 + i) < c.length; i++) {
                ps.setString(7 + i, c[6 + i]);
            }
        });
    }

    private static String buildFieldUpdateClauses(int extra) {
        if (extra <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= extra && i <= 7; i++) {
            sb.append(", field").append(i).append("=EXCLUDED.field").append(i);
        }
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // scheme
    // ─────────────────────────────────────────────────────────────────────────

    private static void insertScheme(Connection conn, String releaseDate) throws Exception {
        String id      = "SNOMEDCT-v" + releaseDate;
        String edition = "SNOMEDCT v" + releaseDate;
        String version = "v" + releaseDate;
        String sql = "INSERT INTO term.scheme (ID, NAME, EDITION, VERSION, AUTHORITY, DATE) " +
                     "VALUES (?,?,?,?,?,?) ON CONFLICT (ID) DO UPDATE SET " +
                     "NAME=EXCLUDED.NAME, EDITION=EXCLUDED.EDITION, VERSION=EXCLUDED.VERSION, " +
                     "AUTHORITY=EXCLUDED.AUTHORITY, DATE=EXCLUDED.DATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, "SNOMEDCT-Int");
            ps.setString(3, edition);
            ps.setString(4, version);
            ps.setString(5, "SNOMED International");
            ps.setString(6, releaseDate);
            ps.executeUpdate();
        }
        System.out.println("  SCHEME: " + id);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // mrcm_constraints 재생성
    // ─────────────────────────────────────────────────────────────────────────

    private static void rebuildMrcmConstraints(Connection conn, String releaseDate) throws Exception {
        // term.referenceset에서 최신 활성 MRCM AttributeRange / Domain 행을 RF2 형식으로 추출
        Path tmpDir = Files.createTempDirectory("snomed_mrcm_");

        Path rangeFile  = exportMrcmRefset(conn, tmpDir, releaseDate,
                "723562003", "der2_ssccRefset_MRCMAttributeRangeSnapshot_INT_" + releaseDate + ".txt",
                8);
        Path domainFile = exportMrcmRefset(conn, tmpDir, releaseDate,
                "723560006", "der2_sssssssRefset_MRCMDomainSnapshot_INT_" + releaseDate + ".txt",
                13);

        System.out.println("  MRCM Range 파일 추출: " + rangeFile);
        System.out.println("  MRCM Domain 파일 추출: " + domainFile);

        // MrcmAttributeRangeLoader 호출 (내부적으로 TRUNCATE 후 재적재)
        MrcmAttributeRangeLoader.main(new String[]{ rangeFile.toString(), domainFile.toString() });

        // 임시 파일 삭제
        Files.deleteIfExists(rangeFile);
        Files.deleteIfExists(domainFile);
        Files.deleteIfExists(tmpDir);
    }

    /**
     * term.referenceset에서 특정 refset_id의 최신 활성 행을 RF2 탭 구분 파일로 추출한다.
     * fieldCount: 6(base) + N개 extra field. MRCM AttributeRange=8(+2fields), Domain=13(+7fields)
     */
    private static Path exportMrcmRefset(Connection conn, Path dir, String releaseDate,
                                          String refsetId, String fileName, int totalCols) throws Exception {
        // DISTINCT ON으로 최신 active=1 행만 추출
        StringBuilder selectFields = new StringBuilder(
            "referenceset_id, effective_time, active, module_id, refset_id, referenced_component_id");
        for (int i = 1; i <= totalCols - 6; i++) selectFields.append(", field").append(i);

        String sql = "SELECT " + selectFields +
                     " FROM (" +
                     "  SELECT DISTINCT ON (referenceset_id) " + selectFields +
                     "  FROM term.referenceset" +
                     "  WHERE refset_id = '" + refsetId + "'" +
                     "  ORDER BY referenceset_id, effective_time DESC" +
                     ") r WHERE active = 1";

        // RF2 헤더 컬럼명 (MRCM 스펙 기준)
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("723562003", "id\teffectiveTime\tactive\tmoduleId\trefsetId\treferencedComponentId\trangeConstraint\tattributeRule\truleStrengthId\tcontentTypeId");
        headers.put("723560006", "id\teffectiveTime\tactive\tmoduleId\trefsetId\treferencedComponentId\tdomainConstraint\tparentDomain\tproximalPrimitiveConstraint\tproximalPrimitiveRefinement\tdomainTemplateForPrecoordination\tdomainTemplateForPostcoordination\tguideURL");

        Path outFile = dir.resolve(fileName);
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(outFile, StandardCharsets.UTF_8));
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            w.println(headers.getOrDefault(refsetId,
                "id\teffectiveTime\tactive\tmoduleId\trefsetId\treferencedComponentId"));
            int colCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                StringBuilder line = new StringBuilder();
                for (int i = 1; i <= colCount; i++) {
                    if (i > 1) line.append('\t');
                    String v = rs.getString(i);
                    line.append(v != null ? v : "");
                }
                w.println(line);
            }
        }
        return outFile;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 유틸리티
    // ─────────────────────────────────────────────────────────────────────────

    @FunctionalInterface
    interface RowSetter {
        void set(PreparedStatement ps, String[] cols) throws SQLException;
    }

    private static int batchInsert(Connection conn, String sql, byte[] bytes,
                                   int minCols, RowSetter setter) throws Exception {
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             BufferedReader r = reader(bytes)) {
            String line;
            while ((line = r.readLine()) != null) {
                if (isHeader(line) || line.trim().isEmpty()) continue;
                String[] c = line.split("\t", -1);
                if (c.length < minCols) continue;
                setter.set(ps, c);
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) ps.executeBatch();
            }
            ps.executeBatch();
        }
        return count;
    }

    private static byte[] readEntry(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int n;
        while ((n = zis.read(chunk)) != -1) buf.write(chunk, 0, n);
        return buf.toByteArray();
    }

    private static BufferedReader reader(byte[] bytes) {
        return new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(bytes), StandardCharsets.UTF_8));
    }

    private static boolean isHeader(String line) {
        return line.startsWith("id\t") || line.startsWith("Id\t");
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return 0; }
    }

    /** ZIP 파일명에서 YYYYMMDD 추출 (e.g. _20260701T) */
    private static String extractReleaseDate(String filename) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("_(\\d{8})T").matcher(filename);
        return m.find() ? m.group(1) : "UNKNOWN";
    }
}
