package co.infoclinic.term.common.loader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * REFERENCESET_ACTIVE 테이블 적재기
 *
 * refset_active_ddl.sql + refset_active_loader.sql 의 통합 Java 버전.
 * REFERENCESET(RF2 원본 전체 이력)에서 각 refset_id별 최신 활성(active=1) 행을 추출하여
 * 비정규화된 REFERENCESET_ACTIVE 테이블에 적재한다.
 *
 * MySQL → PostgreSQL 변환 사항:
 *   - 문자열 리터럴: "value" → 'value'
 *   - GROUP BY + JOIN(MAX) → DISTINCT ON ... ORDER BY effective_time DESC
 *     (idx_referenceset_r_id_rs_id_etime 인덱스 활용, HashAggregate 3.5M행 제거)
 *   - ORDER BY NULL 제거 (MySQL 최적화 힌트)
 *   - 다중 테이블 UPDATE → UPDATE ... FROM 구문
 *   - \! echo, SET @@GLOBAL 제거
 *
 * 쿼리 최적화 핵심:
 *   원본: MAX(effective_time) GROUP BY referenceset_id + JOIN back
 *     → Seq Scan × 2 + HashAggregate (3.5M 행), 매우 느림
 *   개선: DISTINCT ON (referenceset_id) ORDER BY referenceset_id, effective_time DESC
 *     → idx_referenceset_r_id_rs_id_etime 인덱스 스캔, 훨씬 빠름
 */
public class ReferenceSetActiveLoader {

    private static final Logger log = Logger.getLogger(ReferenceSetActiveLoader.class.getName());

    private static final String JDBC_URL      = "jdbc:postgresql://localhost:5432/term";
    private static final String JDBC_USER     = "postgres";
    private static final String JDBC_PASSWORD = "julab123!";

