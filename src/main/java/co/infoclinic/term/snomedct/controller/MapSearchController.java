package co.infoclinic.term.snomedct.controller;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Map 탭 전용 검색 API
 *
 * ES _analyze / _search (snomedct_test 인덱스, stop/stop_synonym/stop_edge5 analyzer) 대체.
 *
 * ── ES analyzer 대응표 ─────────────────────────────────────────────────────
 *   stop          → PostgreSQL FTS: to_tsvector('english', term) @@ plainto_tsquery(...)
 *   stop_synonym  → 동의어 확장: UMLS_SYNONYM/UMLS_SYNONYM_PHRASE 조회 후 ILIKE ANY(...)
 *   stop_edge5    → pg_trgm: term ILIKE '%q%'
 * ──────────────────────────────────────────────────────────────────────────
 *
 * ── ES 불용어 필터 (en_stopwords) 대응 ─────────────────────────────────────
 *   PostgreSQL FTS 'english' 사전이 영어 불용어를 자동으로 제거.
 *   analyze 엔드포인트에서도 동일 불용어 목록을 직접 적용.
 * ──────────────────────────────────────────────────────────────────────────
 *
 * ── UMLS 동의어 확장 흐름 ────────────────────────────────────────────────
 *   1. 검색어(다중 단어) → UMLS_SYNONYM_PHRASE 테이블에서 canonical 조회
 *      (예: "renal failure" → "renal_failure")
 *   2. canonical → UMLS_SYNONYM 테이블에서 모든 동의어 조회
 *      (예: "renal_failure" → "kidney_failure", "renal_insufficiency", ...)
 *   3. 동의어(언더스코어→공백 변환)를 SEARCH_INDEX에서 ILIKE 검색
 * ──────────────────────────────────────────────────────────────────────────
 */
@Api(tags = "VI-01. Map")
@RestController
public class MapSearchController {

    @Autowired
    private DataSource dataSource;

    // 영어 불용어 (ES _english_ stopwords 핵심 목록)
    private static final Set<String> STOP_WORDS = new LinkedHashSet<>(Arrays.asList(
        "a", "an", "the", "and", "or", "of", "in", "to", "for", "with", "on", "at", "by",
        "from", "is", "was", "are", "were", "be", "been", "being", "have", "has", "had",
        "do", "does", "did", "will", "would", "could", "should", "may", "might", "not",
        "this", "that", "these", "those", "it", "its", "as", "but", "if", "then", "than",
        "so", "no", "nor", "yet", "both", "either", "neither", "each", "such", "about"
    ));

    // ── DTOs ─────────────────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AnalyzeResponse {
        private List<Map<String, String>> tokens = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    public static class MapSearchRequest {
        private String q;
        private List<String> semanticTags;
        private String state = "active";
        private int size = 500;
        private int page = 1;
    }

    @Data
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MapSearchHit {
        private String conceptId;
        private String descriptionId;
        private String term;
        private String fsn;
        private String semanticTag;
        private String lang;
        private String acceptabilityId;
        private int termLength;
        private boolean conceptActive;
        private boolean descriptionActive;
        private String matchType;  // "stop" | "synonym" | "edge"
    }

    @Data
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MapSearchResponse {
        private long total;
        private List<MapSearchHit> hits = new ArrayList<>();
        private List<Map<String, Object>> semanticTags = new ArrayList<>();
    }

    // =========================================================================
    // GET /map/SNOMEDCT/analyze?q={term}
    // ES _analyze (stop_analyzer) 대체: 불용어 제거 후 토큰 반환
    // =========================================================================

    @ApiOperation(value = "Mapping 분석 (단어 토크나이징) [GET]")
    @RequestMapping(value = "/map/SNOMEDCT/analyze", method = RequestMethod.GET)
    public AnalyzeResponse analyze(@RequestParam("q") String q) {
        AnalyzeResponse resp = new AnalyzeResponse();
        if (q == null || q.trim().isEmpty()) return resp;

        String[] parts = q.trim().toLowerCase().split("\\s+");
        for (String part : parts) {
            String clean = part.replaceAll("[^a-z0-9]", "");
            if (!clean.isEmpty() && !STOP_WORDS.contains(clean)) {
                Map<String, String> token = new LinkedHashMap<>();
                token.put("token", clean);
                resp.getTokens().add(token);
            }
        }
        return resp;
    }

    // =========================================================================
    // POST /map/SNOMEDCT/search
    // ES _search (stop / stop_synonym / stop_edge5) 통합 대체
    // =========================================================================

