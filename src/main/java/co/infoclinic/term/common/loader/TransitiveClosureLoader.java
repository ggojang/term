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
 * 처리 흐름:
 *   PostgreSQL INFERRED_RELATIONSHIP (Full) → effectiveTime 기준 active IS-A 추출
 *   → 메모리 계층 계산 → TC 테이블 배치 INSERT (effectiveTime 태깅)
 *
 * TC 테이블 구조:
 *   각 행 = IS-A 관계의 직접 부모-자식 쌍 하나 (EFFECTIVE_TIME 컬럼으로 릴리즈 구분)
 *   PATH   : 루트(138875005)에서 PARENT_ID까지 '~' 구분 경로
 *   DEPTH  : 루트 기준 깊이 (루트=0, 루트 직계자녀=1)
 *
 * 릴리즈 추가 방식:
 *   - 기존 effectiveTime의 TC가 이미 있으면 DELETE 후 재적재
 *   - TRUNCATE 대신 effectiveTime 단위 DELETE → 멀티 릴리즈 공존 가능
 *
 * 메모리 사용:
 *   대용량 온톨로지(SNOMED CT 국제판 기준 ~350K 개념)에서
 *   codeDescendantCodesMap이 크게 증가할 수 있음.
 *   실행 시 -Xmx4g 이상 권장.
 *
 * SNOMED CT International Edition 릴리즈 주기:
 *   - 2002~2011 : 연 1회 (1월 31일)
 *   - 2012~현재 : 연 2회 (1월 31일, 7월 31일)
 *   실제 effectiveTime 목록은 INFERRED_RELATIONSHIP 테이블에서 조회 가능
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
    // 메모리 내 계층 구조
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
    private String effectiveTime;
    private long insertedRows = 0;

    // =========================================================================
    // 진입점
    // =========================================================================

    /**
     * 단독 실행 진입점.
     * args[0]: effectiveTime (예: 20241001). 생략 시 inferred_relationship의 최신 날짜 자동 사용.
     */
    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET search_path TO term");
            }
            String et = args.length > 0 ? args[0] : null;
            load(conn, et);
            conn.commit();
        }
        System.out.println("[INFO] TC 적재 완료.");
    }

    /**
     * 외부에서 호출하는 정적 진입점.
     *
     * @param conn          기존 DB 연결 (autoCommit=false 권장)
     * @param effectiveTime 릴리즈 기준 날짜 (null 이면 inferred_relationship 최신 날짜 자동 조회)
     */
    public static void load(Connection conn, String effectiveTime) throws Exception {
        new TransitiveClosureLoader().doLoad(conn, effectiveTime);
    }

    // =========================================================================
    // 내부 처리
    // =========================================================================

    private void doLoad(Connection conn, String effectiveTimeParam) throws Exception {
        this.conn = conn;
        long startTime = System.currentTimeMillis();

        // effectiveTime 결정
        if (effectiveTimeParam != null && !effectiveTimeParam.isEmpty()) {
            this.effectiveTime = effectiveTimeParam;
        } else {
            this.effectiveTime = resolveLatestEffectiveTime();
        }
        log.info("TC 생성 시작 (effectiveTime=" + this.effectiveTime + ")");

        // 1. INFERRED_RELATIONSHIP에서 해당 시점 기준 IS-A 관계 로딩
        loadIsaRelationships();
        int totalRels = codeChildCodesMap.values().stream().mapToInt(Set::size).sum();
        log.info("  IS-A 관계 로딩 완료: " + totalRels + "건, 개념 수=" + codeTermMap.size());

        Set<String> rootChildren = codeChildCodesMap.get(ROOT_SCTID);
        if (rootChildren == null || rootChildren.isEmpty()) {
            log.warning("루트(" + ROOT_SCTID + ")의 자식을 찾을 수 없음. TC 생성 생략.");
            return;
        }

        // 2. 동일 effectiveTime의 기존 TC 행 삭제 (TRUNCATE 대신 부분 삭제로 멀티 릴리즈 유지)
        try (Statement stmt = conn.createStatement()) {
            int deleted = stmt.executeUpdate(
                "DELETE FROM term.tc WHERE effective_time = '" + this.effectiveTime + "'");
            log.info("  기존 TC 삭제 완료 (effectiveTime=" + this.effectiveTime + ", " + deleted + "건)");
        }
        conn.commit();

        // 3. PreparedStatement 준비
        insertPs = conn.prepareStatement(
            "INSERT INTO term.tc " +
            "(concept_id, term, parent_id, children_count, descendant_count, depth, path, effective_time) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        );

        // 4. 루트에서 DFS 탐색 → TC 행 생성 및 배치 INSERT
        travelSubtypes();

        // 마지막 배치 플러시
        if (insertedRows % BATCH_SIZE != 0) {
            insertPs.executeBatch();
        }
        conn.commit();
        insertPs.close();

        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        log.info("TC 생성 완료 (effectiveTime=" + this.effectiveTime + "): "
                 + insertedRows + "건, 소요=" + elapsed + "초");
    }

    // =========================================================================
    // effectiveTime 자동 결정 (최신 릴리즈 날짜)
    // =========================================================================

    private String resolveLatestEffectiveTime() throws Exception {
        String sql = "SELECT MAX(effective_time) AS max_et FROM term.inferred_relationship";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String et = rs.getString("max_et");
                if (et != null && !et.isEmpty()) {
                    log.info("  effectiveTime 자동 결정: " + et);
                    return et;
                }
            }
        }
        throw new IllegalStateException("inferred_relationship에서 effectiveTime을 조회할 수 없음");
    }

    // =========================================================================
    // IS-A 관계 조회
    // =========================================================================

    /**
     * INFERRED_RELATIONSHIP에서 effectiveTime 기준 활성 IS-A 관계를 조회한다.
     *
     * DISTINCT ON (source_id, destination_id) + ORDER BY effective_time DESC
     * → effectiveTime <= 지정값 중 가장 최신 행을 선택하여 active=1 필터
     * → 해당 시점 기준 계층 상태 재현
     */
    private void loadIsaRelationships() throws Exception {
        String isaSql =
            "SELECT child_id, parent_id FROM (" +
            "  SELECT DISTINCT ON (source_id, destination_id)" +
            "    source_id AS child_id, destination_id AS parent_id, active" +
            "  FROM term.inferred_relationship" +
            "  WHERE type_id = '" + ISA_TYPE_ID + "'" +
            "    AND effective_time <= '" + this.effectiveTime + "'" +
            "  ORDER BY source_id, destination_id, effective_time DESC" +
            ") latest WHERE active = 1";

        String fsnSql =
            "SELECT concept_id, term FROM (" +
            "  SELECT DISTINCT ON (concept_id)" +
            "    concept_id, term, active" +
            "  FROM term.description" +
            "  WHERE type_id = '" + FSN_TYPE_ID + "'" +
            "    AND language_code = 'en'" +
            "    AND effective_time <= '" + this.effectiveTime + "'" +
            "  ORDER BY concept_id, effective_time DESC" +
            ") latest WHERE active = 1";

        log.info("  FSN 용어 로딩 중...");
        Map<String, String> fsnMap = new HashMap<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(fsnSql)) {
            while (rs.next()) {
                fsnMap.put(rs.getString("concept_id"), rs.getString("term"));
            }
        }
        log.info("  FSN 용어 로딩 완료: " + fsnMap.size() + "건");

        log.info("  IS-A 관계 쿼리 실행 중 (effectiveTime<=" + this.effectiveTime + ")...");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(isaSql)) {
            while (rs.next()) {
                String child  = rs.getString("child_id");
                String parent = rs.getString("parent_id");
                codeChildCodesMap.computeIfAbsent(parent, k -> new HashSet<>()).add(child);
                codeTermMap.put(child, fsnMap.getOrDefault(child, ""));
            }
        }
    }

    // =========================================================================
    // DFS 계층 탐색
    // =========================================================================

    private void travelSubtypes() throws Exception {
        Set<String> rootChildren = codeChildCodesMap.get(ROOT_SCTID);

        for (String code : rootChildren) {
            addSubtypeCode(ROOT_SCTID, code);

            Set<String> childCodes = codeChildCodesMap.get(code);
            int childrenCount = 0;
            if (childCodes != null && !childCodes.isEmpty()) {
                childrenCount = childCodes.size();
                travelSubtypes(code, ROOT_SCTID + "~" + code, childCodes, 1);
            }

            write(code, ROOT_SCTID, getTerm(code),
                  childrenCount, getDescendantCount(code), 1, ROOT_SCTID);
        }

        write(ROOT_SCTID, "",
              "SNOMED CT Concept (SNOMED RT+CTV3)",
              rootChildren.size(), getDescendantCount(ROOT_SCTID), 0, "");
    }

    private void travelSubtypes(String parentCode, String parentPath,
                                 Set<String> codes, int depth) throws Exception {
        for (String code : codes) {
            addSubtypeCode(parentPath, code);

            Set<String> childCodes = codeChildCodesMap.get(code);
            int childrenCount = 0;
            if (childCodes != null && !childCodes.isEmpty()) {
                childrenCount = childCodes.size();
                travelSubtypes(code, parentPath + "~" + code, childCodes, depth + 1);
            }

            write(code, parentCode, getTerm(code),
                  childrenCount, getDescendantCount(code), depth + 1, parentPath);
        }
    }

    private void addSubtypeCode(String parentPath, String code) {
        for (String ancestor : parentPath.split("~")) {
            codeDescendantCodesMap
                .computeIfAbsent(ancestor, k -> new HashSet<>())
                .add(code);
        }
    }

    private String getTerm(String code) {
        return codeTermMap.getOrDefault(code, "");
    }

    private int getDescendantCount(String code) {
        Set<String> descendants = codeDescendantCodesMap.get(code);
        return descendants == null ? 0 : descendants.size();
    }

    // =========================================================================
    // TC 테이블 INSERT
    // =========================================================================

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
        insertPs.setString(8, effectiveTime);  // EFFECTIVE_TIME

        insertPs.addBatch();
        insertedRows++;

        if (insertedRows % BATCH_SIZE == 0) {
            insertPs.executeBatch();
            conn.commit();
            log.log(Level.INFO, "  TC 적재 중: {0}건...", insertedRows);
        }
    }
}