    /**
     * 실행 옵션:
     *   (인수 없음)              전체 실행 (버전 날짜 자동 결정: inferred_relationship MAX)
     *   YYYYMMDD                버전 날짜 지정 후 전체 실행
     *   YYYYMMDD --update-only  UPDATE 단계만 실행 (INSERT 완료 후 중단된 경우 재개용)
     */
    public static void main(String[] args) throws Exception {
        String versionDate = null;
        boolean updateOnly = false;
        for (String arg : args) {
            if ("--update-only".equals(arg)) updateOnly = true;
            else if (arg.matches("\\d{8}")) versionDate = arg;
        }
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);
            try (Statement s = conn.createStatement()) {
                s.execute("SET search_path TO term");
            }
            if (updateOnly) {
                System.out.println("[INFO] --update-only 모드: UPDATE 단계만 실행합니다.");
                new ReferenceSetActiveLoader(resolveVersionDate(conn, versionDate)).runUpdates(conn);
            } else {
                load(conn, versionDate);
            }
            conn.commit();
        }
        System.out.println("[INFO] REFERENCESET_ACTIVE 적재 완료.");
    }

    /** SnomedDeltaLoader에서 직접 호출 (versionDate=null이면 DB에서 자동 결정) */
    public static void load(Connection conn, String versionDate) throws Exception {
        new ReferenceSetActiveLoader(resolveVersionDate(conn, versionDate)).doLoad(conn);
    }

    /** 하위 호환: versionDate 없이 호출 */
    public static void load(Connection conn) throws Exception {
        load(conn, null);
    }

    private static String resolveVersionDate(Connection conn, String versionDate) throws Exception {
        if (versionDate != null && !versionDate.isEmpty()) return versionDate;
        try (Statement s = conn.createStatement();
             java.sql.ResultSet rs = s.executeQuery(
                 "SELECT MAX(effective_time) FROM term.inferred_relationship")) {
            if (rs.next()) {
                String et = rs.getString(1);
                if (et != null && !et.isEmpty()) {
                    System.out.println("[INFO] VERSION_DATE 자동 결정: " + et);
                    return et;
                }
            }
        }
        throw new IllegalStateException("VERSION_DATE 자동 결정 실패 — 인수로 YYYYMMDD를 지정하세요.");
    }

    private final String VERSION_DATE;
    private final String VERSION_VALUE;

    public ReferenceSetActiveLoader() {
        this("20260601");
    }

    public ReferenceSetActiveLoader(String versionDate) {
        this.VERSION_DATE  = versionDate;
        this.VERSION_VALUE = "INT-" + versionDate;
        this.BASE_EXPRS    = "'" + this.VERSION_VALUE + "', r.referenceset_id, r.effective_time, r.module_id, '', " +
                             "r.refset_id, '', r.referenced_component_id, r.active, ''";
    }

    private final String BASE_EXPRS;

    // =========================================================================
    // DISTINCT ON 기반 INSERT 헬퍼
    // =========================================================================

    /**
     * 공통 INSERT 패턴.
     *
     * 원본 GROUP BY + JOIN(MAX) 방식:
     *   FROM (SELECT refset_id, referenceset_id, MAX(et) ... GROUP BY referenceset_id) r1
     *   INNER JOIN referenceset r2 ON ... AND r2.et = r1.max_etime
     *   WHERE r2.active = 1
     *
     * 개선된 DISTINCT ON 방식:
     *   FROM (SELECT DISTINCT ON (referenceset_id) * FROM referenceset
     *         WHERE refset_id IN (...) AND et <= VERSION_DATE
     *         ORDER BY referenceset_id, effective_time DESC) r
     *   WHERE r.active = 1
     *
     * idx_referenceset_r_id_rs_id_etime (refset_id, referenceset_id, effective_time) 사용.
     */
    private String buildInsertSql(String insertCols, String selectExprs, String refsetFilter) {
        return
            "INSERT INTO term.referenceset_active\n  (" + insertCols + ")\n" +
            "SELECT " + selectExprs + "\n" +
            "FROM (\n" +
            "  SELECT DISTINCT ON (referenceset_id)\n" +
            "    referenceset_id, effective_time, module_id, refset_id, active,\n" +
            "    referenced_component_id, field1, field2, field3, field4, field5, field6, field7\n" +
            "  FROM term.referenceset\n" +
            "  WHERE " + refsetFilter + "\n" +
            "    AND effective_time <= '" + VERSION_DATE + "'\n" +
            "  ORDER BY referenceset_id, effective_time DESC\n" +
            ") r\n" +
            "WHERE r.active = 1";
    }

    // 공통 INSERT 컬럼 접두부
    private static final String BASE_COLS =
        "version, uuid, effective_time, module_id, module_name, refset_id, refset_name, " +
        "referenced_component_id, referenced_component_active, referenced_component_name";

    // =========================================================================

    private void doLoad(Connection conn) throws Exception {
        long start = System.currentTimeMillis();
        log.info("REFERENCESET_ACTIVE 적재 시작...");

        try (Statement stmt = conn.createStatement()) {

            stmt.execute("TRUNCATE TABLE term.referenceset_active");
            conn.commit();

            // ----------------------------------------------------------------
            // Descriptor: FIELD1(ID),2(ID),3(group)  refset=900000000000456007
            // ----------------------------------------------------------------
            log.info("  Loading Descriptor ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value, field2_id, field2_value, field3_id, field3_value",
                BASE_EXPRS + ", r.field1, '', r.field2, '', '', r.field3",
                "refset_id = '900000000000456007'"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // Simple type refsets (추가 FIELD 없음)
            // ----------------------------------------------------------------
            log.info("  Loading Simple type refsets ...");
            stmt.execute(buildInsertSql(
                BASE_COLS,
                BASE_EXPRS,
                "refset_id IN (\n" +
                "    '723264001','450970008','1157358007','733990004','733991000',\n" +
                "    '787778008','816080008','1303957004','721144007','721145008'\n" +
                "  )"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=ID 그룹 1: ICD-O, MRCM module scope, Anatomy association
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(ID) - group 1 ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value",
                BASE_EXPRS + ", r.field1, ''",
                "refset_id IN ('446608001','723563008','734138000','734139008')"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=ID 그룹 2: Concept/Description inactivation indicator
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(ID) - group 2 (inactivation indicator) ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value",
                BASE_EXPRS + ", r.field1, ''",
                "refset_id IN ('900000000000489007','900000000000490003')"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=ID 그룹 3: CTV3 simple map
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(ID) - group 3 (CTV3) ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value",
                BASE_EXPRS + ", r.field1, ''",
                "refset_id = '900000000000497000'"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=ID 그룹 5: US/GB English language reference sets
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(ID) - group 5 (US/GB English) ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value",
                BASE_EXPRS + ", r.field1, ''",
                "refset_id IN ('900000000000509007','900000000000508004')"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=ID 그룹 6: POSSIBLY EQ, MOVED TO/FROM association
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(ID) - group 6 (Association) ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value",
                BASE_EXPRS + ", r.field1, ''",
                "refset_id IN ('900000000000523009','900000000000524003','900000000000525002')"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=ID 그룹 7~10: REPLACED BY, SAME AS, WAS A, ALTERNATIVE, REFERS TO
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(ID) - group 7~10 (Association continued) ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value",
                BASE_EXPRS + ", r.field1, ''",
                "refset_id IN (\n" +
                "    '900000000000526001','900000000000527005','900000000000528000',\n" +
                "    '900000000000530003','900000000000531004'\n" +
                "  )"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=ID 그룹 11: SNOMED CT to MedDRA / Orphanet simple map
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(ID) - group 11 (MedDRA/Orphanet) ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value",
                BASE_EXPRS + ", r.field1, ''",
                "refset_id IN ('816210007','784008009')"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=VALUE (ID 변환 불필요) 1: MedDRA to SNOMED CT map
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(VALUE) - group 1 (MedDRA→SNOMED) ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value",
                BASE_EXPRS + ", '', r.field1",
                "refset_id = '1193497006'"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=VALUE 2: OWL axiom / OWL ontology
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(VALUE) - group 2 (OWL) ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value",
                BASE_EXPRS + ", '', r.field1",
                "refset_id IN ('733073007','762103008')"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1,2 (ID 변환 불필요) 1: Description format
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1,2(no translate) - group 1 (Description format) ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value, field2_id, field2_value",
                BASE_EXPRS + ", r.field1, '', '', r.field2",
                "refset_id = '900000000000538005'"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1,2 (ID 변환 불필요) 2: Module dependency, EDQM
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1,2(no translate) - group 2 (Module dependency/EDQM) ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value, field2_id, field2_value",
                BASE_EXPRS + ", '', r.field1, '', r.field2",
                "refset_id IN ('900000000000534007','1237627005')"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1(x),2(x),3,4: MRCM attribute range
            // ----------------------------------------------------------------
            log.info("  Loading FIELD3,4 - MRCM attribute range ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value, field2_id, field2_value, field3_id, field3_value, field4_id, field4_value",
                BASE_EXPRS + ", '', r.field1, '', r.field2, r.field3, '', r.field4, ''",
                "refset_id = '723562003'"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1~4: LOINC Part map
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1~4 - LOINC Part map ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value, field2_id, field2_value, field3_id, field3_value, field4_id, field4_value",
                BASE_EXPRS + ", r.field1, '', r.field2, '', r.field3, '', r.field4, ''",
                "refset_id = '705112009'"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1~5: LOINC Term to Expression
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1~5 - LOINC Term to Expression ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value, field2_id, field2_value, field3_id, field3_value, field4_id, field4_value, field5_id, field5_value",
                BASE_EXPRS + ", r.field1, '', '', r.field2, r.field3, '', r.field4, '', r.field5, ''",
                "refset_id = '705110001'"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1~6: ICD-9-CM complex map
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1~6 - ICD-9-CM complex map ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value, field2_id, field2_value, field3_id, field3_value, field4_id, field4_value, field5_id, field5_value, field6_id, field6_value",
                BASE_EXPRS + ", '', r.field1, '', r.field2, '', r.field3, '', r.field4, '', r.field5, r.field6, ''",
                "refset_id = '447563008'"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1,5,6: MRCM attribute domain
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1,5,6 - MRCM attribute domain ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value, field2_id, field2_value, field3_id, field3_value, field4_id, field4_value, field5_id, field5_value, field6_id, field6_value",
                BASE_EXPRS + ", r.field1, '', '', r.field2, '', r.field3, '', r.field4, r.field5, '', r.field6, ''",
                "refset_id = '723561005'"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1~7: ICD-10 extended map, ICPC-2 complex map
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1~7 - ICD-10/ICPC-2 complex map ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value, field2_id, field2_value, field3_id, field3_value, field4_id, field4_value, field5_id, field5_value, field6_id, field6_value, field7_id, field7_value",
                BASE_EXPRS + ", '', r.field1, '', r.field2, '', r.field3, '', r.field4, r.field5, '', r.field6, '', r.field7, ''",
                "refset_id IN ('447562003','450993002')"
            ));
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1~7: MRCM domain (모두 VALUE)
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1~7 - MRCM domain ...");
            stmt.execute(buildInsertSql(
                BASE_COLS + ", field1_id, field1_value, field2_id, field2_value, field3_id, field3_value, field4_id, field4_value, field5_id, field5_value, field6_id, field6_value, field7_id, field7_value",
                BASE_EXPRS + ", '', r.field1, '', r.field2, '', r.field3, '', r.field4, '', r.field5, '', r.field6, '', r.field7",
                "refset_id = '723560006'"
            ));
            conn.commit();

            // ================================================================
            // UPDATE: NAME/VALUE 컬럼 후처리
            // ================================================================
        }
        runUpdates(conn);

        long elapsed = (System.currentTimeMillis() - start) / 1000;
        log.info("REFERENCESET_ACTIVE 적재 완료. 소요시간=" + elapsed + "초");
    }

    /** UPDATE 단계만 실행 (--update-only 재개 또는 doLoad에서 호출) */
    private void runUpdates(Connection conn) throws Exception {
        log.info("REFERENCESET_ACTIVE UPDATE 단계 시작...");
        try (Statement stmt = conn.createStatement()) {

            log.info("  Updating REFSET_NAME ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET refset_name = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.refset_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001' AND d.concept_id = ra2.refset_id AND d.active = 1\n" +
                ") AS r2\n" +
                "WHERE ra.refset_id = r2.refset_id"
            );
            conn.commit();

            log.info("  Updating MODULE_NAME ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET module_name = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.module_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001' AND d.concept_id = ra2.module_id AND d.active = 1\n" +
                ") AS r2\n" +
                "WHERE ra.module_id = r2.module_id"
            );
            conn.commit();

            log.info("  Updating REFERENCED_COMPONENT_NAME (concept) ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET referenced_component_name = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.referenced_component_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001' AND d.concept_id = ra2.referenced_component_id AND d.active = 1\n" +
                ") AS r2\n" +
                "WHERE ra.referenced_component_id = r2.referenced_component_id"
            );
            conn.commit();

            log.info("  Updating REFERENCED_COMPONENT_NAME (description) ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET referenced_component_name = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.referenced_component_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d ON d.description_id = ra2.referenced_component_id AND d.active = 1\n" +
                "  WHERE ra2.referenced_component_name = ''\n" +
                ") AS r2\n" +
                "WHERE ra.referenced_component_id = r2.referenced_component_id"
            );
            conn.commit();

            log.info("  Updating FIELD1_VALUE ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET field1_value = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.field1_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001' AND d.concept_id = ra2.field1_id AND d.active = 1\n" +
                "  WHERE ra2.refset_id IN (\n" +
                "    '705110001','705112009','723561005','723563008','734138000','734139008',\n" +
                "    '900000000000456007','900000000000489007','900000000000490003',\n" +
                "    '900000000000508004','900000000000509007','900000000000523009',\n" +
                "    '900000000000524003','900000000000525002','900000000000526001',\n" +
                "    '900000000000527005','900000000000528000','900000000000530003',\n" +
                "    '900000000000531004','900000000000538005','450993002','816210007','784008009'\n" +
                "  ) AND ra2.field1_id IS NOT NULL\n" +
                ") AS r2\n" +
                "WHERE ra.field1_id = r2.field1_id"
            );
            conn.commit();

            log.info("  Updating FIELD2_VALUE ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET field2_value = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.field2_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001' AND d.concept_id = ra2.field2_id AND d.active = 1\n" +
                "  WHERE ra2.refset_id IN ('705112009','900000000000456007') AND ra2.field2_id IS NOT NULL\n" +
                ") AS r2\n" +
                "WHERE ra.field2_id = r2.field2_id"
            );
            conn.commit();

            log.info("  Updating FIELD3_VALUE ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET field3_value = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.field3_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001' AND d.concept_id = ra2.field3_id AND d.active = 1\n" +
                "  WHERE ra2.refset_id IN ('705110001','705112009','723562003') AND ra2.field3_id IS NOT NULL\n" +
                ") AS r2\n" +
                "WHERE ra.field3_id = r2.field3_id"
            );
            conn.commit();

            log.info("  Updating FIELD4_VALUE ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET field4_value = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.field4_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001' AND d.concept_id = ra2.field4_id AND d.active = 1\n" +
                "  WHERE ra2.refset_id IN ('705110001','705112009','723562003') AND ra2.field4_id IS NOT NULL\n" +
                ") AS r2\n" +
                "WHERE ra.field4_id = r2.field4_id"
            );
            conn.commit();

            log.info("  Updating FIELD5_VALUE ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET field5_value = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.field5_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001' AND d.concept_id = ra2.field5_id AND d.active = 1\n" +
                "  WHERE ra2.refset_id IN ('705110001','723561005') AND ra2.field5_id IS NOT NULL\n" +
                ") AS r2\n" +
                "WHERE ra.field5_id = r2.field5_id"
            );
            conn.commit();

            log.info("  Updating FIELD6_VALUE ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET field6_value = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.field6_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001' AND d.concept_id = ra2.field6_id AND d.active = 1\n" +
                "  WHERE ra2.refset_id IN ('723561005','447563008','447562003','450993002') AND ra2.field6_id IS NOT NULL\n" +
                ") AS r2\n" +
                "WHERE ra.field6_id = r2.field6_id"
            );
            conn.commit();

            log.info("  Updating FIELD7_VALUE ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET field7_value = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.field7_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001' AND d.concept_id = ra2.field7_id AND d.active = 1\n" +
                "  WHERE ra2.refset_id IN ('447562003','450993002') AND ra2.field7_id IS NOT NULL\n" +
                ") AS r2\n" +
                "WHERE ra.field7_id = r2.field7_id"
            );
            conn.commit();
        }
        log.info("REFERENCESET_ACTIVE UPDATE 단계 완료.");
    }
}
