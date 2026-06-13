package co.infoclinic.term.common.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LOINC PostgreSQL 적재기
 *
 * 입력:
 *   classFilePath  - release_files/loinc_2.82_class.csv (TSV, 항상 필요)
 *   loincBaseDir   - LOINC_2.82/ 폴더 경로 (LoincTable/, AccessoryFiles/ 포함)
 *                    null이면 CLASS 테이블만 적재
 *
 * 적재 순서:
 *   1. CLASS  ← classFilePath (TSV)
 *   2. LOINC, MAP_TO, SOURCE_ORGANIZATION  ← LoincTable/*.csv
 *   3. HIERARCHY_TMP  ← AccessoryFiles/ComponentHierarchyBySystem/
 *   4. LP, LP_LINK, LP_MAP  ← AccessoryFiles/PartFile/
 *   5. LA, LA_LINK  ← AccessoryFiles/AnswerFile/
 *   6. PARENT_LG, PARENT_LG_ATTRIBUTES, LG, LG_ATTRIBUTES, LG_TERMS
 *                   ← AccessoryFiles/GroupFile/
 *   7. LINGUISTIC_VARIANTS, LINGUISTIC_VARIANT  ← AccessoryFiles/LinguisticVariants/
 *   8. PANEL  ← AccessoryFiles/PanelsAndForms/
 *   9. HIERARCHY 빌드 (SQL)
 *  10. HIERARCHY_LG 빌드 (SQL)
 *  11. PANEL PATH/ROOT_CODE 빌드 (SQL)
 *  12. children/descendant count 계산 (SQL)
 */
public class LoincDataLoader {

    private static final Logger log = Logger.getLogger(LoincDataLoader.class.getName());

    private static final String JDBC_URL      = "jdbc:postgresql://localhost:5432/term";
    private static final String JDBC_USER     = "postgres";
    private static final String JDBC_PASSWORD = "julab123!";

    private static final int BATCH_SIZE = 10000;

    // =========================================================================
    // 진입점 (단독 실행)
    // =========================================================================

