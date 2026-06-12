package co.infoclinic.term.common.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

/**
 * SNOMED CT RF2 전체 데이터 적재기 (MySQL LOAD DATA → PostgreSQL COPY 변환)
 *
 * MySQLLoader.sql의 LOAD DATA LOCAL INFILE 구문을 Java로 변환한 클래스.
 * release_files 디렉터리의 추출된 RF2 파일들을 PostgreSQL term 스키마에 적재한다.
 *
 * 실행 방법:
 *   java SnomedDataLoader [release_files_base_path]
 *   인자 생략 시 프로젝트 루트 아래 release_files/ 디렉터리를 기본값으로 사용.
 *
 * 적재 순서:
 *   1. term 스키마 초기화 (schema_term_postgresql.sql 실행)
 *   2. SCHEME 메타데이터 삽입
 *   3. 국제판(International RF2) 핵심 테이블 적재
 *   4. release_files 내 모든 애드온 패키지 Refset 적재
 *
 * 성능:
 *   고용량 테이블(CONCEPT, DESCRIPTION, RELATIONSHIP)은 PostgreSQL COPY FROM STDIN을 사용.
 *   REFERENCESET은 파일마다 컬럼 수가 다르므로 PreparedStatement 배치 적재를 사용.
 */
public class SnomedDataLoader {

    private static final String JDBC_URL      = "jdbc:postgresql://localhost:5432/term";
    private static final String JDBC_USER     = "postgres";
    private static final String JDBC_PASSWORD = "julab123!";

    /** PreparedStatement 배치 크기: 메모리와 커밋 빈도의 균형점 */
    private static final int BATCH_SIZE = 5000;

    public static void main(String[] args) throws Exception {
        // 인자로 release_files 경로를 받거나, 기본값으로 프로젝트 내 디렉터리 사용
        String releaseFilesBase = args.length > 0
                ? args[0]
                : Paths.get("").toAbsolutePath().resolve("release_files").toString();

        Path basePath = Paths.get(releaseFilesBase).toAbsolutePath().normalize();
        if (!Files.isDirectory(basePath)) {
            System.err.println("[ERROR] release_files 디렉터리를 찾을 수 없습니다: " + basePath);
            System.exit(1);
        }

        System.out.println("[INFO] release_files 경로: " + basePath);

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);
            initSchema(conn);
            loadScheme(conn, basePath);
            loadInternational(conn, basePath);
            loadAddonPackages(conn, basePath);
            conn.commit();

            // REFERENCESET에서 최신 활성 행 추출 → REFERENCESET_ACTIVE 적재
            // (refset_active_ddl.sql + refset_active_loader.sql 통합 Java 버전)
            System.out.println("[INFO] REFERENCESET_ACTIVE 테이블 적재 시작...");
            ReferenceSetActiveLoader.load(conn);

            // IS-A 계층 Transitive Closure 계산 및 TC 테이블 적재
            // (TransitiveClosureGeneratorFromInferred.sh + TransitiveClosureGenerator.java 통합 Java 버전)
            System.out.println("[INFO] TC (Transitive Closure) 테이블 적재 시작...");
            TransitiveClosureLoader.load(conn);
            conn.commit();

            // SEARCH_INDEX 적재 (Elasticsearch 대체: pg_trgm GIN 인덱스 기반 검색)
            System.out.println("[INFO] SEARCH_INDEX 테이블 적재 시작...");
            SearchIndexLoader.load(conn);
            conn.commit();

