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
 * SEARCH_INDEX 테이블 적재기 (Elasticsearch 대체)
 *
 * 외부 Elasticsearch v2.x + Logstash + startup.sh 구성을 PostgreSQL 내장으로 대체.
 * pg_trgm GIN 인덱스로 자동완성·유사도·전문 검색을 지원한다.
 *
 * 원본 처리 흐름 (startup.sh):
 *   [Shell] MySQL → 검색 데이터 CSV 내보내기
 *   [Java]  MrcmAppender: IS-A 계층 DFS + MRCM attr 매핑 → 결합 CSV
 *   [Shell] Logstash → Elasticsearch 인덱스 적재
 *
 * 변환 후 처리 흐름:
 *   [Java]  PostgreSQL 테이블 직접 조회 → 메모리 맵 구축 → SEARCH_INDEX 배치 INSERT
 *
 * ES 매핑 필드 대응:
 *   term        → TERM  (GIN trgm: LIKE/similarity, GIN fts: @@)
 *   semanticTag → SEMANTIC_TAG
 *   fsn         → FSN
 *   mrcmAttrs   → MRCM_ATTRS ('+' 구분 attribute_id 목록, MrcmAppender 로직 동일)
 *
 * 검색 쿼리 예시 (프론트엔드):
 *   -- 자동완성 (prefix)
 *   SELECT * FROM term.search_index
 *   WHERE term ILIKE 'diab%' AND component_active = 1
 *   ORDER BY term_length LIMIT 20;
 *
 *   -- 전문 검색 (FTS)
 *   SELECT * FROM term.search_index
 *   WHERE to_tsvector('english', term) @@ plainto_tsquery('english', 'diabetes mellitus')
 *   AND component_active = 1;
 *
 *   -- 유사도 검색 (오타 허용)
 *   SELECT *, similarity(term, 'diabetis') AS score FROM term.search_index
 *   WHERE similarity(term, 'diabetis') > 0.3 AND component_active = 1
 *   ORDER BY score DESC LIMIT 20;
 */
public class SearchIndexLoader {

    private static final Logger log = Logger.getLogger(SearchIndexLoader.class.getName());

    private static final String JDBC_URL      = "jdbc:postgresql://localhost:5432/term";
    private static final String JDBC_USER     = "postgres";
    private static final String JDBC_PASSWORD = "julab123!";

    private static final String ROOT_SCTID    = "138875005";
    private static final String ISA_TYPE_ID   = "116680003";
    private static final String FSN_TYPE_ID   = "900000000000003001";
    private static final String SYN_TYPE_ID   = "900000000000013009";
    private static final String DEF_TYPE_ID   = "900000000000550004";
    private static final String PRIMITIVE_ID  = "900000000000074008";
    private static final String US_REFSET_ID  = "900000000000509007";
    /** SNOMED CT International Edition 모듈 ID */
    private static final String INTL_MODULE_ID = "900000000000207008";

    private static final int BATCH_SIZE = 5000;

    // ── 메모리 맵 ──────────────────────────────────────────────────────────────

    /** concept_id → [effective_time, active, definition_status_id] */
    private final Map<String, String[]> conceptMap      = new HashMap<>();

    /** concept_id → FSN term */
    private final Map<String, String>   fsnMap          = new HashMap<>();

    /** International 모듈 FSN만: concept_id → [fsn_term, effective_time]
     *  semantic tag 집계에만 사용 (KCD-9 등 extension 오염 방지) */
    private final Map<String, String[]> intlFsnMap      = new HashMap<>();

    /** description_id → [concept_id, effective_time, active, type_id, term, lang] */
    private final Map<String, String[]> descMap         = new HashMap<>();

    /** description_id → acceptability FIELD1 (US English refset) */
    private final Map<String, String>   acceptMap       = new HashMap<>();

    /** value_id → Set<attribute_id>  (MRCM range 기준, MrcmAppender.rangeAttrMap 동일) */
    private final Map<String, Set<String>> rangeAttrMap = new HashMap<>();

