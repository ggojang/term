package co.infoclinic.term.common.loader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SNOMED CT IS-A 계층 Transitive Closure(TC) 테이블 적재기
 *
 * TransitiveClosureGeneratorFromInferred.sh + TransitiveClosureGenerator.java의 통합 Java 버전.
 * 중간 CSV 파일 없이 PostgreSQL INFERRED_RELATIONSHIP → 메모리 계산 → TC 테이블 직접 적재.
 *
 * 원본 처리 흐름:
 *   [Shell] MySQL → ISA CSV 파일 내보내기
 *   [Java]  ISA CSV → 메모리 맵 구축 → TC CSV 생성
 *   [Shell] TC CSV → MySQL TC 테이블 LOAD DATA
 *
 * 변환 후 처리 흐름:
 *   [Java]  PostgreSQL INFERRED_RELATIONSHIP → 메모리 맵 구축 → TC 테이블 배치 INSERT
 *
 * TC 테이블 의미:
 *   각 행 = IS-A 관계의 직접 부모-자식 쌍 하나
 *   다중 부모(multiple inheritance) 개념은 부모 수만큼 행이 존재
 *   PATH   : 루트(138875005)에서 PARENT_ID까지 '~' 구분 경로
 *   DEPTH  : 루트 기준 깊이 (루트=0, 루트 직계 자녀=1, ...)
 *
 * 메모리 사용:
 *   대용량 온톨로지(SNOMED CT 국제판 기준 ~350K 개념)에서
 *   codeDescendantCodesMap이 크게 증가할 수 있음.
 *   실행 시 -Xmx4g 이상 권장.
 */
public class TransitiveClosureLoader {

    private static final Logger log = Logger.getLogger(TransitiveClosureLoader.class.getName());

    private static final String JDBC_URL      = "jdbc:postgresql://localhost:5432/term";
    private static final String JDBC_USER     = "postgres";
    private static final String JDBC_PASSWORD = "julab123!";

    /** SNOMED CT 최상위 루트 개념 ID */
    private static final String ROOT_SCTID  = "138875005";

    /** IS-A 관계 TYPE_ID (116680003 = Is a) */
    private static final String ISA_TYPE_ID = "116680003";

    /** FSN(Fully Specified Name) TYPE_ID */
    private static final String FSN_TYPE_ID = "900000000000003001";

    /** 배치 INSERT 크기 */
    private static final int BATCH_SIZE = 5000;

    // -------------------------------------------------------------------------
    // 메모리 내 계층 구조 (TransitiveClosureGenerator.java 동일)
    // -------------------------------------------------------------------------

    /** 부모 → 직접 자식 집합 */
    private final Map<String, Set<String>> codeChildCodesMap = new HashMap<>();

    /** 개념 ID → FSN 용어 */
    private final Map<String, String> codeTermMap = new HashMap<>();

    /**
     * 개념 ID → 전이적 하위 개념(descendant) 집합
     * addSubtypeCode()가 경로 상 모든 조상에 자손을 추가하여 구축
     */
    private final Map<String, Set<String>> codeDescendantCodesMap = new HashMap<>();

    // -------------------------------------------------------------------------
    // TC 적재 상태
    // -------------------------------------------------------------------------
    private Connection conn;
    private PreparedStatement insertPs;
    private long insertedRows = 0;

    // =========================================================================
    // 진입점
    // =========================================================================