            System.out.println("[INFO] 모든 데이터 적재 완료.");
        }
    }

    // -------------------------------------------------------------------------
    // 스키마 초기화
    // -------------------------------------------------------------------------

    /**
     * classpath의 schema_term_postgresql.sql을 읽어 term 스키마를 재생성한다.
     * DROP SCHEMA IF EXISTS term CASCADE로 기존 데이터가 모두 삭제되므로 주의.
     */
    private static void initSchema(Connection conn) throws SQLException, IOException {
        System.out.println("[INFO] term 스키마 초기화 중...");

        URL sqlUrl = SnomedDataLoader.class.getClassLoader().getResource("schema_term_postgresql.sql");
        if (sqlUrl == null) {
            throw new IllegalStateException("schema_term_postgresql.sql을 classpath에서 찾을 수 없습니다.");
        }

        String ddl = readResourceAsString("schema_term_postgresql.sql");
        List<String> statements = splitSqlStatements(ddl);

        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                if (!sql.isBlank()) {
                    stmt.execute(sql);
                }
            }
            stmt.execute("SET search_path TO term");
        }

        conn.commit();
        System.out.println("[INFO] 스키마 초기화 완료.");
    }

    /**
     * SQL 파일을 ';' 기준으로 분리한다.
     * '--' 주석 라인과 빈 줄은 유지하되 빈 구문은 제외한다.
     */
    private static List<String> splitSqlStatements(String ddl) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String line : ddl.split("\n")) {
            // 주석 라인은 구문 분리에 포함하지 않음
            String trimmed = line.stripTrailing();
            current.append(trimmed).append("\n");

            if (trimmed.stripLeading().startsWith("--")) {
                continue;
            }

            // ';' 로 끝나는 라인에서 구문 분리
            if (trimmed.endsWith(";")) {
                String stmt = current.toString().trim();
                if (!stmt.isEmpty()) {
                    result.add(stmt);
                }
                current.setLength(0);
            }
        }

        // 마지막 미종료 구문 처리
        String remaining = current.toString().trim();
        if (!remaining.isEmpty()) {
            result.add(remaining);
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // SCHEME 메타데이터
    // -------------------------------------------------------------------------

    /**
     * release_files 하위 국제판 디렉터리명에서 배포일을 추출해 SCHEME 테이블에 삽입한다.
     * MySQLLoader.sql의 INSERT INTO term.SCHEME ... 구문을 대체.
     */
    private static void loadScheme(Connection conn, Path basePath) throws SQLException, IOException {
        System.out.println("[INFO] SCHEME 메타데이터 삽입 중...");

        // 국제판 디렉터리 이름에서 배포일(YYYYMMDD) 추출
        String releaseDate = findReleaseDate(basePath, "SnomedCT_InternationalRF2_PRODUCTION_");
        if (releaseDate == null) {
            releaseDate = "20260601";
        }

        String id        = "SNOMEDCT-v" + releaseDate;
        String name      = "SNOMEDCT-Int";
        String edition   = "SNOMEDCT v" + releaseDate;
        String version   = "v" + releaseDate;
        String authority = "SNOMED International";
        String date      = releaseDate;

        // ON CONFLICT로 재실행 시 중복 삽입 방지
        String sql = "INSERT INTO term.scheme (id, name, edition, version, authority, date) "
                   + "VALUES (?, ?, ?, ?, ?, ?) "
                   + "ON CONFLICT (id) DO UPDATE SET "
                   + "name=EXCLUDED.name, edition=EXCLUDED.edition, "
                   + "version=EXCLUDED.version, authority=EXCLUDED.authority, date=EXCLUDED.date";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, name);
            ps.setString(3, edition);
            ps.setString(4, version);
            ps.setString(5, authority);
            ps.setString(6, date);
            ps.executeUpdate();
        }

        conn.commit();
        System.out.println("[INFO] SCHEME 삽입 완료: " + id);
    }

    /** 패키지 디렉터리명 접두어로 배포일(YYYYMMDD) 추출 */
    private static String findReleaseDate(Path basePath, String packagePrefix) throws IOException {
        try (Stream<Path> dirs = Files.list(basePath)) {
            return dirs
                .filter(Files::isDirectory)
                .map(p -> p.getFileName().toString())
                .filter(name -> name.startsWith(packagePrefix))
                .findFirst()
                .map(name -> {
                    // 디렉터리명에서 8자리 날짜 추출 (예: ..._20260601T120000Z)
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{8})T").matcher(name);
                    return m.find() ? m.group(1) : null;
                })
                .orElse(null);
        }
    }

    // -------------------------------------------------------------------------
    // 국제판 핵심 테이블 적재
    // -------------------------------------------------------------------------

    /**
     * SnomedCT_InternationalRF2_PRODUCTION_* 패키지에서 핵심 테이블을 적재한다.
     * MySQL LOAD DATA → PostgreSQL COPY FROM STDIN (CopyManager 사용).
     */
    private static void loadInternational(Connection conn, Path basePath) throws Exception {
        System.out.println("[INFO] 국제판 핵심 테이블 적재 시작...");

        Path intlBase = findPackageDir(basePath, "SnomedCT_InternationalRF2_PRODUCTION_");
        if (intlBase == null) {
            System.out.println("[WARN] 국제판 디렉터리를 찾을 수 없어 건너뜁니다.");
            return;
        }

        Path fullTermDir     = intlBase.resolve("Full/Terminology");
        Path snapshotTermDir = intlBase.resolve("Snapshot/Terminology");

        // -- CONCEPT (Full) --
        // RF2 컬럼 순서: id, effectiveTime, active, moduleId, definitionStatusId
        copyFromFile(conn, fullTermDir, "sct2_Concept_Full_",
            "COPY term.concept (concept_id, effective_time, active, module_id, definition_status_id) " +
            "FROM STDIN WITH (FORMAT TEXT, DELIMITER E'\\t', HEADER TRUE, NULL '')");

        // -- DESCRIPTION (Full) --
        // RF2 컬럼 순서: id, effectiveTime, active, moduleId, conceptId, languageCode, typeId, term, caseSignificanceId
        copyFromFile(conn, fullTermDir, "sct2_Description_Full-en_",
            "COPY term.description (description_id, effective_time, active, module_id, concept_id, language_code, type_id, term, case_significance_id) " +
            "FROM STDIN WITH (FORMAT TEXT, DELIMITER E'\\t', HEADER TRUE, NULL '')");

        // -- INFERRED_RELATIONSHIP (Full) --
        // RF2 컬럼 순서: id, effectiveTime, active, moduleId, sourceId, destinationId, relationshipGroup, typeId, characteristicTypeId, modifierId
        copyFromFile(conn, fullTermDir, "sct2_Relationship_Full_",
            "COPY term.inferred_relationship (relationship_id, effective_time, active, module_id, source_id, destination_id, relationship_group, type_id, characteristic_type_id, modifier_id) " +
            "FROM STDIN WITH (FORMAT TEXT, DELIMITER E'\\t', HEADER TRUE, NULL '')");

        // ConcreteValues도 INFERRED_RELATIONSHIP에 통합 적재 (MySQLLoader.sql 동일)
        copyFromFile(conn, fullTermDir, "sct2_RelationshipConcreteValues_Full_",
            "COPY term.inferred_relationship (relationship_id, effective_time, active, module_id, source_id, destination_id, relationship_group, type_id, characteristic_type_id, modifier_id) " +
            "FROM STDIN WITH (FORMAT TEXT, DELIMITER E'\\t', HEADER TRUE, NULL '')");

        // -- INFERRED_RELATIONSHIP_SNAP (Snapshot) --
        copyFromFile(conn, snapshotTermDir, "sct2_Relationship_Snapshot_",
            "COPY term.inferred_relationship_snap (relationship_id, effective_time, active, module_id, source_id, destination_id, relationship_group, type_id, characteristic_type_id, modifier_id) " +
            "FROM STDIN WITH (FORMAT TEXT, DELIMITER E'\\t', HEADER TRUE, NULL '')");

        copyFromFile(conn, snapshotTermDir, "sct2_RelationshipConcreteValues_Snapshot_",
            "COPY term.inferred_relationship_snap (relationship_id, effective_time, active, module_id, source_id, destination_id, relationship_group, type_id, characteristic_type_id, modifier_id) " +
            "FROM STDIN WITH (FORMAT TEXT, DELIMITER E'\\t', HEADER TRUE, NULL '')");

        // -- STATED_RELATIONSHIP (Full) --
        copyFromFile(conn, fullTermDir, "sct2_StatedRelationship_Full_",
            "COPY term.stated_relationship (relationship_id, effective_time, active, module_id, source_id, destination_id, relationship_group, type_id, characteristic_type_id, modifier_id) " +
            "FROM STDIN WITH (FORMAT TEXT, DELIMITER E'\\t', HEADER TRUE, NULL '')");

        // -- 국제판 REFERENCESET (Full/Refset/**) --
        loadAllRefsets(conn, intlBase.resolve("Full/Refset"));

        conn.commit();
        System.out.println("[INFO] 국제판 적재 완료.");
    }

    // -------------------------------------------------------------------------
    // 애드온 패키지 적재
    // -------------------------------------------------------------------------

    /**
     * release_files 디렉터리 내 국제판 외 모든 애드온 패키지를 자동 탐색하여 적재한다.
     * LOINC RF2, NCPT 등 import_loinc.sql / import_ncptr.sql에 정의된 패키지 포함.
     * 파일이 없는 패키지는 경고 메시지 후 건너뛴다.
     */
    private static void loadAddonPackages(Connection conn, Path basePath) throws Exception {
        System.out.println("[INFO] 애드온 패키지 적재 시작...");

        try (Stream<Path> dirs = Files.list(basePath)) {
            List<Path> packages = dirs
                .filter(Files::isDirectory)
                // 국제판만 제외; LOINCRF2 / NCPT 등은 모두 포함
                .filter(p -> !p.getFileName().toString().startsWith("SnomedCT_InternationalRF2_"))
                .sorted()
                .toList();

            for (Path pkg : packages) {
                System.out.println("[INFO] 패키지 적재: " + pkg.getFileName());

                // Full/Terminology: CONCEPT, DESCRIPTION, RELATIONSHIP, OWL Refset
                loadAddonTerminology(conn, pkg, false);

                // Snapshot/Terminology: INFERRED_RELATIONSHIP_SNAP
                // (import_loinc.sql, import_ncptr.sql 에서 Snapshot Relationship을 SNAP 테이블에 적재)
                loadAddonTerminology(conn, pkg, true);

                // Full/Refset: REFERENCESET (der2_* 파일들)
                Path refsetDir = pkg.resolve("Full/Refset");
                if (Files.isDirectory(refsetDir)) {
                    loadAllRefsets(conn, refsetDir);
                }

                // Snapshot/Refset: 일부 패키지는 Snapshot에만 Refset이 있음
                Path snapRefsetDir = pkg.resolve("Snapshot/Refset");
                if (Files.isDirectory(snapRefsetDir)) {
                    loadAllRefsets(conn, snapRefsetDir);
                }
            }
        }

        conn.commit();
        System.out.println("[INFO] 애드온 패키지 적재 완료.");
    }

    /**
     * 애드온 패키지의 Terminology 파일을 적재한다.
     *
     * isSnapshot=false → Full/Terminology
     *   sct2_Concept_*        → CONCEPT
     *   sct2_Description_*    → DESCRIPTION
     *   sct2_Relationship_*   → INFERRED_RELATIONSHIP
     *   sct2_StatedRelationship_* → STATED_RELATIONSHIP
     *   sct2_sRefset_*        → REFERENCESET (OWL Expression 등 Terminology에 포함된 Refset)
     *
     * isSnapshot=true → Snapshot/Terminology
     *   sct2_Relationship_*   → INFERRED_RELATIONSHIP_SNAP
     *   (import_loinc.sql, import_ncptr.sql 에서 Snapshot Relationship을 SNAP 테이블에 적재)
     */
    private static void loadAddonTerminology(Connection conn, Path packageDir, boolean isSnapshot)
            throws Exception {
        Path termDir = packageDir.resolve(isSnapshot ? "Snapshot/Terminology" : "Full/Terminology");
        if (!Files.isDirectory(termDir)) {
            return;
        }

        try (Stream<Path> files = Files.list(termDir)) {
            List<Path> txtFiles = files
                .filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".txt"))
                .sorted()
                .toList();

            for (Path file : txtFiles) {
                String lname = file.getFileName().toString().toLowerCase(Locale.ROOT);

                if (isSnapshot) {
                    // Snapshot/Terminology: Relationship 파일만 INFERRED_RELATIONSHIP_SNAP에 적재
                    if (lname.startsWith("sct2_relationship_") || lname.contains("relationshipconcretevalues_")) {
                        copyFile(conn, file,
                            "COPY term.inferred_relationship_snap " +
                            "(relationship_id, effective_time, active, module_id, source_id, destination_id, " +
                            "relationship_group, type_id, characteristic_type_id, modifier_id) " +
                            "FROM STDIN WITH (FORMAT TEXT, DELIMITER E'\\t', HEADER TRUE, NULL '')");
                    }
                } else {
                    // Full/Terminology
                    if (lname.startsWith("sct2_concept_")) {
                        copyFile(conn, file,
                            "COPY term.concept " +
                            "(concept_id, effective_time, active, module_id, definition_status_id) " +
                            "FROM STDIN WITH (FORMAT TEXT, DELIMITER E'\\t', HEADER TRUE, NULL '')");

                    } else if (lname.startsWith("sct2_description_")) {
                        copyFile(conn, file,
                            "COPY term.description " +
                            "(description_id, effective_time, active, module_id, concept_id, language_code, " +
                            "type_id, term, case_significance_id) " +
                            "FROM STDIN WITH (FORMAT TEXT, DELIMITER E'\\t', HEADER TRUE, NULL '')");

                    } else if (lname.startsWith("sct2_statedrelationship_")) {
                        copyFile(conn, file,
                            "COPY term.stated_relationship " +
                            "(relationship_id, effective_time, active, module_id, source_id, destination_id, " +
                            "relationship_group, type_id, characteristic_type_id, modifier_id) " +
                            "FROM STDIN WITH (FORMAT TEXT, DELIMITER E'\\t', HEADER TRUE, NULL '')");

                    } else if (lname.startsWith("sct2_relationship_") || lname.contains("relationshipconcretevalues_")) {
                        copyFile(conn, file,
                            "COPY term.inferred_relationship " +
                            "(relationship_id, effective_time, active, module_id, source_id, destination_id, " +
                            "relationship_group, type_id, characteristic_type_id, modifier_id) " +
                            "FROM STDIN WITH (FORMAT TEXT, DELIMITER E'\\t', HEADER TRUE, NULL '')");

                    } else if (lname.startsWith("sct2_srefset_")) {
                        // OWL Expression 등 Terminology 폴더에 위치한 Refset 파일 → REFERENCESET
                        // (import_ncptr.sql: sct2_sRefset_OWLExpressionNCPTFull_*.txt → REFERENCESET)
                        loadRefsetFile(conn, file);
                    }
                    // sct2_Identifier, sct2_TextDefinition 등은 DDL 대상 테이블 없으므로 건너뜀
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // REFERENCESET 적재 (PreparedStatement 배치)
    // -------------------------------------------------------------------------

    /**
     * 지정된 Refset 디렉터리 아래 모든 .txt 파일을 REFERENCESET 테이블에 적재한다.
     * 파일마다 컬럼 수(헤더)가 다르므로 COPY 대신 PreparedStatement 배치 사용.
     *
     * RF2 Refset 공통 컬럼 (항상 존재):
     *   [0] id  [1] effectiveTime  [2] active  [3] moduleId  [4] refsetId  [5] referencedComponentId
     * 이후 FIELD1~FIELD7은 Refset 유형에 따라 존재 여부가 다름.
     */
    private static void loadAllRefsets(Connection conn, Path refsetDir) throws Exception {
        if (!Files.isDirectory(refsetDir)) {
            return;
        }

        try (Stream<Path> walk = Files.walk(refsetDir)) {
            List<Path> refsetFiles = walk
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".txt"))
                .sorted()
                .toList();

            for (Path file : refsetFiles) {
                loadRefsetFile(conn, file);
            }
        }
    }

    /**
     * 단일 Refset 파일을 REFERENCESET 테이블에 적재한다.
     * 헤더 라인으로 실제 컬럼 수를 파악하여 동적으로 FIELD 컬럼 수를 결정한다.
     *
     * REFERENCESET UUID 충돌 시 ON CONFLICT DO NOTHING으로 처리
     * (동일 배포판 재적재 또는 패키지 간 중복 멤버 방어).
     */
    private static void loadRefsetFile(Connection conn, Path file) throws Exception {
        String sql = "INSERT INTO term.referenceset "
            + "(referenceset_id, effective_time, active, module_id, refset_id, referenced_component_id, "
            + "field1, field2, field3, field4, field5, field6, field7) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON CONFLICT (referenceset_id) DO NOTHING";

        long count = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file.toFile()), StandardCharsets.UTF_8));
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                return; // 빈 파일
            }

            // 헤더 컬럼 수로 FIELD 컬럼 수 계산 (6개 공통 컬럼 이후 나머지가 FIELD)
            int headerCols = headerLine.split("\t", -1).length;
            int fieldCount = Math.min(headerCols - 6, 7); // 최대 FIELD7까지

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] cols = line.split("\t", -1);
                if (cols.length < 6) {
                    continue;
                }

                // 공통 6개 컬럼
                ps.setString(1, cols[0]); // referenceset_id (UUID)
                ps.setString(2, cols[1]); // effective_time
                ps.setShort(3, parseShort(cols[2])); // active
                ps.setString(4, cols[3]); // module_id
                ps.setString(5, cols[4]); // refset_id
                ps.setString(6, cols[5]); // referenced_component_id

                // FIELD1~FIELD7: 파일에 없는 컬럼은 NULL
                for (int i = 1; i <= 7; i++) {
                    int colIdx = 5 + i; // cols[6]=FIELD1, cols[7]=FIELD2, ...
                    if (i <= fieldCount && colIdx < cols.length && !cols[colIdx].isEmpty()) {
                        ps.setString(6 + i, cols[colIdx]);
                    } else {
                        ps.setNull(6 + i, java.sql.Types.VARCHAR);
                    }
                }

                ps.addBatch();
                count++;

                if (count % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();
                }
            }

            if (count % BATCH_SIZE != 0) {
                ps.executeBatch();
            }
        }

        System.out.println("[INFO]   REFERENCESET 적재 완료: " + file.getFileName() + " (" + count + "건)");
    }

    // -------------------------------------------------------------------------
    // PostgreSQL COPY 유틸리티
    // -------------------------------------------------------------------------

    /**
     * 지정 디렉터리에서 fileNamePrefix로 시작하는 .txt 파일을 찾아 COPY FROM STDIN으로 적재한다.
     * 파일이 없으면 경고 후 건너뛴다.
     */
    private static void copyFromFile(Connection conn, Path dir, String fileNamePrefix, String copySql)
            throws Exception {
        if (!Files.isDirectory(dir)) {
            System.out.println("[WARN] 디렉터리 없음: " + dir);
            return;
        }

        Path file = findFile(dir, fileNamePrefix);
        if (file == null) {
            System.out.println("[WARN] 파일 없음 (prefix=" + fileNamePrefix + "): " + dir);
            return;
        }

        copyFile(conn, file, copySql);
    }

    /**
     * 파일을 PostgreSQL COPY FROM STDIN으로 직접 스트리밍 적재한다.
     * MySQL의 LOAD DATA LOCAL INFILE과 동일하게 클라이언트 측에서 파일을 서버로 전송.
     */
    private static void copyFile(Connection conn, Path file, String copySql) throws Exception {
        CopyManager copyManager = ((BaseConnection) conn.unwrap(BaseConnection.class)).getCopyAPI();

        try (InputStream is = new FileInputStream(file.toFile())) {
            long rows = copyManager.copyIn(copySql, is);
            conn.commit();
            System.out.println("[INFO]   COPY 완료: " + file.getFileName() + " (" + rows + "행)");
        } catch (Exception e) {
            System.out.println("[ERROR] COPY 실패: " + file.getFileName() + " - " + e.getMessage());
            conn.rollback();
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    // 유틸리티
    // -------------------------------------------------------------------------

    /** 디렉터리에서 지정 접두어로 시작하는 첫 번째 .txt 파일 반환 */
    private static Path findFile(Path dir, String prefix) throws IOException {
        String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).startsWith(lowerPrefix)
                          && p.toString().toLowerCase(Locale.ROOT).endsWith(".txt"))
                .findFirst()
                .orElse(null);
        }
    }

    /** release_files에서 접두어로 패키지 디렉터리 탐색 */
    private static Path findPackageDir(Path basePath, String prefix) throws IOException {
        try (Stream<Path> stream = Files.list(basePath)) {
            return stream
                .filter(Files::isDirectory)
                .filter(p -> p.getFileName().toString().startsWith(prefix))
                .findFirst()
                .orElse(null);
        }
    }

    /** classpath 리소스를 문자열로 읽기 */
    private static String readResourceAsString(String resourceName) throws IOException {
        try (InputStream is = SnomedDataLoader.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IllegalStateException("classpath 리소스 없음: " + resourceName);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** RF2 active 플래그(0/1)를 SMALLINT로 변환, 파싱 실패 시 0 반환 */
    private static short parseShort(String value) {
        try {
            return Short.parseShort(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
