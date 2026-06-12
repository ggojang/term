package co.infoclinic.term.snomedct.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

/**
 * Map 탭 전용 검색 API (Elasticsearch _analyze / _search 대체)
 *
 * 원본:
 *   POST http://115.68.120.16:19210/snomedct_test/_analyze  → 토큰 분석
 *   POST http://115.68.120.16:19210/snomedct_test/_search   → 검색
 *
 * 대체:
 *   GET  /map/SNOMEDCT/analyze?q={term}                     → 공백 분리 토큰 반환
 *   POST /map/SNOMEDCT/search                               → SEARCH_INDEX pg_trgm 검색
 */
@RestController
public class MapSearchController {

    @Autowired
    private DataSource dataSource;

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
        private String state = "active";   // active | inactive | both
        private int size = 20;
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
    }

    @Data
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MapSearchResponse {
        private long total;
        private List<MapSearchHit> hits = new ArrayList<>();
        private List<Map<String, Object>> semanticTags = new ArrayList<>();
    }

    // ── 토큰 분석 (ES _analyze 대체) ─────────────────────────────────────────

    /**
     * 검색어를 공백 기준으로 분리하여 토큰 목록 반환.
     * ES의 stop_analyzer 동작을 단순화한 버전.
     */
    @RequestMapping(value = "/map/SNOMEDCT/analyze", method = RequestMethod.GET)
    public AnalyzeResponse analyze(@RequestParam("q") String q) {
        AnalyzeResponse resp = new AnalyzeResponse();
        if (q == null || q.trim().isEmpty()) return resp;

        // 영어 불용어 (ES stop_analyzer 기준 일부)
        List<String> stopWords = Arrays.asList(
            "a", "an", "the", "and", "or", "of", "in", "to", "for", "with", "on", "at", "by",
            "from", "is", "was", "are", "were", "be", "been", "being", "have", "has", "had",
            "do", "does", "did", "will", "would", "could", "should", "may", "might", "not"
        );

        String[] parts = q.trim().toLowerCase().split("\\s+");
        for (String part : parts) {
            if (!part.isEmpty() && !stopWords.contains(part)) {
                Map<String, String> token = new LinkedHashMap<>();
                token.put("token", part);
                resp.getTokens().add(token);
            }
        }
        return resp;
    }

    // ── 검색 (ES _search 대체) ────────────────────────────────────────────────

    /**
     * SEARCH_INDEX 테이블에서 pg_trgm/FTS 기반 검색.
     * map/main.js의 stop, stop_synonym, stop_edge5 세 가지 검색을 하나로 통합.
     */
    @RequestMapping(value = "/map/SNOMEDCT/search", method = RequestMethod.POST)
    public MapSearchResponse search(@RequestBody MapSearchRequest req) {
        MapSearchResponse resp = new MapSearchResponse();
        if (req == null || req.getQ() == null || req.getQ().trim().isEmpty()) return resp;

        String word = req.getQ().trim().toLowerCase();
        String activeWhere = "active".equals(req.getState())   ? "component_active = 1"
                           : "inactive".equals(req.getState()) ? "component_active = 0"
                           : "(component_active = 1 OR component_active = 0)";

        // 시맨틱태그 필터
        String semWhere = "";
        if (req.getSemanticTags() != null && !req.getSemanticTags().isEmpty()) {
            StringBuilder sb = new StringBuilder("AND semantic_tag IN (");
            for (int i = 0; i < req.getSemanticTags().size(); i++) {
                sb.append(i == 0 ? "?" : ",?");
            }
            sb.append(")");
            semWhere = sb.toString();
        }

        // 검색 조건: 단어 여러 개면 FTS, 하나면 trgm LIKE
        String termWhere = word.contains(" ")
            ? "to_tsvector('english', term) @@ plainto_tsquery('english', ?)"
            : "term ILIKE ?";
        String termParam = word.contains(" ") ? word : "%" + word + "%";

        int offset = (req.getPage() - 1) * req.getSize();

        String countSql = "SELECT COUNT(*) FROM term.search_index WHERE " + activeWhere
                        + " AND (" + termWhere + ") " + semWhere;

        String dataSql = "SELECT concept_id, description_id, term, fsn, semantic_tag, lang,"
                       + " acceptability_id, term_length, concept_active, description_active"
                       + " FROM term.search_index WHERE " + activeWhere
                       + " AND (" + termWhere + ") " + semWhere
                       + " ORDER BY term_length ASC, term ASC"
                       + " LIMIT ? OFFSET ?";

        String semAggSql = "SELECT semantic_tag, COUNT(*) AS cnt FROM term.search_index"
                         + " WHERE " + activeWhere + " AND (" + termWhere + ") "
                         + " GROUP BY semantic_tag ORDER BY cnt DESC LIMIT 50";

        try (Connection conn = dataSource.getConnection()) {
            // 건수
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                int idx = 1;
                ps.setString(idx++, termParam);
                if (req.getSemanticTags() != null) {
                    for (String st : req.getSemanticTags()) ps.setString(idx++, st);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) resp.setTotal(rs.getLong(1));
                }
            }

            // 결과
            try (PreparedStatement ps = conn.prepareStatement(dataSql)) {
                int idx = 1;
                ps.setString(idx++, termParam);
                if (req.getSemanticTags() != null) {
                    for (String st : req.getSemanticTags()) ps.setString(idx++, st);
                }
                ps.setInt(idx++, req.getSize());
                ps.setInt(idx,   offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        MapSearchHit hit = new MapSearchHit();
                        hit.setConceptId(rs.getString("concept_id"));
                        hit.setDescriptionId(rs.getString("description_id"));
                        hit.setTerm(rs.getString("term"));
                        hit.setFsn(rs.getString("fsn"));
                        hit.setSemanticTag(rs.getString("semantic_tag"));
                        hit.setLang(rs.getString("lang"));
                        hit.setAcceptabilityId(rs.getString("acceptability_id"));
                        hit.setTermLength(rs.getInt("term_length"));
                        hit.setConceptActive(rs.getInt("concept_active") == 1);
                        hit.setDescriptionActive(rs.getInt("description_active") == 1);
                        resp.getHits().add(hit);
                    }
                }
            }

            // 시맨틱태그 집계
            try (PreparedStatement ps = conn.prepareStatement(semAggSql)) {
                ps.setString(1, termParam);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> entry = new LinkedHashMap<>();
                        entry.put("name", rs.getString("semantic_tag"));
                        entry.put("count", rs.getLong("cnt"));
                        resp.getSemanticTags().add(entry);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }
}