    /**
     * 단독 실행 진입점.
     * SnomedDataLoader에서 호출할 때는 load(Connection) 사용.
     */
    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET search_path TO term");
            }
            load(conn);
            conn.commit();
        }
        System.out.println("[INFO] TC 적재 완료.");
    }

    /**
     * SnomedDataLoader에서 호출하는 정적 진입점.
     * 기존 연결을 재사용하며, 커밋은 내부에서 수행.
     */
    public static void load(Connection conn) throws Exception {
        new TransitiveClosureLoader().doLoad(conn);
    }

    // =========================================================================
    // 내부 처리
    // =========================================================================

    private void doLoad(Connection conn) throws Exception {
        this.conn = conn;
        long startTime = System.currentTimeMillis();
        log.info("TC (Transitive Closure) 생성 시작...");

        // 1. INFERRED_RELATIONSHIP에서 IS-A 관계 로딩 (원본 MySQL SELECT ... INTO OUTFILE 대체)
        loadIsaRelationships();
        int totalRels = codeChildCodesMap.values().stream().mapToInt(Set::size).sum();
        log.info("  IS-A 관계 로딩 완료: " + totalRels + "건, 개념 수(FSN 포함)=" + codeTermMap.size());

        Set<String> rootChildren = codeChildCodesMap.get(ROOT_SCTID);
        if (rootChildren == null || rootChildren.isEmpty()) {
            log.warning("루트(" + ROOT_SCTID + ")의 자식을 찾을 수 없음. TC 생성 생략.");
            return;
        }

        // 2. TC 테이블 초기화
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE term.tc RESTART IDENTITY");
        }
        conn.commit();
        log.info("  TC 테이블 초기화 완료.");

        // 3. PreparedStatement 준비
        insertPs = conn.prepareStatement(
            "INSERT INTO term.tc " +
            "(concept_id, term, parent_id, children_count, descendant_count, depth, path) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)"
        );

        // 4. 루트에서 DFS 탐색 → TC 행 생성 및 배치 INSERT
        //    (원본 TransitiveClosureGenerator.compute() + travelSubtypes() 동일 알고리즘)
        travelSubtypes();

        // 마지막 배치 플러시
        if (insertedRows % BATCH_SIZE != 0) {
            insertPs.executeBatch();
        }
        conn.commit();
        insertPs.close();

        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        log.info("TC 생성 완료: " + insertedRows + "건 적재, 소요시간=" + elapsed + "초");
    }

    // =========================================================================
    // IS-A 관계 조회 (Shell script의 MySQL SELECT → INTO OUTFILE 대체)
    // =========================================================================

    /**
     * INFERRED_RELATIONSHIP에서 최신 활성 IS-A 관계를 조회하여 메모리 맵에 로드한다.
     *
     * 원본 MySQL 쿼리 변환:
     *   - 각 (source_id, destination_id) 쌍의 최신 EFFECTIVE_TIME 기준 active=1인 IS-A 관계 선택
     *   - source_id(자식 개념)의 FSN 용어 JOIN (최신 active FSN)
     *   - active 개념만 포함 (원본 CONCEPT.ACTIVE=1 조건 대응)
     *
     * 결과 → codeChildCodesMap(부모→자식집합), codeTermMap(개념→용어)
     */
    private void loadIsaRelationships() throws Exception {
        // ① 최신 활성 IS-A 관계를 DISTINCT ON으로 효율적으로 조회.
        //    DISTINCT ON (source_id, destination_id) + ORDER BY effective_time DESC
        //    → type_id 인덱스(idx_inferred_type_src_dst_et) 활용, 서브쿼리/GROUP BY 불필요.
        //    active=1 필터는 외부에서 처리(최신 effective_time 행의 active 상태 확인).
        String isaSql =
            "SELECT child_id, parent_id FROM (" +
            "  SELECT DISTINCT ON (source_id, destination_id)" +
            "    source_id AS child_id, destination_id AS parent_id, active" +
            "  FROM term.inferred_relationship" +
            "  WHERE type_id = '" + ISA_TYPE_ID + "'" +
            "  ORDER BY source_id, destination_id, effective_time DESC" +
            ") latest WHERE active = 1";

        // ② 각 개념의 최신 활성 FSN 용어를 DISTINCT ON으로 조회.
        //    idx_description_c_id_t_id_etime (concept_id, type_id, effective_time) 활용.
        String fsnSql =
            "SELECT concept_id, term FROM (" +
            "  SELECT DISTINCT ON (concept_id)" +
            "    concept_id, term, active" +
            "  FROM term.description" +
            "  WHERE type_id = '" + FSN_TYPE_ID + "'" +
            "    AND language_code = 'en'" +
            "  ORDER BY concept_id, effective_time DESC" +
            ") latest WHERE active = 1";

        // FSN 용어 맵 선 로딩 (description 테이블, ~350K 행 예상)
        log.info("  FSN 용어 로딩 중...");
        Map<String, String> fsnMap = new HashMap<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(fsnSql)) {
            while (rs.next()) {
                fsnMap.put(rs.getString("concept_id"), rs.getString("term"));
            }
        }
        log.info("  FSN 용어 로딩 완료: " + fsnMap.size() + "건");

        // IS-A 관계 로딩
        log.info("  IS-A 관계 쿼리 실행 중...");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(isaSql)) {
            while (rs.next()) {
                String child  = rs.getString("child_id");
                String parent = rs.getString("parent_id");

                codeChildCodesMap.computeIfAbsent(parent, k -> new HashSet<>()).add(child);
                // FSN 용어: 미리 로딩한 맵에서 참조
                codeTermMap.put(child, fsnMap.getOrDefault(child, ""));
            }
        }
    }

    // =========================================================================
    // DFS 계층 탐색 (TransitiveClosureGenerator.java 동일 알고리즘)
    // =========================================================================

    /**
     * 루트(138875005)에서 DFS 탐색을 시작하여 TC 행을 생성한다.
     * 원본 TransitiveClosureGenerator.travelSubtypes() 동일.
     */
    private void travelSubtypes() throws Exception {
        Set<String> rootChildren = codeChildCodesMap.get(ROOT_SCTID);

        for (String code : rootChildren) {
            // 루트의 자손에 code 추가
            addSubtypeCode(ROOT_SCTID, code);

            Set<String> childCodes = codeChildCodesMap.get(code);
            int childrenCount = 0;
            if (childCodes != null && !childCodes.isEmpty()) {
                childrenCount = childCodes.size();
                // DFS 재귀: code의 자식들 탐색
                travelSubtypes(code, ROOT_SCTID + "~" + code, childCodes, 1);
            }

            // code 행 기록: 부모=루트, 깊이=1, 경로=루트ID
            write(code, ROOT_SCTID, getTerm(code),
                  childrenCount, getDescendantCount(code), 1, ROOT_SCTID);
        }

        // 루트 자체 행 기록: 부모="", 깊이=0, 경로=""
        write(ROOT_SCTID, "",
              "SNOMED CT Concept (SNOMED RT+CTV3)",
              rootChildren.size(), getDescendantCount(ROOT_SCTID), 0, "");
    }

    /**
     * DFS 재귀 탐색.
     * 원본 TransitiveClosureGenerator.travelSubtypes(parentCode, parentPath, codes, depth) 동일.
     *
     * @param parentCode  현재 탐색 중인 부모 개념 ID
     * @param parentPath  루트에서 parentCode까지의 경로 (ID들을 '~'로 연결)
     * @param codes       parentCode의 직접 자식 집합
     * @param depth       현재 parentCode의 루트 기준 깊이
     */
    private void travelSubtypes(String parentCode, String parentPath,
                                 Set<String> codes, int depth) throws Exception {
        for (String code : codes) {
            // parentPath 상 모든 조상의 descendant 집합에 code 추가
            addSubtypeCode(parentPath, code);

            Set<String> childCodes = codeChildCodesMap.get(code);
            int childrenCount = 0;
            if (childCodes != null && !childCodes.isEmpty()) {
                childrenCount = childCodes.size();
                travelSubtypes(code, parentPath + "~" + code, childCodes, depth + 1);
            }

            // code 행 기록: depth+1은 code 자신의 깊이
            write(code, parentCode, getTerm(code),
                  childrenCount, getDescendantCount(code), depth + 1, parentPath);
        }
    }

    /**
     * parentPath 상의 모든 조상에 code를 자손(descendant)으로 추가한다.
     * codeDescendantCodesMap을 통해 descendant_count 산출에 사용.
     * 원본 TransitiveClosureGenerator.addSubtypeCode() 동일.
     */
    private void addSubtypeCode(String parentPath, String code) {
        for (String ancestor : parentPath.split("~")) {
            codeDescendantCodesMap
                .computeIfAbsent(ancestor, k -> new HashSet<>())
                .add(code);
        }
    }

    /** 개념의 FSN 용어 반환 (없으면 빈 문자열) */
    private String getTerm(String code) {
        return codeTermMap.getOrDefault(code, "");
    }

    /** 개념의 전이적 자손 수 반환 */
    private int getDescendantCount(String code) {
        Set<String> descendants = codeDescendantCodesMap.get(code);
        return descendants == null ? 0 : descendants.size();
    }

    // =========================================================================
    // TC 테이블 INSERT (Shell script의 LOAD DATA LOCAL INFILE 대체)
    // =========================================================================

    /**
     * TC 행을 PreparedStatement 배치로 INSERT한다.
     * 원본: LOAD DATA LOCAL INFILE '${TC_FILE}' INTO TABLE term.TC ...
     *   (CONCEPT_ID, PARENT_ID, TERM, CHILDREN_COUNT, DESCENDANT_COUNT, DEPTH, PATH)
     *
     * PostgreSQL INSERT 컬럼 순서가 원본 LOAD DATA의 컬럼 순서와 동일하게 매핑.
     */
    private void write(String code, String parentCode, String term,
                       int childrenCount, int descendantCount,
                       int depth, String path) throws Exception {
        insertPs.setString(1, code);           // CONCEPT_ID
        insertPs.setString(2, term);           // TERM
        insertPs.setString(3, parentCode);     // PARENT_ID
        insertPs.setInt(4, childrenCount);     // CHILDREN_COUNT
        insertPs.setInt(5, descendantCount);   // DESCENDANT_COUNT
        insertPs.setInt(6, depth);             // DEPTH
        insertPs.setString(7, path);           // PATH

        insertPs.addBatch();
        insertedRows++;

        if (insertedRows % BATCH_SIZE == 0) {
            insertPs.executeBatch();
            conn.commit();
            log.log(Level.INFO, "  TC 적재 중: {0}건...", insertedRows);
        }
    }
}
