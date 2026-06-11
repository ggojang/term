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
 *   - ORDER BY NULL 제거 (MySQL 최적화 힌트, PostgreSQL 불필요)
 *   - 다중 테이블 UPDATE:
 *       MySQL : UPDATE t1, (SELECT ...) t2 SET t1.c = t2.c WHERE ...
 *       PgSQL : UPDATE t1 SET c = t2.c FROM (SELECT ...) t2 WHERE ...
 *   - \! echo "..." (MySQL 클라이언트 명령) 제거
 *   - SET @@GLOBAL... (MySQL 전역 변수 설정) 제거
 *   - CONCAT() 함수 PostgreSQL에서도 동일하게 사용 가능
 *
 * 적재 순서 (원본 refset_active_loader.sql 순서 동일):
 *   1. 각 REFSET_ID 그룹별 INSERT (최신 active=1 행)
 *   2. REFSET_NAME, MODULE_NAME, REFERENCED_COMPONENT_NAME UPDATE
 *   3. FIELD1~7 VALUE UPDATE (DESCRIPTION FSN 참조)
 */
public class ReferenceSetActiveLoader {

    private static final Logger log = Logger.getLogger(ReferenceSetActiveLoader.class.getName());

    private static final String JDBC_URL      = "jdbc:postgresql://localhost:5432/term";
    private static final String JDBC_USER     = "postgres";
    private static final String JDBC_PASSWORD = "julab123!";

    /** 적재 기준 버전 (EFFECTIVE_TIME <= 이 값인 최신 행 선택) */
    private static final String VERSION_DATE  = "20260601";