    @ApiOperation(value = "Mapping 검색 (FTS + trigram) [POST]")
    @RequestMapping(value = "/map/SNOMEDCT/search", method = RequestMethod.POST)
    public MapSearchResponse search(@RequestBody MapSearchRequest req) {
        MapSearchResponse resp = new MapSearchResponse();
        if (req == null || req.getQ() == null || req.getQ().trim().isEmpty()) return resp;

        String word = req.getQ().trim().toLowerCase();
        int offset  = Math.max(0, (req.getPage() - 1)) * req.getSize();

        String activeWhere = buildActiveWhere(req.getState());
        String semWhere    = buildSemWhere(req.getSemanticTags());
        List<String> semParams = req.getSemanticTags() != null ? req.getSemanticTags() : new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {

            // ── 1. stop: FTS (ES stop_analyzer 대체) ──────────────────────────
            List<MapSearchHit> stopHits = searchFts(conn, word, activeWhere, semWhere, semParams, req.getSize(), offset, "stop");

            // ── 2. stop_synonym: 동의어 확장 검색 (ES stop_synonym_analyzer 대체) ─
            List<String> synonyms = expandSynonyms(conn, word);
            List<MapSearchHit> synHits = new ArrayList<>();
            if (!synonyms.isEmpty()) {
                synHits = searchBySynonyms(conn, synonyms, activeWhere, semWhere, semParams, req.getSize(), "synonym");
            }

            // ── 3. stop_edge5: pg_trgm ILIKE (ES stop_edge5_analyzer 대체) ────
            List<MapSearchHit> edgeHits = searchTrgm(conn, word, activeWhere, semWhere, semParams, req.getSize(), offset, "edge");

            // ── 결과 병합: 중복 제거 (term 기준), stop > synonym > edge 우선순위 ──
            Map<String, MapSearchHit> merged = new LinkedHashMap<>();
            for (MapSearchHit h : stopHits)  { merged.put(h.getTerm(), h); }
            for (MapSearchHit h : synHits)   { merged.putIfAbsent(h.getTerm(), h); }
            for (MapSearchHit h : edgeHits)  { merged.putIfAbsent(h.getTerm(), h); }

            List<MapSearchHit> allHits = new ArrayList<>(merged.values());
            resp.setTotal(allHits.size());
            resp.setHits(allHits);

            // ── 시맨틱태그 집계 ────────────────────────────────────────────────
            resp.setSemanticTags(aggregateSemanticTags(conn, word, activeWhere, semWhere, semParams));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }

    // =========================================================================
    // 내부: FTS 검색 (stop_analyzer 대체)
    // =========================================================================

    private List<MapSearchHit> searchFts(Connection conn, String word,
            String activeWhere, String semWhere, List<String> semParams,
            int size, int offset, String matchType) throws Exception {

        String sql = "SELECT concept_id, description_id, term, fsn, semantic_tag, lang,"
                   + " acceptability_id, term_length, concept_active, description_active"
                   + " FROM term.search_index WHERE " + activeWhere
                   + " AND to_tsvector('english', term) @@ plainto_tsquery('english', ?)"
                   + (semWhere.isEmpty() ? "" : " AND " + semWhere)
                   + " ORDER BY term_length ASC LIMIT ? OFFSET ?";

        List<MapSearchHit> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, word);
            for (String s : semParams) ps.setString(idx++, s);
            ps.setInt(idx++, size);
            ps.setInt(idx,   offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapHit(rs, matchType));
            }
        }
        return list;
    }

    // =========================================================================
    // 내부: 동의어 확장 (stop_synonym_analyzer 대체)
    //   1. syn_ph.txt → UMLS_SYNONYM_PHRASE: 구문 정규화
    //   2. syn.txt → UMLS_SYNONYM: 동의어 목록 조회
    // =========================================================================

    private List<String> expandSynonyms(Connection conn, String word) throws Exception {
        Set<String> result = new LinkedHashSet<>();

        // 단계 1: 구문 정규화 (공백 → 언더스코어로 조회)
        String normalized = word.replace(' ', '_');

        // UMLS_SYNONYM_PHRASE 테이블에서 canonical 조회
        String phraseSql = "SELECT canonical FROM term.umls_synonym_phrase WHERE lower(phrase) = lower(?)";
        List<String> canonicals = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(phraseSql)) {
            ps.setString(1, word);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) canonicals.add(rs.getString(1));
            }
        }
        // 매핑 없으면 입력어 자체를 canonical로 사용
        if (canonicals.isEmpty()) {
            canonicals.add(normalized);
        }

        // 단계 2: UMLS_SYNONYM 테이블에서 동의어 조회
        String synSql = "SELECT synonym FROM term.umls_synonym WHERE lower(canonical) = lower(?)";
        for (String canonical : canonicals) {
            try (PreparedStatement ps = conn.prepareStatement(synSql)) {
                ps.setString(1, canonical);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        // 언더스코어 → 공백 변환 (SEARCH_INDEX term은 공백 형식)
                        result.add(rs.getString(1).replace('_', ' '));
                    }
                }
            }
        }

        return new ArrayList<>(result);
    }

    // =========================================================================
    // 내부: 동의어 목록으로 SEARCH_INDEX 검색
    // =========================================================================

    private List<MapSearchHit> searchBySynonyms(Connection conn, List<String> synonyms,
            String activeWhere, String semWhere, List<String> semParams,
            int size, String matchType) throws Exception {

        if (synonyms.isEmpty()) return new ArrayList<>();

        // ILIKE ANY(ARRAY[...]) 사용 (PostgreSQL 배열 비교)
        StringBuilder inPart = new StringBuilder();
        for (int i = 0; i < synonyms.size(); i++) {
            inPart.append(i == 0 ? "?" : ",?");
        }

        String sql = "SELECT concept_id, description_id, term, fsn, semantic_tag, lang,"
                   + " acceptability_id, term_length, concept_active, description_active"
                   + " FROM term.search_index WHERE " + activeWhere
                   + " AND lower(term) = ANY(ARRAY[" + inPart + "]::text[])"
                   + (semWhere.isEmpty() ? "" : " AND " + semWhere)
                   + " ORDER BY term_length ASC LIMIT ?";

        List<MapSearchHit> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            for (String syn : synonyms) ps.setString(idx++, syn.toLowerCase());
            for (String s : semParams) ps.setString(idx++, s);
            ps.setInt(idx, size);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapHit(rs, matchType));
            }
        }
        return list;
    }

    // =========================================================================
    // 내부: pg_trgm ILIKE 검색 (stop_edge5_analyzer 대체)
    // =========================================================================

    private List<MapSearchHit> searchTrgm(Connection conn, String word,
            String activeWhere, String semWhere, List<String> semParams,
            int size, int offset, String matchType) throws Exception {

        String sql = "SELECT concept_id, description_id, term, fsn, semantic_tag, lang,"
                   + " acceptability_id, term_length, concept_active, description_active"
                   + " FROM term.search_index WHERE " + activeWhere
                   + " AND term ILIKE ?"
                   + (semWhere.isEmpty() ? "" : " AND " + semWhere)
                   + " ORDER BY term_length ASC LIMIT ? OFFSET ?";

        List<MapSearchHit> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, "%" + word + "%");
            for (String s : semParams) ps.setString(idx++, s);
            ps.setInt(idx++, size);
            ps.setInt(idx,   offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapHit(rs, matchType));
            }
        }
        return list;
    }

    // =========================================================================
    // 내부: 시맨틱태그 집계
    // =========================================================================

    private List<Map<String, Object>> aggregateSemanticTags(Connection conn, String word,
            String activeWhere, String semWhere, List<String> semParams) throws Exception {

        String sql = "SELECT semantic_tag, COUNT(*) AS cnt"
                   + " FROM term.search_index WHERE " + activeWhere
                   + " AND term ILIKE ?"
                   + (semWhere.isEmpty() ? "" : " AND " + semWhere)
                   + " GROUP BY semantic_tag ORDER BY cnt DESC LIMIT 50";

        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, "%" + word + "%");
            for (String s : semParams) ps.setString(idx++, s);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("name",  rs.getString("semantic_tag"));
                    entry.put("count", rs.getLong("cnt"));
                    list.add(entry);
                }
            }
        }
        return list;
    }

    // =========================================================================
    // 유틸
    // =========================================================================

    private String buildActiveWhere(String state) {
        if ("active".equals(state))   return "component_active = 1";
        if ("inactive".equals(state)) return "component_active = 0";
        return "(component_active = 1 OR component_active = 0)";
    }

    private String buildSemWhere(List<String> tags) {
        if (tags == null || tags.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("semantic_tag IN (");
        for (int i = 0; i < tags.size(); i++) sb.append(i == 0 ? "?" : ",?");
        sb.append(")");
        return sb.toString();
    }

    private MapSearchHit mapHit(ResultSet rs, String matchType) throws Exception {
        MapSearchHit h = new MapSearchHit();
        h.setConceptId(rs.getString("concept_id"));
        h.setDescriptionId(rs.getString("description_id"));
        h.setTerm(rs.getString("term"));
        h.setFsn(rs.getString("fsn"));
        h.setSemanticTag(rs.getString("semantic_tag"));
        h.setLang(rs.getString("lang"));
        h.setAcceptabilityId(rs.getString("acceptability_id"));
        h.setTermLength(rs.getInt("term_length"));
        h.setConceptActive(rs.getInt("concept_active") == 1);
        h.setDescriptionActive(rs.getInt("description_active") == 1);
        h.setMatchType(matchType);
        return h;
    }
}