    /** parent_id → Set<child_id> (IS-A 관계) */
    private final Map<String, Set<String>> childrenMap  = new HashMap<>();

    /** concept_id → mrcm_attrs 문자열 ("+attrId1+attrId2...") */
    private final Map<String, String>   mrcmAttrsMap    = new HashMap<>();

    // ── 상태 ──────────────────────────────────────────────────────────────────
    private Connection        conn;
    private PreparedStatement insertPs;
    private long              insertedRows = 0;

    // =========================================================================
    // 진입점
    // =========================================================================

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);
            try (Statement s = conn.createStatement()) {
                s.execute("SET search_path TO term");
            }
            load(conn);
            conn.commit();
        }
        System.out.println("[INFO] SEARCH_INDEX 적재 완료.");
    }

    public static void load(Connection conn) throws Exception {
        new SearchIndexLoader().doLoad(conn);
    }

    // =========================================================================
    // 메인 처리
    // =========================================================================

    private void doLoad(Connection conn) throws Exception {
        this.conn = conn;
        long start = System.currentTimeMillis();
        log.info("SEARCH_INDEX 생성 시작...");

        // 1. 각종 맵 로딩
        loadConceptMap();
        loadDescriptionMap();   // FSN 맵도 함께 구축
        loadAcceptabilityMap();
        loadMrcmAttrMap();
        loadIsaMap();

        // 2. MRCM attrs DFS 계산 (MrcmAppender.compute() 동일 알고리즘)
        computeMrcmAttrs();

        // 3. SEARCH_INDEX 초기화
        try (Statement s = conn.createStatement()) {
            s.execute("TRUNCATE TABLE term.search_index RESTART IDENTITY");
        }
        conn.commit();

        // 4. 배치 INSERT
        insertPs = conn.prepareStatement(
            "INSERT INTO term.search_index " +
            "(concept_id, description_id, component_effective_time, concept_effective_time, " +
            " description_effective_time, component_active, concept_active, description_active, " +
            " term, semantic_tag, fsn, primitive, type, acceptability_id, term_length, lang, mrcm_attrs) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
        );

        buildIndex();

        if (insertedRows % BATCH_SIZE != 0) {
            insertPs.executeBatch();
        }
        conn.commit();
        insertPs.close();

        // 5. International FSN에서 semantic tag 집계 → snomed_semantic_tag 테이블 갱신
        saveSemanticTags();
        conn.commit();

        long elapsed = (System.currentTimeMillis() - start) / 1000;
        log.info("SEARCH_INDEX 생성 완료: " + insertedRows + "건, 소요시간=" + elapsed + "초");
    }

    // =========================================================================
    // 1. 개념 최신 상태 로딩
    // =========================================================================

    private void loadConceptMap() throws Exception {
        log.info("  개념 맵 로딩 중...");
        String sql =
            "SELECT DISTINCT ON (concept_id) concept_id, effective_time, active, definition_status_id " +
            "FROM term.concept ORDER BY concept_id, effective_time DESC";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                conceptMap.put(rs.getString(1), new String[]{
                    rs.getString(2),  // effective_time
                    rs.getString(3),  // active
                    rs.getString(4)   // definition_status_id
                });
            }
        }
        log.info("  개념 맵: " + conceptMap.size() + "건");
    }

    // =========================================================================
    // 2. 설명(Description) 최신 상태 로딩 + FSN 맵 구축
    // =========================================================================

    private void loadDescriptionMap() throws Exception {
        log.info("  설명 맵 로딩 중...");
        // 영문 description 중 최신 상태 행만 (DISTINCT ON)
        String sql =
            "SELECT DISTINCT ON (description_id) " +
            "  description_id, concept_id, effective_time, active, type_id, term, language_code, module_id " +
            "FROM term.description " +
            "WHERE language_code = 'en' " +
            "ORDER BY description_id, effective_time DESC";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                String descId    = rs.getString(1);
                String conceptId = rs.getString(2);
                String et        = rs.getString(3);
                String active    = rs.getString(4);
                String typeId    = rs.getString(5);
                String term      = rs.getString(6);
                String lang      = rs.getString(7);
                String moduleId  = rs.getString(8);

                descMap.put(descId, new String[]{conceptId, et, active, typeId, term, lang});

                // FSN 맵: 활성 FSN만
                if (FSN_TYPE_ID.equals(typeId) && "1".equals(active)) {
                    fsnMap.put(conceptId, term);
                    // International 모듈 FSN만 별도 추적 (semantic tag 집계용)
                    if (INTL_MODULE_ID.equals(moduleId)) {
                        intlFsnMap.put(conceptId, new String[]{term, et});
                    }
                }
            }
        }
        log.info("  설명 맵: " + descMap.size() + "건, FSN: " + fsnMap.size() + "건, International FSN: " + intlFsnMap.size() + "건");
    }

    // =========================================================================
    // 3. US English acceptability 로딩
    // =========================================================================

    private void loadAcceptabilityMap() throws Exception {
        log.info("  Acceptability 맵 로딩 중...");
        // US English language refset: 최신 활성 행
        String sql =
            "SELECT DISTINCT ON (referenced_component_id) referenced_component_id, field1 " +
            "FROM term.referenceset " +
            "WHERE refset_id = '" + US_REFSET_ID + "' AND active = 1 " +
            "ORDER BY referenced_component_id, effective_time DESC";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                acceptMap.put(rs.getString(1), rs.getString(2));
            }
        }
        log.info("  Acceptability 맵: " + acceptMap.size() + "건");
    }

    // =========================================================================
    // 4. MRCM attr 맵 로딩 (MrcmAppender.makeRangeAttrMap() 동일)
    //    MRCM_CONSTRAINTS: ATTRIBUTE_ID, VALUE_ID
    //    → rangeAttrMap[value_id] = Set<attribute_id>
    // =========================================================================

    private void loadMrcmAttrMap() throws Exception {
        log.info("  MRCM attr 맵 로딩 중...");
        String sql = "SELECT DISTINCT attribute_id, value_id FROM term.mrcm_constraints WHERE value_id IS NOT NULL";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                String attrId = rs.getString(1);
                String valId  = rs.getString(2);
                rangeAttrMap.computeIfAbsent(valId, k -> new HashSet<>()).add(attrId);
            }
        }
        log.info("  MRCM attr 맵: " + rangeAttrMap.size() + " value 항목");
    }

    // =========================================================================
    // 5. IS-A 계층 로딩
    // =========================================================================

    private void loadIsaMap() throws Exception {
        log.info("  IS-A 계층 로딩 중...");
        String sql =
            "SELECT child_id, parent_id FROM (" +
            "  SELECT DISTINCT ON (source_id, destination_id) " +
            "    source_id AS child_id, destination_id AS parent_id, active " +
            "  FROM term.inferred_relationship WHERE type_id = '" + ISA_TYPE_ID + "' " +
            "  ORDER BY source_id, destination_id, effective_time DESC" +
            ") r WHERE active = 1";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                String child  = rs.getString(1);
                String parent = rs.getString(2);
                childrenMap.computeIfAbsent(parent, k -> new HashSet<>()).add(child);
            }
        }
        int total = childrenMap.values().stream().mapToInt(Set::size).sum();
        log.info("  IS-A 관계: " + total + "건");
    }

    // =========================================================================
    // 6. MRCM attrs DFS 계산 (MrcmAppender.compute() 동일 알고리즘)
    //    루트에서 DFS: 각 개념이 MRCM value로 등록된 attribute들을
    //    하위 개념까지 전파하여 mrcm_attrs 문자열 생성
    // =========================================================================

    private void computeMrcmAttrs() {
        log.info("  MRCM attrs DFS 계산 중...");
        Set<String> rootChildren = childrenMap.get(ROOT_SCTID);
        if (rootChildren == null) return;

        for (String code : rootChildren) {
            Set<String> ruleSet = appendRule(code, new HashSet<>());
            saveRule(code, ruleSet);
            Set<String> children = childrenMap.get(code);
            if (children != null) {
                travelSubtypes(children, ruleSet);
            }
        }

        // Set<String> → "+attr1+attr2..." 문자열로 변환하여 mrcmAttrsMap에 저장
        Map<String, Set<String>> conceptRuleMap = new HashMap<>();
        // 임시로 conceptRuleMap을 인스턴스 필드 대신 로컬로 관리했으므로 재구성
        // (saveRule이 mrcmAttrsMap에 직접 쓰도록 구현)
        log.info("  MRCM attrs 계산 완료: " + mrcmAttrsMap.size() + " 개념");
    }

    /** 현재 개념에 rangeAttrMap 기반 attribute 추가 후 반환 */
    private Set<String> appendRule(String id, Set<String> inherited) {
        Set<String> result = new HashSet<>(inherited);
        Set<String> attrs = rangeAttrMap.get(id);
        if (attrs != null) result.addAll(attrs);
        return result;
    }

    /** ruleSet이 있으면 mrcmAttrsMap에 "+attr1+attr2" 형태로 저장 */
    private void saveRule(String id, Set<String> ruleSet) {
        if (ruleSet.isEmpty()) return;
        String existing = mrcmAttrsMap.get(id);
        if (existing == null) {
            StringBuilder sb = new StringBuilder();
            for (String attr : ruleSet) sb.append('+').append(attr);
            mrcmAttrsMap.put(id, sb.toString());
        } else {
            // 기존 attrs에 추가
            Set<String> merged = new HashSet<>(ruleSet);
            for (String part : existing.split("\\+")) {
                if (!part.isEmpty()) merged.add(part);
            }
            StringBuilder sb = new StringBuilder();
            for (String attr : merged) sb.append('+').append(attr);
            mrcmAttrsMap.put(id, sb.toString());
        }
    }

    /** DFS 재귀 탐색 (MrcmAppender.travelSubtypes() 동일) */
    private void travelSubtypes(Set<String> codes, Set<String> parentRuleSet) {
        for (String code : codes) {
            Set<String> ruleSet = appendRule(code, parentRuleSet);
            saveRule(code, ruleSet);
            Set<String> children = childrenMap.get(code);
            if (children != null) {
                travelSubtypes(children, ruleSet);
            }
        }
    }

    // =========================================================================
    // 7. SEARCH_INDEX 행 생성 및 배치 INSERT
    //    US English acceptability가 있는 영문 description 전체 대상
    // =========================================================================

    private void buildIndex() throws Exception {
        log.info("  SEARCH_INDEX 행 생성 중...");

        for (Map.Entry<String, String[]> entry : descMap.entrySet()) {
            String descId   = entry.getKey();
            String[] desc   = entry.getValue(); // [conceptId, et, active, typeId, term, lang]
            String conceptId      = desc[0];
            String descEt         = desc[1];
            String descActiveStr  = desc[2];
            String typeId         = desc[3];
            String term           = desc[4];
            String lang           = desc[5];

            // US English acceptability 없는 description 제외
            String acceptId = acceptMap.get(descId);
            if (acceptId == null) continue;

            String[] concept = conceptMap.get(conceptId);
            if (concept == null) continue;

            String conceptEt         = concept[0];
            String conceptActiveStr  = concept[1];
            String defStatusId       = concept[2];

            int conceptActive = "1".equals(conceptActiveStr) ? 1 : 0;
            int descActive    = "1".equals(descActiveStr)    ? 1 : 0;
            int componentActive = (conceptActive == 1 && descActive == 1) ? 1 : 0;

            String componentEt = conceptEt.compareTo(descEt) >= 0 ? conceptEt : descEt;

            String fsn         = fsnMap.getOrDefault(conceptId, "");
            String semanticTag = extractSemanticTag(fsn);
            int primitive      = PRIMITIVE_ID.equals(defStatusId) ? 1 : 0;
            int type           = toType(typeId);
            String mrcmAttrs   = mrcmAttrsMap.getOrDefault(conceptId, "");

            insertPs.setString(1,  conceptId);
            insertPs.setString(2,  descId);
            insertPs.setString(3,  componentEt);
            insertPs.setString(4,  conceptEt);
            insertPs.setString(5,  descEt);
            insertPs.setInt   (6,  componentActive);
            insertPs.setInt   (7,  conceptActive);
            insertPs.setInt   (8,  descActive);
            insertPs.setString(9,  term);
            insertPs.setString(10, semanticTag);
            insertPs.setString(11, fsn);
            insertPs.setInt   (12, primitive);
            insertPs.setInt   (13, type);
            insertPs.setString(14, acceptId);
            insertPs.setInt   (15, term.length());
            insertPs.setString(16, lang);
            insertPs.setString(17, mrcmAttrs);

            insertPs.addBatch();
            insertedRows++;

            if (insertedRows % BATCH_SIZE == 0) {
                insertPs.executeBatch();
                conn.commit();
                log.log(Level.INFO, "  SEARCH_INDEX 적재 중: {0}건...", insertedRows);
            }
        }
    }

    // =========================================================================
    // 유틸
    // =========================================================================

    // =========================================================================
    // Semantic Tag 집계 및 저장
    // =========================================================================

    /**
     * intlFsnMap(International 모듈 Snapshot FSN)에서 semantic tag를 집계하여
     * term.snomed_semantic_tag 테이블에 UPSERT한다.
     *
     * International 모듈(900000000000207008)만 사용하므로 KCD-9 등
     * 국가 extension FSN이 포함하는 임상 괄호 표현이 오염되지 않는다.
     */
    private void saveSemanticTags() throws Exception {
        log.info("  Semantic tag 집계 중 (International FSN " + intlFsnMap.size() + "건)...");

        // tag → [count, max_effective_time]
        Map<String, long[]>   countMap = new HashMap<>();
        Map<String, String>   etMap    = new HashMap<>();

        for (Map.Entry<String, String[]> e : intlFsnMap.entrySet()) {
            String[] val = e.getValue();   // [fsn, effective_time]
            String tag = extractSemanticTag(val[0]);
            if (tag.isEmpty()) continue;

            long[] cnt = countMap.computeIfAbsent(tag, k -> new long[]{0});
            cnt[0]++;
            String et = val[1];
            etMap.merge(tag, et, (a, b) -> a.compareTo(b) >= 0 ? a : b);
        }

        if (countMap.isEmpty()) {
            log.warning("  International FSN에서 semantic tag를 추출하지 못했습니다.");
            return;
        }

        // 기존 데이터 삭제 후 재적재
        try (Statement s = conn.createStatement()) {
            s.execute("TRUNCATE TABLE term.snomed_semantic_tag");
        }

        String upsertSql =
            "INSERT INTO term.snomed_semantic_tag (tag, concept_count, effective_time, loaded_at) " +
            "VALUES (?, ?, ?, NOW())";
        try (PreparedStatement ps = conn.prepareStatement(upsertSql)) {
            for (Map.Entry<String, long[]> e : countMap.entrySet()) {
                String tag = e.getKey();
                ps.setString(1, tag);
                ps.setLong  (2, e.getValue()[0]);
                ps.setString(3, etMap.getOrDefault(tag, ""));
                ps.addBatch();
            }
            ps.executeBatch();
        }
        log.info("  Semantic tag 저장 완료: " + countMap.size() + "종");
    }

    /**
     * FSN에서 Semantic Tag 추출.
     * "Diabetes mellitus (disorder)" → "disorder"
     */
    private static String extractSemanticTag(String fsn) {
        if (fsn == null || fsn.isEmpty()) return "";
        int open  = fsn.lastIndexOf('(');
        int close = fsn.lastIndexOf(')');
        if (open >= 0 && close > open) {
            return fsn.substring(open + 1, close);
        }
        return "";
    }

    /** ES type 필드 값 반환: 1=FSN, 3=Synonym, 4=Definition */
    private static int toType(String typeId) {
        if (FSN_TYPE_ID.equals(typeId)) return 1;
        if (SYN_TYPE_ID.equals(typeId)) return 3;
        if (DEF_TYPE_ID.equals(typeId)) return 4;
        return 0;
    }
}