    /** VERSION 컬럼 값 = 'INT-20260601' */
    private static final String VERSION_VALUE = "INT-" + VERSION_DATE;

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET search_path TO term");
            }
            load(conn);
            conn.commit();
        }
        System.out.println("[INFO] REFERENCESET_ACTIVE 적재 완료.");
    }

    /** SnomedDataLoader에서 호출하는 정적 진입점. */
    public static void load(Connection conn) throws Exception {
        new ReferenceSetActiveLoader().doLoad(conn);
    }

    private void doLoad(Connection conn) throws Exception {
        long start = System.currentTimeMillis();
        log.info("REFERENCESET_ACTIVE 적재 시작...");

        try (Statement stmt = conn.createStatement()) {

            // ----------------------------------------------------------------
            // 초기화
            // ----------------------------------------------------------------
            stmt.execute("TRUNCATE TABLE term.referenceset_active");
            conn.commit();

            // ================================================================
            // INSERT 블록 (refset_active_loader.sql 순서 동일)
            // 공통 패턴:
            //   R1: 각 REFERENCESET_ID 별 MAX(EFFECTIVE_TIME) 서브쿼리
            //   R2: R1에 JOIN하여 해당 시점의 실제 행 취득
            // ================================================================

            // ----------------------------------------------------------------
            // Descriptor: REFSET_ID=900000000000456007, FIELD1,2,3(group)
            // ----------------------------------------------------------------
            log.info("  Loading Descriptor ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value, field2_id, field2_value, field3_id, field3_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '',\n" +
                "  r2.field1, '', r2.field2, '', '', r2.field3\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id = '900000000000456007'\n" +  // Reference set descriptor
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // Simple type refsets (no additional FIELDs)
            // ----------------------------------------------------------------
            log.info("  Loading Simple type refsets ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, ''\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id IN (\n" +
                "    '723264001',  -- Lateralizable body structure reference set\n" +
                "    '450970008',  -- General Practice / Family Practice reference set\n" +
                "    '1157358007', -- ICNP Simple Type reference set\n" +
                "    '733990004',  -- Nursing Activities Simple Type reference set\n" +
                "    '733991000',  -- Nursing Health Issues Simple Type reference set\n" +
                "    '787778008',  -- Global Patient Set\n" +
                "    '816080008',  -- International Patient Summary\n" +
                "    '1303957004', -- NCPT simple type reference set\n" +
                "    '721144007',  -- General dentistry diagnostic reference set\n" +
                "    '721145008'   -- Odontogram reference set\n" +
                "  )\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=ID 변환 필요 그룹 1: ICD-O, MRCM module scope, Anatomy association
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(ID) refsets - group 1 ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '', r2.field1, ''\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id IN (\n" +
                "    '446608001',  -- ICD-O simple map reference set\n" +
                "    '723563008',  -- MRCM module scope reference set\n" +
                "    '734138000',  -- Anatomy structure and entire association reference set\n" +
                "    '734139008'   -- Anatomy structure and part association reference set\n" +
                "  )\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=ID 그룹 2: Concept/Description inactivation indicator
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(ID) refsets - group 2 ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '', r2.field1, ''\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id IN (\n" +
                "    '900000000000489007', -- Concept inactivation indicator reference set\n" +
                "    '900000000000490003'  -- Description inactivation indicator reference set\n" +
                "  )\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=ID 그룹 3: CTV3 simple map
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(ID) refsets - group 3 (CTV3) ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '', r2.field1, ''\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id = '900000000000497000' -- CTV3 simple map reference set\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=ID 그룹 5: US/GB English language reference sets
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(ID) refsets - group 5 (US/GB English) ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '', r2.field1, ''\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id IN (\n" +
                "    '900000000000509007', -- US English language reference set\n" +
                "    '900000000000508004'  -- GB English language reference set\n" +
                "  )\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=ID 그룹 6: Association reference sets (POSSIBLY EQ, MOVED TO/FROM)
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(ID) refsets - group 6 (Association) ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '', r2.field1, ''\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id IN (\n" +
                "    '900000000000523009', -- POSSIBLY EQUIVALENT TO association reference set\n" +
                "    '900000000000524003', -- MOVED TO association reference set\n" +
                "    '900000000000525002'  -- MOVED FROM association reference set\n" +
                "  )\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=ID 그룹 7~10: REPLACED BY, SAME AS, WAS A, ALTERNATIVE, REFERS TO
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(ID) refsets - group 7~10 (Association continued) ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '', r2.field1, ''\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id IN (\n" +
                "    '900000000000526001', -- REPLACED BY association reference set\n" +
                "    '900000000000527005', -- SAME AS association reference set\n" +
                "    '900000000000528000', -- WAS A association reference set\n" +
                "    '900000000000530003', -- ALTERNATIVE association reference set\n" +
                "    '900000000000531004'  -- REFERS TO concept association reference set\n" +
                "  )\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=ID 그룹 11: SNOMED CT to MedDRA / Orphanet simple map
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(ID) refsets - group 11 (MedDRA/Orphanet map) ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '', r2.field1, ''\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id IN (\n" +
                "    '816210007', -- SNOMED CT to MedDRA simple map reference set\n" +
                "    '784008009'  -- SNOMED CT to Orphanet simple map reference set\n" +
                "  )\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=VALUE (ID 변환 불필요) 1: MedDRA to SNOMED CT map
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(VALUE, no translate) - group 1 (MedDRA→SNOMED) ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '', '', r2.field1\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id = '1193497006' -- MedDRA to SNOMED CT simple map reference set\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1=VALUE 2: OWL axiom / OWL ontology reference sets
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1(VALUE, no translate) - group 2 (OWL) ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '', '', r2.field1\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id IN (\n" +
                "    '733073007', -- OWL axiom reference set\n" +
                "    '762103008'  -- OWL ontology reference set\n" +
                "  )\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1,2 (ID 변환 불필요) 1: Description format reference set
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1,2(no translate) - group 1 (Description format) ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value, field2_id, field2_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '',\n" +
                "  r2.field1, '', '', r2.field2\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id = '900000000000538005' -- Description format reference set\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1,2 (ID 변환 불필요) 2: Module dependency, EDQM
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1,2(no translate) - group 2 (Module dependency/EDQM) ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value, field2_id, field2_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '',\n" +
                "  '', r2.field1, '', r2.field2\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id IN (\n" +
                "    '900000000000534007', -- Module dependency reference set\n" +
                "    '1237627005'          -- EDQM reference set\n" +
                "  )\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1(x),2(x),3(o),4(o): MRCM attribute range
            // ----------------------------------------------------------------
            log.info("  Loading FIELD3,4 - MRCM attribute range ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value, field2_id, field2_value, field3_id, field3_value, field4_id, field4_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '',\n" +
                "  '', r2.field1, '', r2.field2, r2.field3, '', r2.field4, ''\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id = '723562003' -- MRCM attribute range international reference set\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1(o-LP),2,3,4: LOINC Part map reference set
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1~4 - LOINC Part map ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value, field2_id, field2_value, field3_id, field3_value, field4_id, field4_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '',\n" +
                "  r2.field1, '', r2.field2, '', r2.field3, '', r2.field4, ''\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id = '705112009' -- LOINC Part map reference set\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1(LOINC Code),2(Parsable String),3,4,5: LOINC Term to Expression
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1~5 - LOINC Term to Expression ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value, field2_id, field2_value, field3_id, field3_value,\n" +
                "   field4_id, field4_value, field5_id, field5_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '',\n" +
                "  r2.field1, '', '', r2.field2, r2.field3, '', r2.field4, '', r2.field5, ''\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id = '705110001' -- LOINC Term to Expression reference set\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1~6: ICD-9-CM complex map (6 fields)
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1~6 - ICD-9-CM complex map ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value, field2_id, field2_value, field3_id, field3_value,\n" +
                "   field4_id, field4_value, field5_id, field5_value, field6_id, field6_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '',\n" +
                "  '', r2.field1, '', r2.field2, '', r2.field3, '', r2.field4, '', r2.field5, r2.field6, ''\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id = '447563008' -- SNOMED CT to ICD-9-CM equivalence complex map\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1(o),2~4(x),5,6: MRCM attribute domain
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1,5,6 - MRCM attribute domain ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value, field2_id, field2_value, field3_id, field3_value,\n" +
                "   field4_id, field4_value, field5_id, field5_value, field6_id, field6_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '',\n" +
                "  r2.field1, '', '', r2.field2, '', r2.field3, '', r2.field4, r2.field5, '', r2.field6, ''\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id = '723561005' -- MRCM attribute domain international reference set\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1~7: ICD-10 extended map, ICPC-2 complex map (7 fields)
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1~7 - ICD-10/ICPC-2 complex map ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value, field2_id, field2_value, field3_id, field3_value,\n" +
                "   field4_id, field4_value, field5_id, field5_value, field6_id, field6_value,\n" +
                "   field7_id, field7_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '',\n" +
                "  '', r2.field1, '', r2.field2, '', r2.field3, '', r2.field4, r2.field5, '', r2.field6, '', r2.field7, ''\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id IN (\n" +
                "    '447562003', -- SNOMED CT to ICD-10 extended map reference set\n" +
                "    '450993002'  -- SNOMED CT to ICPC-2 complex map reference set\n" +
                "  )\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1~7: MRCM domain (7 fields, 모두 VALUE)
            // ----------------------------------------------------------------
            log.info("  Loading FIELD1~7 - MRCM domain ...");
            stmt.execute(
                "INSERT INTO term.referenceset_active\n" +
                "  (version, uuid, effective_time, module_id, module_name, refset_id, refset_name,\n" +
                "   referenced_component_id, referenced_component_active, referenced_component_name,\n" +
                "   field1_id, field1_value, field2_id, field2_value, field3_id, field3_value,\n" +
                "   field4_id, field4_value, field5_id, field5_value, field6_id, field6_value,\n" +
                "   field7_id, field7_value)\n" +
                "SELECT\n" +
                "  '" + VERSION_VALUE + "', r1.referenceset_id, r2.effective_time, r2.module_id, '',\n" +
                "  r2.refset_id, '', r2.referenced_component_id, r2.active, '',\n" +
                "  '', r2.field1, '', r2.field2, '', r2.field3, '', r2.field4, '', r2.field5, '', r2.field6, '', r2.field7\n" +
                "FROM (\n" +
                "  SELECT refset_id, referenceset_id, MAX(effective_time) AS max_etime\n" +
                "  FROM term.referenceset\n" +
                "  WHERE refset_id = '723560006' -- MRCM domain international reference set\n" +
                "    AND effective_time <= '" + VERSION_DATE + "'\n" +
                "  GROUP BY refset_id, referenceset_id\n" +
                ") AS r1\n" +
                "INNER JOIN term.referenceset AS r2\n" +
                "  ON r2.refset_id = r1.refset_id\n" +
                " AND r2.referenceset_id = r1.referenceset_id\n" +
                " AND r2.effective_time = r1.max_etime\n" +
                "WHERE r2.active = 1"
            );
            conn.commit();

            // ================================================================
            // UPDATE 블록: NAME/VALUE 컬럼 후처리
            // MySQL 다중 테이블 UPDATE → PostgreSQL UPDATE ... FROM 구문으로 변환
            // ================================================================

            // ----------------------------------------------------------------
            // REFSET_NAME: REFSET_ID → FSN
            // ----------------------------------------------------------------
            log.info("  Updating REFSET_NAME ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET refset_name = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.refset_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001'\n" +
                "   AND d.concept_id = ra2.refset_id\n" +
                "   AND d.active = 1\n" +
                ") AS r2\n" +
                "WHERE ra.refset_id = r2.refset_id"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // MODULE_NAME: MODULE_ID → FSN
            // ----------------------------------------------------------------
            log.info("  Updating MODULE_NAME ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET module_name = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.module_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001'\n" +
                "   AND d.concept_id = ra2.module_id\n" +
                "   AND d.active = 1\n" +
                ") AS r2\n" +
                "WHERE ra.module_id = r2.module_id"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // REFERENCED_COMPONENT_NAME (CONCEPT_ID 기준)
            // ----------------------------------------------------------------
            log.info("  Updating REFERENCED_COMPONENT_NAME (concept) ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET referenced_component_name = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.referenced_component_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001'\n" +
                "   AND d.concept_id = ra2.referenced_component_id\n" +
                "   AND d.active = 1\n" +
                ") AS r2\n" +
                "WHERE ra.referenced_component_id = r2.referenced_component_id"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // REFERENCED_COMPONENT_NAME (DESCRIPTION_ID 기준: US/GB 언어 refset 등)
            // ----------------------------------------------------------------
            log.info("  Updating REFERENCED_COMPONENT_NAME (description) ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET referenced_component_name = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.referenced_component_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.description_id = ra2.referenced_component_id\n" +
                "   AND d.active = 1\n" +
                "  WHERE ra2.referenced_component_name = ''\n" +
                ") AS r2\n" +
                "WHERE ra.referenced_component_id = r2.referenced_component_id"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD1_VALUE: FIELD1_ID → FSN (ID가 CONCEPT_ID인 refset만)
            // ----------------------------------------------------------------
            log.info("  Updating FIELD1_VALUE ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET field1_value = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.field1_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001'\n" +
                "   AND d.concept_id = ra2.field1_id\n" +
                "   AND d.active = 1\n" +
                "  WHERE ra2.refset_id IN (\n" +
                "    '705110001',          -- LOINC Term to Expression\n" +
                "    '705112009',          -- LOINC Part map\n" +
                "    '723561005',          -- MRCM attribute domain\n" +
                "    '723563008',          -- MRCM module scope\n" +
                "    '734138000',\n" +
                "    '734139008',\n" +
                "    '900000000000456007', -- Reference set descriptor\n" +
                "    '900000000000489007', -- Concept inactivation indicator\n" +
                "    '900000000000490003', -- Description inactivation indicator\n" +
                "    '900000000000508004', -- GB English\n" +
                "    '900000000000509007', -- US English\n" +
                "    '900000000000523009',\n" +
                "    '900000000000524003',\n" +
                "    '900000000000525002',\n" +
                "    '900000000000526001',\n" +
                "    '900000000000527005',\n" +
                "    '900000000000528000',\n" +
                "    '900000000000530003',\n" +
                "    '900000000000531004',\n" +
                "    '900000000000538005', -- Description format\n" +
                "    '450993002',          -- ICPC-2 complex map\n" +
                "    '816210007',          -- SNOMED CT to MedDRA\n" +
                "    '784008009'           -- SNOMED CT to Orphanet\n" +
                "  )\n" +
                "    AND ra2.field1_id IS NOT NULL\n" +
                ") AS r2\n" +
                "WHERE ra.field1_id = r2.field1_id"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD2_VALUE: FIELD2_ID → FSN
            // ----------------------------------------------------------------
            log.info("  Updating FIELD2_VALUE ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET field2_value = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.field2_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001'\n" +
                "   AND d.concept_id = ra2.field2_id\n" +
                "   AND d.active = 1\n" +
                "  WHERE ra2.refset_id IN (\n" +
                "    '705112009',          -- LOINC Part map\n" +
                "    '900000000000456007'  -- Reference set descriptor\n" +
                "  )\n" +
                "    AND ra2.field2_id IS NOT NULL\n" +
                ") AS r2\n" +
                "WHERE ra.field2_id = r2.field2_id"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD3_VALUE: FIELD3_ID → FSN
            // ----------------------------------------------------------------
            log.info("  Updating FIELD3_VALUE ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET field3_value = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.field3_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001'\n" +
                "   AND d.concept_id = ra2.field3_id\n" +
                "   AND d.active = 1\n" +
                "  WHERE ra2.refset_id IN (\n" +
                "    '705110001', -- LOINC Term to Expression\n" +
                "    '705112009', -- LOINC Part map\n" +
                "    '723562003'  -- MRCM attribute range\n" +
                "  )\n" +
                "    AND ra2.field3_id IS NOT NULL\n" +
                ") AS r2\n" +
                "WHERE ra.field3_id = r2.field3_id"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD4_VALUE: FIELD4_ID → FSN
            // ----------------------------------------------------------------
            log.info("  Updating FIELD4_VALUE ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET field4_value = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.field4_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001'\n" +
                "   AND d.concept_id = ra2.field4_id\n" +
                "   AND d.active = 1\n" +
                "  WHERE ra2.refset_id IN (\n" +
                "    '705110001', -- LOINC Term to Expression\n" +
                "    '705112009', -- LOINC Part map\n" +
                "    '723562003'  -- MRCM attribute range\n" +
                "  )\n" +
                "    AND ra2.field4_id IS NOT NULL\n" +
                ") AS r2\n" +
                "WHERE ra.field4_id = r2.field4_id"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD5_VALUE: FIELD5_ID → FSN
            // ----------------------------------------------------------------
            log.info("  Updating FIELD5_VALUE ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET field5_value = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.field5_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001'\n" +
                "   AND d.concept_id = ra2.field5_id\n" +
                "   AND d.active = 1\n" +
                "  WHERE ra2.refset_id IN (\n" +
                "    '705110001', -- LOINC Term to Expression\n" +
                "    '723561005'  -- MRCM attribute domain\n" +
                "  )\n" +
                "    AND ra2.field5_id IS NOT NULL\n" +
                ") AS r2\n" +
                "WHERE ra.field5_id = r2.field5_id"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD6_VALUE: FIELD6_ID → FSN
            // ----------------------------------------------------------------
            log.info("  Updating FIELD6_VALUE ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET field6_value = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.field6_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001'\n" +
                "   AND d.concept_id = ra2.field6_id\n" +
                "   AND d.active = 1\n" +
                "  WHERE ra2.refset_id IN (\n" +
                "    '723561005', -- MRCM attribute domain\n" +
                "    '447563008', -- SNOMED CT to ICD-9-CM\n" +
                "    '447562003', -- SNOMED CT to ICD-10\n" +
                "    '450993002'  -- SNOMED CT to ICPC-2\n" +
                "  )\n" +
                "    AND ra2.field6_id IS NOT NULL\n" +
                ") AS r2\n" +
                "WHERE ra.field6_id = r2.field6_id"
            );
            conn.commit();

            // ----------------------------------------------------------------
            // FIELD7_VALUE: FIELD7_ID → FSN
            // ----------------------------------------------------------------
            log.info("  Updating FIELD7_VALUE ...");
            stmt.execute(
                "UPDATE term.referenceset_active AS ra\n" +
                "SET field7_value = r2.term\n" +
                "FROM (\n" +
                "  SELECT DISTINCT ra2.field7_id, d.term\n" +
                "  FROM term.referenceset_active ra2\n" +
                "  INNER JOIN term.description d\n" +
                "    ON d.type_id = '900000000000003001'\n" +
                "   AND d.concept_id = ra2.field7_id\n" +
                "   AND d.active = 1\n" +
                "  WHERE ra2.refset_id IN (\n" +
                "    '447562003', -- SNOMED CT to ICD-10\n" +
                "    '450993002'  -- SNOMED CT to ICPC-2\n" +
                "  )\n" +
                "    AND ra2.field7_id IS NOT NULL\n" +
                ") AS r2\n" +
                "WHERE ra.field7_id = r2.field7_id"
            );
            conn.commit();
        }

        long elapsed = (System.currentTimeMillis() - start) / 1000;
        log.info("REFERENCESET_ACTIVE 적재 완료. 소요시간=" + elapsed + "초");
    }
}