    public static void main(String[] args) throws Exception {
        String classFile  = args.length > 0 ? args[0] : "release_files/loinc_2.82_class.csv";
        String loincBase  = args.length > 1 ? args[1] : null;

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);
            load(conn, classFile, loincBase);
            conn.commit();
        }
        System.out.println("[INFO] LOINC 적재 완료.");
    }

    // =========================================================================
    // SnomedDataLoader에서 호출
    // =========================================================================

    public static void load(Connection conn, String classFilePath, String loincBaseDir) throws Exception {
        long t0 = System.currentTimeMillis();
        log.info("LOINC 적재 시작");

        // 세션 최적화 (WAL 동기화 완화, 체크포인트 미룸)
        try (Statement st = conn.createStatement()) {
            st.execute("SET synchronous_commit = OFF");
            st.execute("SET work_mem = '256MB'");
            st.execute("SET maintenance_work_mem = '512MB'");
        }

        truncateTables(conn);
        dropIndexes(conn);

        // CLASS 적재 (항상)
        File classFile = new File(classFilePath);
        if (classFile.exists()) {
            loadClass(conn, classFile);
            conn.commit();
            log.info("  CLASS 적재 완료");
        } else {
            log.warning("  CLASS 파일 없음: " + classFile.getAbsolutePath());
        }

        if (loincBaseDir == null || loincBaseDir.isEmpty()) {
            log.info("  loincBaseDir 미지정 → CLASS만 적재");
            createIndexes(conn);
            return;
        }

        File base = new File(loincBaseDir);
        if (!base.exists()) {
            log.warning("  LOINC 기본 디렉토리 없음: " + base.getAbsolutePath() + " → 건너뜀");
            createIndexes(conn);
            return;
        }

        // 핵심 테이블
        loadCsvOptional(conn, new File(base, "LoincTable/Loinc.csv"),            (c, f) -> loadLoinc(c, f));
        loadCsvOptional(conn, new File(base, "LoincTable/MapTo.csv"),             (c, f) -> loadMapTo(c, f));
        loadCsvOptional(conn, new File(base, "LoincTable/SourceOrganization.csv"), (c, f) -> loadSourceOrganization(c, f));

        // HIERARCHY_TMP
        loadCsvOptional(conn, new File(base, "AccessoryFiles/ComponentHierarchyBySystem/ComponentHierarchyBySystem.csv"),
                (c, f) -> loadHierarchyTmp(c, f));

        // LP / LP_LINK / LP_MAP
        loadCsvOptional(conn, new File(base, "AccessoryFiles/PartFile/Part.csv"),                        (c, f) -> loadLP(c, f));
        loadCsvOptional(conn, new File(base, "AccessoryFiles/PartFile/LoincPartLink_Primary.csv"),       (c, f) -> loadLPLink(c, f));
        loadCsvOptional(conn, new File(base, "AccessoryFiles/PartFile/LoincPartLink_Supplementary.csv"), (c, f) -> loadLPLink(c, f));
        loadCsvOptional(conn, new File(base, "AccessoryFiles/PartFile/PartRelatedCodeMapping.csv"),      (c, f) -> loadLPMap(c, f));

        // LA / LA_LINK
        loadCsvOptional(conn, new File(base, "AccessoryFiles/AnswerFile/AnswerList.csv"),         (c, f) -> loadLA(c, f));
        loadCsvOptional(conn, new File(base, "AccessoryFiles/AnswerFile/LoincAnswerListLink.csv"), (c, f) -> loadLALink(c, f));

        // GROUP
        loadCsvOptional(conn, new File(base, "AccessoryFiles/GroupFile/ParentGroup.csv"),           (c, f) -> loadParentLG(c, f));
        loadCsvOptional(conn, new File(base, "AccessoryFiles/GroupFile/ParentGroupAttributes.csv"), (c, f) -> loadParentLGAttr(c, f));
        loadCsvOptional(conn, new File(base, "AccessoryFiles/GroupFile/Group.csv"),                 (c, f) -> loadLG(c, f));
        loadCsvOptional(conn, new File(base, "AccessoryFiles/GroupFile/GroupAttributes.csv"),       (c, f) -> loadLGAttr(c, f));
        loadCsvOptional(conn, new File(base, "AccessoryFiles/GroupFile/GroupLoincTerms.csv"),       (c, f) -> loadLGTerms(c, f));

        // LINGUISTIC_VARIANTS (메타)
        loadCsvOptional(conn, new File(base, "AccessoryFiles/LinguisticVariants/LinguisticVariants.csv"),
                (c, f) -> loadLinguisticVariants(c, f));

        // LINGUISTIC_VARIANT (각 언어별 파일) — 병렬 로딩
        File lvDir = new File(base, "AccessoryFiles/LinguisticVariants");
        if (lvDir.exists()) {
            File[] lvFiles = Arrays.stream(lvDir.listFiles())
                    .filter(f -> f.getName().endsWith("LinguisticVariant.csv"))
                    .toArray(File[]::new);
            loadLinguisticVariantsParallel(lvFiles);
        }

        // PANEL
        loadCsvOptional(conn, new File(base, "AccessoryFiles/PanelsAndForms/PanelsAndForms.csv"), (c, f) -> loadPanel(c, f));

        // 인덱스 재생성 (빌드 SQL 전에 — HIERARCHY 빌드가 인덱스를 사용)
        log.info("  인덱스 재생성 중...");
        createIndexes(conn);
        log.info("  인덱스 재생성 완료");

        // HIERARCHY 빌드
        buildHierarchy(conn);
        conn.commit();
        log.info("  HIERARCHY 빌드 완료");

        // HIERARCHY_LG 빌드
        buildHierarchyLG(conn);
        conn.commit();
        log.info("  HIERARCHY_LG 빌드 완료");

        // PANEL PATH 빌드
        buildPanelPaths(conn);
        conn.commit();
        log.info("  PANEL PATH 빌드 완료");

        // children/descendant count
        buildCounts(conn);
        conn.commit();
        log.info("  count 계산 완료");

        log.info("LOINC 적재 완료: " + (System.currentTimeMillis() - t0) / 1000 + "초");
    }

    // =========================================================================
    // 인덱스 DROP / CREATE (적재 전후)
    // =========================================================================

    private static final String[] INDEX_DROPS = {
        "DROP INDEX IF EXISTS loinc.IDX_LNC_HIER_CODE",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_HIER_PARENT",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_HIER_PATH",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_HIER_CHILDREN",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_HIER_DESCENDANT",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_HTMP_CODE",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_HTMP_PARENT",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_HTMP_PATH",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_HLG_CODE",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_HLG_PARENT",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_HLG_PATH",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_HLG_CHILDREN",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_HLG_DESCENDANT",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_LV_CODE",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_PANEL_CODE",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_PANEL_ID",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_PANEL_PARENT_ID",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_PANEL_PARENT_NAME",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_PANEL_PARENT_CODE",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_PANEL_PATH",
        "DROP INDEX IF EXISTS loinc.IDX_LNC_PANEL_ROOT_CODE"
    };

    private static final String[] INDEX_CREATES = {
        "CREATE INDEX IDX_LNC_HIER_CODE       ON loinc.HIERARCHY (CODE)",
        "CREATE INDEX IDX_LNC_HIER_PARENT     ON loinc.HIERARCHY (PARENT)",
        "CREATE INDEX IDX_LNC_HIER_PATH       ON loinc.HIERARCHY (PATH)",
        "CREATE INDEX IDX_LNC_HIER_CHILDREN   ON loinc.HIERARCHY (CHILDREN_COUNT)",
        "CREATE INDEX IDX_LNC_HIER_DESCENDANT ON loinc.HIERARCHY (DESCENDANT_COUNT)",
        "CREATE INDEX IDX_LNC_HTMP_CODE       ON loinc.HIERARCHY_TMP (CODE)",
        "CREATE INDEX IDX_LNC_HTMP_PARENT     ON loinc.HIERARCHY_TMP (PARENT)",
        "CREATE INDEX IDX_LNC_HTMP_PATH       ON loinc.HIERARCHY_TMP (PATH)",
        "CREATE INDEX IDX_LNC_HLG_CODE        ON loinc.HIERARCHY_LG (CODE)",
        "CREATE INDEX IDX_LNC_HLG_PARENT      ON loinc.HIERARCHY_LG (PARENT)",
        "CREATE INDEX IDX_LNC_HLG_PATH        ON loinc.HIERARCHY_LG (PATH)",
        "CREATE INDEX IDX_LNC_HLG_CHILDREN    ON loinc.HIERARCHY_LG (CHILDREN_COUNT)",
        "CREATE INDEX IDX_LNC_HLG_DESCENDANT  ON loinc.HIERARCHY_LG (DESCENDANT_COUNT)",
        "CREATE INDEX IDX_LNC_LV_CODE         ON loinc.LINGUISTIC_VARIANT (CODE, ISO_LANG, ISO_COUNTRY)",
        "CREATE INDEX IDX_LNC_PANEL_CODE        ON loinc.PANEL (CODE)",
        "CREATE INDEX IDX_LNC_PANEL_ID          ON loinc.PANEL (ID)",
        "CREATE INDEX IDX_LNC_PANEL_PARENT_ID   ON loinc.PANEL (PARENT_ID)",
        "CREATE INDEX IDX_LNC_PANEL_PARENT_NAME ON loinc.PANEL (PARENT_NAME)",
        "CREATE INDEX IDX_LNC_PANEL_PARENT_CODE ON loinc.PANEL (PARENT_CODE)",
        "CREATE INDEX IDX_LNC_PANEL_PATH        ON loinc.PANEL (PATH)",
        "CREATE INDEX IDX_LNC_PANEL_ROOT_CODE   ON loinc.PANEL (ROOT_CODE)"
    };

    private static void dropIndexes(Connection conn) throws Exception {
        try (Statement st = conn.createStatement()) {
            for (String sql : INDEX_DROPS) st.execute(sql);
        }
        conn.commit();
        log.info("  인덱스 DROP 완료");
    }

    private static void createIndexes(Connection conn) throws Exception {
        conn.setAutoCommit(true); // CREATE INDEX CONCURRENTLY 없이도 빠르게 처리
        try (Statement st = conn.createStatement()) {
            for (String sql : INDEX_CREATES) {
                st.execute(sql);
            }
        }
        conn.setAutoCommit(false);
        log.info("  인덱스 CREATE 완료");
    }

    // =========================================================================
    // LINGUISTIC_VARIANT 병렬 적재
    // =========================================================================

    private static void loadLinguisticVariantsParallel(File[] files) throws Exception {
        int threads = Math.min(files.length, Runtime.getRuntime().availableProcessors());
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();
        for (File f : files) {
            final File lvFile = f;
            futures.add(pool.submit(() -> {
                try (Connection c = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
                    c.setAutoCommit(false);
                    try (Statement st = c.createStatement()) {
                        st.execute("SET synchronous_commit = OFF");
                    }
                    loadLinguisticVariant(c, lvFile);
                    c.commit();
                    log.info("  완료(병렬): " + lvFile.getName());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }));
        }
        pool.shutdown();
        for (Future<?> fu : futures) fu.get(); // 예외 전파
    }

    // =========================================================================
    // 테이블 초기화
    // =========================================================================

    private static void truncateTables(Connection conn) throws Exception {
        String[] tables = {
            "loinc.hierarchy_lg", "loinc.hierarchy", "loinc.hierarchy_tmp",
            "loinc.panel", "loinc.la_link", "loinc.la",
            "loinc.lg_terms", "loinc.lg_attributes", "loinc.lg",
            "loinc.parent_lg_attributes", "loinc.parent_lg",
            "loinc.lp_map", "loinc.lp_link", "loinc.lp",
            "loinc.linguistic_variant", "loinc.linguistic_variants",
            "loinc.source_organization", "loinc.map_to",
            "loinc.loinc", "loinc.class"
        };
        try (Statement s = conn.createStatement()) {
            for (String t : tables) {
                s.execute("TRUNCATE TABLE " + t + " RESTART IDENTITY CASCADE");
            }
        }
        conn.commit();
    }

    // =========================================================================
    // 편의 메서드: 파일 존재 시에만 적재
    // =========================================================================

    @FunctionalInterface
    interface CsvLoader { void load(Connection conn, File file) throws Exception; }

    private static void loadCsvOptional(Connection conn, File file, CsvLoader loader) throws Exception {
        if (file.exists()) {
            loader.load(conn, file);
            conn.commit();
            log.info("  적재 완료: " + file.getName());
        } else {
            log.info("  파일 없음 (건너뜀): " + file.getPath());
        }
    }

    // =========================================================================
    // CSV/TSV 파싱 유틸
    // =========================================================================

    /** 단순 CSV 파싱 (큰따옴표 옵션 지원, CRLF) */
    private static String[] parseCsvLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuote && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"'); i++;
                } else {
                    inQuote = !inQuote;
                }
            } else if (c == ',' && !inQuote) {
                fields.add(sb.toString()); sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    private static String col(String[] row, int idx) {
        if (idx >= row.length) return null;
        String v = row[idx].trim();
        return v.isEmpty() ? null : v;
    }

    private static Integer colInt(String[] row, int idx) {
        String v = col(row, idx);
        if (v == null) return null;
        try { return Integer.parseInt(v); } catch (NumberFormatException e) { return null; }
    }

    private static Long colLong(String[] row, int idx) {
        String v = col(row, idx);
        if (v == null) return null;
        try { return Long.parseLong(v); } catch (NumberFormatException e) { return null; }
    }

    // =========================================================================
    // CLASS 적재 (TSV)
    // =========================================================================

    private static void loadClass(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO loinc.class (type, name, abbreviation, term) VALUES (?, ?, ?, ?)";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "").trim();
                if (line.isEmpty()) continue;
                String[] f = line.split("\t", -1);
                if (f.length < 4) continue;
                Integer type = colInt(f, 0);
                String name = col(f, 1);
                String abbr = col(f, 2);
                String term = col(f, 3);
                if (name != null) name = name.replace(' ', '_');
                ps.setObject(1, type);
                ps.setString(2, name);
                ps.setString(3, abbr);
                ps.setString(4, term);
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  CLASS: " + count + "건");
    }

    // =========================================================================
    // LOINC 메인 테이블 적재
    // =========================================================================

    // LOINC 2.82 CSV 실제 컬럼 (40개):
    // 0:LOINC_NUM 1:COMPONENT 2:PROPERTY 3:TIME_ASPCT 4:SYSTEM 5:SCALE_TYP
    // 6:METHOD_TYP 7:CLASS 8:VersionLastChanged 9:CHNG_TYPE 10:DefinitionDescription
    // 11:STATUS 12:CONSUMER_NAME 13:CLASSTYPE(int) 14:FORMULA 15:EXMPL_ANSWERS
    // 16:SURVEY_QUEST_TEXT 17:SURVEY_QUEST_SRC 18:UNITSREQUIRED 19:RELATEDNAMES2
    // 20:SHORTNAME 21:ORDER_OBS 22:HL7_FIELD_SUBFIELD_ID 23:EXTERNAL_COPYRIGHT_NOTICE
    // 24:EXAMPLE_UNITS 25:LONG_COMMON_NAME 26:EXAMPLE_UCUM_UNITS
    // 27:STATUS_REASON 28:STATUS_TEXT 29:CHANGE_REASON_PUBLIC
    // 30:COMMON_TEST_RANK(int) 31:COMMON_ORDER_RANK(int) 32:HL7_ATTACHMENT_STRUCTURE
    // 33:EXTERNAL_COPYRIGHT_LINK 34:PanelType 35:AskAtOrderEntry
    // 36:AssociatedObservations 37:VersionFirstReleased 38:ValidHL7AttachmentRequest
    // 39:DisplayName
    // (없는 컬럼: SUBMITTED_UNITS, CDISC_COMMON_TESTS, UNITS_AND_RANGE,
    //             EXAMPLE_SI_UCUM_UNITS, COMMON_SI_TEST_RANK → NULL로 적재)
    private static void loadLoinc(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO loinc.loinc (" +
            "code,component,property,time_aspect,system,scale_type,method_type," +
            "class_name,last_changed_version,change_type,definition_description,status," +
            "consumer_name,class_type,formula,example_answers,survey_quest_text,survey_quest_src," +
            "units_required,related_names2,short_name,order_obs," +
            "hl7_field_subfield_id,external_copyright_notice,example_units,long_common_name," +
            "example_ucum_units,status_reason,status_text," +
            "change_reason_public,common_test_rank,common_order_rank," +
            "hl7_attachment_structure,external_copyright_link,panel_type,ask_at_order_entry," +
            "associated_observations,first_released_version,valid_hl7_attachment_request,display_name" +
            ") VALUES (" + repeat("?", 40) + ")";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "");
                if (line.isEmpty()) continue;
                String[] f = parseCsvLine(line);
                // f[0]-f[12]: code~consumer_name (strings)
                ps.setString(1, col(f, 0));
                for (int i = 1; i <= 12; i++) ps.setString(i + 1, col(f, i));
                // f[13]: class_type (int)
                ps.setObject(14, colInt(f, 13));
                // f[14]-f[18]: formula~units_required (strings)
                for (int i = 14; i <= 18; i++) ps.setString(i + 1, col(f, i));
                // f[19]-f[25]: related_names2~long_common_name (strings) → ps[20]-ps[26]
                for (int i = 19; i <= 25; i++) ps.setString(i + 1, col(f, i));
                // f[26]-f[29]: example_ucum_units~change_reason_public (strings) → ps[27]-ps[30]
                for (int i = 26; i <= 29; i++) ps.setString(i + 1, col(f, i));
                // f[30]: common_test_rank (int) → ps[31]
                ps.setObject(31, colInt(f, 30));
                // f[31]: common_order_rank (int) → ps[32]
                ps.setObject(32, colInt(f, 31));
                // f[32]-f[39]: hl7_attachment~display_name (strings) → ps[33]-ps[40]
                for (int i = 32; i <= 39; i++) ps.setString(i + 1, col(f, i));
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  LOINC: " + count + "건");
    }

    // =========================================================================
    // MAP_TO 적재
    // =========================================================================

    private static void loadMapTo(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO loinc.map_to (code, map_to, comment) VALUES (?, ?, ?)";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "");
                if (line.isEmpty()) continue;
                String[] f = parseCsvLine(line);
                ps.setString(1, col(f, 0));
                ps.setString(2, col(f, 1));
                ps.setString(3, col(f, 2));
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  MAP_TO: " + count + "건");
    }

    // =========================================================================
    // SOURCE_ORGANIZATION 적재
    // =========================================================================

    private static void loadSourceOrganization(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO loinc.source_organization (id, copyright_id, name, copyright, terms_of_use, url)" +
                     " VALUES (?, ?, ?, ?, ?, ?)";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "");
                if (line.isEmpty()) continue;
                String[] f = parseCsvLine(line);
                ps.setObject(1, colLong(f, 0));
                for (int i = 1; i <= 5; i++) ps.setString(i + 1, col(f, i));
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  SOURCE_ORGANIZATION: " + count + "건");
    }

    // =========================================================================
    // HIERARCHY_TMP 적재 (ComponentHierarchyBySystem.csv)
    // =========================================================================

    private static void loadHierarchyTmp(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO loinc.hierarchy_tmp (path, sequence, parent, code, name) VALUES (?, ?, ?, ?, ?)";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "");
                if (line.isEmpty()) continue;
                String[] f = parseCsvLine(line);
                ps.setString(1, col(f, 0));
                ps.setObject(2, colInt(f, 1));
                ps.setString(3, col(f, 2));
                ps.setString(4, col(f, 3));
                ps.setString(5, col(f, 4));
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  HIERARCHY_TMP: " + count + "건");
    }

    // =========================================================================
    // LP 적재 (Part.csv)
    // =========================================================================

    private static void loadLP(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO loinc.lp (part_number, part_type_name, part_name, part_display_name, status)" +
                     " VALUES (?, ?, ?, ?, ?)";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "");
                if (line.isEmpty()) continue;
                String[] f = parseCsvLine(line);
                for (int i = 0; i < 5; i++) ps.setString(i + 1, col(f, i));
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  LP: " + count + "건");
    }

    // =========================================================================
    // LP_LINK 적재 (LoincPartLink_Primary/Supplementary.csv)
    // =========================================================================

    private static void loadLPLink(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO loinc.lp_link (loinc_number, long_common_name, part_number, part_name," +
                     " part_code_system, part_type_name, link_type_name, property) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "");
                if (line.isEmpty()) continue;
                String[] f = parseCsvLine(line);
                for (int i = 0; i < 8; i++) ps.setString(i + 1, col(f, i));
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  LP_LINK (" + file.getName() + "): " + count + "건");
    }

    // =========================================================================
    // LP_MAP 적재 (PartRelatedCodeMapping.csv)
    // =========================================================================

    private static void loadLPMap(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO loinc.lp_map (part_number, part_name, part_type_name, ext_code_id," +
                     " ext_code_display_name, ext_code_system, equivalence, content_origin," +
                     " ext_code_system_version, ext_code_system_copyright_notice) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "");
                if (line.isEmpty()) continue;
                String[] f = parseCsvLine(line);
                for (int i = 0; i < 10; i++) ps.setString(i + 1, col(f, i));
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  LP_MAP: " + count + "건");
    }

    // =========================================================================
    // LA 적재 (AnswerList.csv)
    // =========================================================================

    private static void loadLA(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO loinc.la (answer_list_id, answer_list_name, answer_list_oid," +
                     " ext_defined_yn, ext_defined_answer_list_code_system, ext_defined_answer_list_link," +
                     " answer_string_id, local_answer_code, local_answer_code_system, sequence_number," +
                     " display_text, ext_code_id, ext_code_display_name, ext_code_system," +
                     " ext_code_system_version, ext_code_system_copyright_notice, subsequence_text_prompt," +
                     " description, score) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "");
                if (line.isEmpty()) continue;
                String[] f = parseCsvLine(line);
                for (int i = 0; i < 9; i++) ps.setString(i + 1, col(f, i));
                ps.setObject(10, colInt(f, 9)); // sequence_number
                for (int i = 10; i < 19; i++) ps.setString(i + 1, col(f, i));
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  LA: " + count + "건");
    }

    // =========================================================================
    // LA_LINK 적재 (LoincAnswerListLink.csv)
    // =========================================================================

    private static void loadLALink(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO loinc.la_link (loinc_number, long_common_name, answer_list_id," +
                     " answer_list_name, answer_list_link_type, applicable_context) VALUES (?, ?, ?, ?, ?, ?)";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "");
                if (line.isEmpty()) continue;
                String[] f = parseCsvLine(line);
                for (int i = 0; i < 6; i++) ps.setString(i + 1, col(f, i));
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  LA_LINK: " + count + "건");
    }

    // =========================================================================
    // PARENT_LG 적재 (ParentGroup.csv)
    // =========================================================================

    private static void loadParentLG(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO loinc.parent_lg (parent_lg_id, parent_lg, status) VALUES (?, ?, ?)";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "");
                if (line.isEmpty()) continue;
                String[] f = parseCsvLine(line);
                for (int i = 0; i < 3; i++) ps.setString(i + 1, col(f, i));
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  PARENT_LG: " + count + "건");
    }

    // =========================================================================
    // PARENT_LG_ATTRIBUTES 적재 (ParentGroupAttributes.csv)
    // =========================================================================

    private static void loadParentLGAttr(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO loinc.parent_lg_attributes (parent_lg_id, type, value) VALUES (?, ?, ?)";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "");
                if (line.isEmpty()) continue;
                String[] f = parseCsvLine(line);
                for (int i = 0; i < 3; i++) ps.setString(i + 1, col(f, i));
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  PARENT_LG_ATTRIBUTES: " + count + "건");
    }

    // =========================================================================
    // LG 적재 (Group.csv)
    // =========================================================================

    private static void loadLG(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO loinc.lg (parent_lg_id, lg_id, lg, archetype, status, version_first_released)" +
                     " VALUES (?, ?, ?, ?, ?, ?)";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "");
                if (line.isEmpty()) continue;
                String[] f = parseCsvLine(line);
                for (int i = 0; i < 6; i++) ps.setString(i + 1, col(f, i));
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  LG: " + count + "건");
    }

    // =========================================================================
    // LG_ATTRIBUTES 적재 (GroupAttributes.csv)
    // =========================================================================

    private static void loadLGAttr(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO loinc.lg_attributes (parent_lg_id, lg_id, type, value) VALUES (?, ?, ?, ?)";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "");
                if (line.isEmpty()) continue;
                String[] f = parseCsvLine(line);
                for (int i = 0; i < 4; i++) ps.setString(i + 1, col(f, i));
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  LG_ATTRIBUTES: " + count + "건");
    }

    // =========================================================================
    // LG_TERMS 적재 (GroupLoincTerms.csv)
    // =========================================================================

    private static void loadLGTerms(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO loinc.lg_terms (category, lg_id, archetype, loinc_number, long_common_name, lg_id_name)" +
                     " VALUES (?, ?, ?, ?, ?, ?)";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "");
                if (line.isEmpty()) continue;
                String[] f = parseCsvLine(line);
                for (int i = 0; i < 6; i++) ps.setString(i + 1, col(f, i));
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  LG_TERMS: " + count + "건");
    }

    // =========================================================================
    // LINGUISTIC_VARIANTS 적재
    // =========================================================================

    private static void loadLinguisticVariants(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO loinc.linguistic_variants (id, iso_lang, iso_country, lang, producer)" +
                     " VALUES (?, ?, ?, ?, ?)";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "");
                if (line.isEmpty()) continue;
                String[] f = parseCsvLine(line);
                for (int i = 0; i < 5; i++) ps.setString(i + 1, col(f, i));
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  LINGUISTIC_VARIANTS: " + count + "건");
    }

    // =========================================================================
    // LINGUISTIC_VARIANT 적재 (언어별)
    // =========================================================================

    private static void loadLinguisticVariant(Connection conn, File file) throws Exception {
        // Filename format: {iso_lang2}{ISO_COUNTRY2}{number}LinguisticVariant.csv
        // e.g., elGR17LinguisticVariant.csv → iso_lang="el", iso_country="GR"
        String fname = file.getName();
        String isoLang = fname.length() >= 2 ? fname.substring(0, 2) : "";
        String isoCountry = fname.length() >= 4 ? fname.substring(2, 4) : "";

        // CSV columns (12): LOINC_NUM, COMPONENT, PROPERTY, TIME_ASPCT, SYSTEM, SCALE_TYP,
        //   METHOD_TYP, CLASS, SHORTNAME, LONG_COMMON_NAME, RELATEDNAMES2, LinguisticVariantDisplayName
        String sql = "INSERT INTO loinc.linguistic_variant (code, component, property, time_aspect, system," +
                     " scale_type, method_type, class_name, short_name, long_common_name, related_names2," +
                     " lang, iso_lang, iso_country) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "");
                if (line.isEmpty()) continue;
                String[] f = parseCsvLine(line);
                ps.setString(1,  col(f, 0));   // code (LOINC_NUM)
                ps.setString(2,  col(f, 1));   // component
                ps.setString(3,  col(f, 2));   // property
                ps.setString(4,  col(f, 3));   // time_aspect
                ps.setString(5,  col(f, 4));   // system
                ps.setString(6,  col(f, 5));   // scale_type
                ps.setString(7,  col(f, 6));   // method_type
                ps.setString(8,  col(f, 7));   // class_name
                ps.setString(9,  col(f, 8));   // short_name
                ps.setString(10, col(f, 9));   // long_common_name
                ps.setString(11, col(f, 10));  // related_names2
                ps.setString(12, col(f, 11));  // lang (LinguisticVariantDisplayName)
                ps.setString(13, isoLang);
                ps.setString(14, isoCountry);
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  LINGUISTIC_VARIANT (" + file.getName() + "): " + count + "건");
    }

    // =========================================================================
    // PANEL 적재 (PanelsAndForms.csv)
    // =========================================================================

    private static void loadPanel(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO loinc.panel (parent_id, parent_code, parent_name, id, sequence, code, name," +
                     " display_name_for_form, observation_required_in_panel, observation_id_in_form," +
                     " skip_logic_help_text, default_value, entry_type, data_type_in_form, data_type_source," +
                     " answer_sequence_override, condition_for_inclusion, allowable_alternative," +
                     " observation_category, context, consistency_checks, relevance_equation," +
                     " coding_instructions, question_cardinality, answer_cardinality," +
                     " answer_list_id_override, answer_list_type_override, external_copyright_notice)" +
                     " VALUES (" + repeat("?", 28) + ")";
        long count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\r", "");
                if (line.isEmpty()) continue;
                String[] f = parseCsvLine(line);
                ps.setString(1, col(f, 0)); // parent_id
                ps.setString(2, col(f, 1)); // parent_code
                ps.setString(3, col(f, 2)); // parent_name
                ps.setString(4, col(f, 3)); // id
                ps.setObject(5, colInt(f, 4)); // sequence
                for (int i = 5; i < 28; i++) ps.setString(i + 1, col(f, i));
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            if (count % BATCH_SIZE != 0) ps.executeBatch();
        }
        log.info("  PANEL: " + count + "건");
    }

    // =========================================================================
    // HIERARCHY 빌드 (MySQL_additional.sql → PostgreSQL)
    // =========================================================================

    private static void buildHierarchy(Connection conn) throws Exception {
        try (Statement s = conn.createStatement()) {

            // 1. 루트 노드 (CLASS 타입)
            s.execute("INSERT INTO loinc.hierarchy (code, path, parent, type, sequence, name, preferred_name) VALUES " +
                "('LABORATORY','CLASS','CLASS',1,1,'Laboratory Class','Laboratory Class')," +
                "('CLINICAL','CLASS','CLASS',1,2,'Clinical Class','Clinical Class')," +
                "('CLAIMS_ATTACHMENT','CLASS','CLASS',1,3,'Claims Attachment','Claims Attachment')," +
                "('SURVEYS','CLASS','CLASS',1,4,'Surveys','Surveys')");

            // 2. CLASS 테이블에서 CLASS 노드 삽입
            s.execute("INSERT INTO loinc.hierarchy (code, path, parent, type, sequence, name, preferred_name) " +
                "SELECT abbreviation, 'CLASS~' || UPPER(name), UPPER(name), 1, 0, term, term FROM loinc.class");

            // 3. LOINC 코드를 HIERARCHY로 삽입 (CLASS 기반 PATH)
            s.execute("INSERT INTO loinc.hierarchy (code, path, parent, type, sequence, name) " +
                "SELECT l.code, 'CLASS~' || UPPER(j0.name) || '~' || j0.abbreviation, j0.abbreviation, 1, 1, l.short_name " +
                "FROM loinc.loinc AS l " +
                "JOIN loinc.class AS j0 ON REPLACE(l.class_name, '/', '-') = j0.abbreviation");

            // 4. PATH/PARENT 공백 → '#' 치환
            s.execute("UPDATE loinc.hierarchy SET parent = REPLACE(parent, ' ', '#')");
            s.execute("UPDATE loinc.hierarchy SET path = REPLACE(path, ' ', '#')");

            // 5. PREFERRED_NAME 생성
            s.execute("UPDATE loinc.hierarchy h SET preferred_name = " +
                "l.component || ':' || l.property || ':' || l.time_aspect || ':' || l.system || ':' || l.scale_type " +
                "FROM loinc.loinc l WHERE h.code = l.code");

            s.execute("UPDATE loinc.hierarchy h SET preferred_name = " +
                "l.component || ':' || l.property || ':' || l.time_aspect || ':' || l.system || ':' || l.scale_type || ':' || l.method_type " +
                "FROM loinc.loinc l WHERE h.code = l.code AND l.method_type IS NOT NULL AND l.method_type <> ''");

            // 6. HIERARCHY_TMP PATH 변환 (.→~ , PARTS~ 접두사)
            s.execute("UPDATE loinc.hierarchy_tmp SET path = REPLACE(path, '.', '~')");
            s.execute("UPDATE loinc.hierarchy_tmp SET path = 'PARTS~' || path");
            s.execute("UPDATE loinc.hierarchy_tmp SET path = 'PARTS', parent = 'PARTS', preferred_name = name WHERE path = 'PARTS~'");
            s.execute("UPDATE loinc.hierarchy_tmp SET type = 2");
            s.execute("UPDATE loinc.hierarchy_tmp SET preferred_name = name WHERE preferred_name IS NULL");

            // HIERARCHY_TMP에서 LOINC 코드 PREFERRED_NAME 채우기
            s.execute("UPDATE loinc.hierarchy_tmp ht SET preferred_name = " +
                "l.component || ':' || l.property || ':' || l.time_aspect || ':' || l.system || ':' || l.scale_type " +
                "FROM loinc.loinc l WHERE ht.code = l.code");

            // 7. HIERARCHY_TMP → HIERARCHY로 병합
            s.execute("INSERT INTO loinc.hierarchy (path, sequence, parent, code, name, preferred_name, type) " +
                "SELECT path, sequence, parent, code, name, preferred_name, type FROM loinc.hierarchy_tmp " +
                "ON CONFLICT DO NOTHING");

            conn.commit();
        }
    }

    // =========================================================================
    // HIERARCHY_LG 빌드 (MySQL_additional2.sql → PostgreSQL)
    // =========================================================================

    private static void buildHierarchyLG(Connection conn) throws Exception {
        try (Statement s = conn.createStatement()) {

            // 그룹 루트
            s.execute("INSERT INTO loinc.hierarchy_lg (code, path, parent, type, sequence, name, preferred_name) VALUES " +
                "('Document groups','GROUP','GROUP',1,0,'Document groups','Document groups')," +
                "('Drugs of abuse','GROUP','GROUP',1,0,'Drugs of abuse','Drugs of abuse')," +
                "('Exercise','GROUP','GROUP',1,0,'Exercise','Exercise')," +
                "('Eye microbiology','GROUP','GROUP',1,0,'Eye microbiology','Eye microbiology')," +
                "('Flowsheet - laboratory','GROUP','GROUP',1,0,'Flowsheet - laboratory','Flowsheet - laboratory')," +
                "('Flowsheet - Vital signs','GROUP','GROUP',1,0,'Flowsheet - Vital signs','Flowsheet - Vital signs')," +
                "('Flowsheet - weight, height, and head circumference','GROUP','GROUP',1,0," +
                " 'Flowsheet - weight, height, and head circumference','Flowsheet - weight, height, and head circumference')," +
                "('Genitourinary microbiology','GROUP','GROUP',1,0,'Genitourinary microbiology','Genitourinary microbiology')," +
                "('Mass-Molar conversion','GROUP','GROUP',1,0,'Mass-Molar conversion','Mass-Molar conversion')," +
                "('Obstetrics','GROUP','GROUP',1,0,'Obstetrics','Obstetrics')," +
                "('Radiology','GROUP','GROUP',1,0,'Radiology','Radiology')," +
                "('Reportable microbiology','GROUP','GROUP',1,0,'Reportable microbiology','Reportable microbiology')," +
                "('Respiratory microbiology','GROUP','GROUP',1,0,'Respiratory microbiology','Respiratory microbiology')," +
                "('Smoking - biochemical markers','GROUP','GROUP',1,0,'Smoking - biochemical markers','Smoking - biochemical markers')," +
                "('Smoking - history','GROUP','GROUP',1,0,'Smoking - history','Smoking - history')," +
                "('Social determinants of health','GROUP','GROUP',1,0,'Social determinants of health','Social determinants of health')");

            // LG_TERMS → HIERARCHY_LG
            s.execute("INSERT INTO loinc.hierarchy_lg (code, path, parent, type, sequence, name) " +
                "SELECT t.loinc_number, 'GROUP~' || t.category, lg_id, 3, 1, long_common_name FROM loinc.lg_terms t");

            // PARENT_LG → HIERARCHY_LG
            s.execute("INSERT INTO loinc.hierarchy_lg (code, path, parent, type, sequence, name, preferred_name) " +
                "SELECT DISTINCT p.parent_lg_id, 'GROUP~' || j2.f2, j2.f2, 3, 1, p.parent_lg, p.parent_lg " +
                "FROM loinc.parent_lg AS p " +
                "JOIN (" +
                "  SELECT DISTINCT f1, t.category AS f2, j.f3, j.f6 " +
                "  FROM loinc.lg_terms AS t " +
                "  JOIN (SELECT lg.lg_id AS f1, lg.parent_lg_id AS f2, lg.parent_lg_id AS f3, lg.lg AS f6 " +
                "        FROM loinc.lg AS lg WHERE lg.lg_id IN (" +
                "          SELECT DISTINCT h.parent FROM loinc.hierarchy_lg AS h) AND lg.status = 'Active') AS j " +
                "  ON t.lg_id = j.f1" +
                ") AS j2 ON p.status = 'ACTIVE' AND p.parent_lg_id = j2.f3");

            conn.commit();
        }
    }

    // =========================================================================
    // PANEL PATH/ROOT_CODE 빌드 (MySQL_additional3.sql → PostgreSQL)
    // =========================================================================

    private static void buildPanelPaths(Connection conn) throws Exception {
        try (Statement s = conn.createStatement()) {

            s.execute("UPDATE loinc.panel SET version = '2.82'");

            // 1st PATH: parent의 code를 path로
            s.execute("UPDATE loinc.panel p1 SET path = p2.parent_code " +
                "FROM (SELECT id, parent_code FROM loinc.panel GROUP BY id, parent_code) p2 " +
                "WHERE p1.parent_id = p2.id");

            // root 설정
            s.execute("UPDATE loinc.panel SET parent_code = 'ROOT', root_code = code, path = code WHERE code = parent_code");

            // root_id 설정
            s.execute("UPDATE loinc.panel p1 SET root_code = p2.root_code " +
                "FROM (SELECT id, root_code FROM loinc.panel WHERE parent_code = 'ROOT') p2 " +
                "WHERE p1.parent_id = p2.id");

            // PATH 연결 (2단계 반복)
            for (int i = 0; i < 5; i++) {
                s.execute("UPDATE loinc.panel p1 SET path = p1.path || '~' || p2.code, root_code = p2.parent_id " +
                    "FROM (SELECT id, code, parent_id, root_code, path FROM loinc.panel) p2 " +
                    "WHERE p1.root_code IS NOT NULL AND p1.root_code NOT LIKE '%-%' AND p1.parent_id = p2.id");
                conn.commit();
            }

            // ROOT_CODE: ID → CODE
            s.execute("UPDATE loinc.panel p1 SET root_code = p2.code " +
                "FROM (SELECT id, code, parent_code FROM loinc.panel) p2 " +
                "WHERE p1.root_code NOT LIKE '%-%' AND p1.root_code = CAST(p2.id AS VARCHAR) AND p2.parent_code = 'ROOT'");

            conn.commit();
        }
    }

    // =========================================================================
    // children/descendant count 계산
    // =========================================================================

    private static void buildCounts(Connection conn) throws Exception {
        try (Statement s = conn.createStatement()) {
            // HIERARCHY children count
            s.execute("UPDATE loinc.hierarchy h SET children_count = c.cnt " +
                "FROM (SELECT parent, COUNT(*) AS cnt FROM loinc.hierarchy GROUP BY parent) c " +
                "WHERE h.code = c.parent");

            // HIERARCHY descendant count (LIKE PATH)
            s.execute("UPDATE loinc.hierarchy h SET descendant_count = c.cnt " +
                "FROM (SELECT h2.path AS p, COUNT(*) AS cnt FROM loinc.hierarchy h1 " +
                "      JOIN loinc.hierarchy h2 ON h1.path LIKE h2.path || '%' AND h1.path <> h2.path " +
                "      GROUP BY h2.path) c " +
                "WHERE h.path = c.p");

            // HIERARCHY_LG children count
            s.execute("UPDATE loinc.hierarchy_lg h SET children_count = c.cnt " +
                "FROM (SELECT parent, COUNT(*) AS cnt FROM loinc.hierarchy_lg GROUP BY parent) c " +
                "WHERE h.code = c.parent");

            conn.commit();
        }
    }

    // =========================================================================
    // 유틸
    // =========================================================================

    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(',');
            sb.append(s);
        }
        return sb.toString();
    }
}
