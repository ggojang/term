package co.infoclinic.term.snomedct.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * TC (Transitive Closure) 조회 API
 *
 * 원본: http://localhost:4000/TC/SNOMEDCT/{semanticType}?q={term}
 * 현재: /TC/SNOMEDCT/{semanticType}?q={term}
 *
 * semanticType 값 → SNOMED CT semantic tag 매핑:
 *   PROCEDURE → procedure
 *   DISORDER  → disorder
 *   FINDING   → finding
 */
@Api(tags = "I-10 SNOMEDCT")
@RestController
public class TcController {

    @Autowired
    private DataSource dataSource;

    /** TC 조회 결과 DTO */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TcEntry {
        private String conceptId;
        private String term;
        private String parentId;
        private int depth;
        private String path;
        private int childrenCount;
        private int descendantCount;
    }

    /**
     * TC 테이블에서 semantic type + 검색어로 개념 목록 조회
     *
     * @param semanticType PROCEDURE | DISORDER | FINDING
     * @param q            검색어 (FSN 부분 매치)
     * @return TC 엔트리 목록 (최대 200건)
     */
    @ApiOperation(value = "Transitive Closure (의미유형별 개념 목록) [GET]")
    @RequestMapping(value = "/TC/SNOMEDCT/{semanticType}", method = RequestMethod.GET)
    public List<TcEntry> getTcBySemanticType(
            @PathVariable String semanticType,
            @RequestParam(value = "q", required = false, defaultValue = "") String q) {

        String semanticTag = toSemanticTag(semanticType);
        List<TcEntry> result = new ArrayList<>();

        // TC 테이블의 TERM 컬럼은 FSN 형태: "Diabetes (disorder)"
        // semantic tag 필터: FSN의 마지막 괄호 내용이 semanticTag와 일치하는 것
        String sql = "SELECT tc.concept_id, tc.term, tc.parent_id,"
                   + " tc.depth, tc.path, tc.children_count, tc.descendant_count"
                   + " FROM term.tc"
                   + " WHERE tc.term ILIKE ?"       // FSN 검색어 매치
                   + " AND tc.term LIKE ?";          // semantic tag 필터: "%(tag)"

        if (!semanticTag.isEmpty()) {
            sql += " ORDER BY tc.term_length ASC, tc.term ASC LIMIT 200";
        } else {
            sql += " ORDER BY tc.term ASC LIMIT 200";
        }

        // TC 테이블에 term_length 컬럼이 없으면 단순 term ASC
        sql = "SELECT tc.concept_id, tc.term, tc.parent_id,"
            + " tc.depth, tc.path, tc.children_count, tc.descendant_count"
            + " FROM term.tc"
            + " WHERE tc.term ILIKE ?"
            + " AND tc.term ILIKE ?"
            + " ORDER BY tc.term ASC LIMIT 200";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q.isEmpty() ? "%" : "%" + q + "%");
            ps.setString(2, "%(" + semanticTag + ")%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new TcEntry(
                        rs.getString("concept_id"),
                        rs.getString("term"),
                        rs.getString("parent_id"),
                        rs.getInt("depth"),
                        rs.getString("path"),
                        rs.getInt("children_count"),
                        rs.getInt("descendant_count")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * TC_CONCEPT_STATS에서 특정 개념의 자손 수 반환
     * GET /tc/SNOMEDCT/descendantCount/{conceptId}?version=v20260601
     */
    @ApiOperation(value = "개념의 자손 수 조회 [GET]")
    @RequestMapping(value = "/tc/SNOMEDCT/descendantCount/{conceptId}", method = RequestMethod.GET)
    public long getDescendantCount(
            @PathVariable String conceptId,
            @RequestParam(value = "version", required = false, defaultValue = "") String version) {

        // version: "v20260601" → effectiveTime: "20260601"
        String et = version.toLowerCase().replaceFirst("^v", "");

        String sql;
        if (et.isEmpty()) {
            sql = "SELECT descendant_count FROM term.tc_concept_stats " +
                  "WHERE concept_id = ? ORDER BY effective_time DESC LIMIT 1";
        } else {
            sql = "SELECT descendant_count FROM term.tc_concept_stats " +
                  "WHERE concept_id = ? AND effective_time <= ? ORDER BY effective_time DESC LIMIT 1";
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, conceptId);
            if (!et.isEmpty()) ps.setString(2, et);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** 서버 재시작 전까지 semantic tag 결과 캐시 */
    private static volatile List<Map<String, Object>> semanticTagCache = null;

    /**
     * GET /tc/SNOMEDCT/semanticTags
     * 1순위: 메모리 캐시 (서버 재시작 전까지 유지 — 즉시 반환)
     * 2순위: snomed_semantic_tag 테이블 (SearchIndexLoader 적재 시 자동 갱신)
     * 3순위: description + concept Snapshot JOIN (테이블 없는 초기 환경)
     *        → 조회 후 snomed_semantic_tag 테이블에 자동 저장해 다음 기동부터 빠르게
     */
    @ApiOperation(value = "SNOMED CT Semantic Tag 전체 목록 조회")
    @RequestMapping(value = "/tc/SNOMEDCT/semanticTags", method = RequestMethod.GET,
                    produces = "application/json")
    public List<Map<String, Object>> getSemanticTags() {
        if (semanticTagCache != null) return semanticTagCache;

        List<Map<String, Object>> result = new ArrayList<>();
        boolean fromFallback = false;

        try (Connection conn = dataSource.getConnection()) {

            // 1순위: snomed_semantic_tag 테이블
            boolean tagTableExists = false;
            try (PreparedStatement chk = conn.prepareStatement(
                    "SELECT 1 FROM information_schema.tables " +
                    "WHERE table_schema='term' AND table_name='snomed_semantic_tag'");
                 ResultSet rs = chk.executeQuery()) {
                tagTableExists = rs.next();
            }
            if (tagTableExists) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT tag, concept_count FROM term.snomed_semantic_tag ORDER BY tag");
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("name",  rs.getString("tag"));
                        m.put("count", rs.getLong("concept_count"));
                        result.add(m);
                    }
                }
            }

            // 2순위: description + concept Snapshot JOIN (테이블 없거나 비어있을 때)
            if (result.isEmpty()) {
                fromFallback = true;
                String sql =
                    "SELECT tag, COUNT(*) AS cnt FROM (" +
                    "  SELECT DISTINCT ON (d.description_id)" +
                    "    SUBSTRING(d.term FROM '\\(([^)]+)\\)$') AS tag," +
                    "    d.active AS desc_active, c_latest.active AS concept_active " +
                    "  FROM term.description d" +
                    "  JOIN (" +
                    "    SELECT DISTINCT ON (concept_id) concept_id, active" +
                    "    FROM term.concept" +
                    "    ORDER BY concept_id, effective_time DESC" +
                    "  ) c_latest ON c_latest.concept_id = d.concept_id" +
                    "  WHERE d.module_id = '900000000000207008'" +
                    "    AND d.type_id   = '900000000000003001'" +
                    "    AND d.term ~ '\\([^)]+\\)$'" +
                    "  ORDER BY d.description_id, d.effective_time DESC" +
                    ") latest " +
                    "WHERE desc_active = 1 AND concept_active = 1 " +
                    "  AND tag IS NOT NULL AND tag <> '' " +
                    "GROUP BY tag ORDER BY tag";
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String tag = rs.getString("tag");
                        if (tag != null && !tag.isEmpty()) {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("name",  tag);
                            m.put("count", rs.getLong("cnt"));
                            result.add(m);
                        }
                    }
                }

                // fallback 결과를 snomed_semantic_tag 테이블에 저장 → 다음 기동부터 빠르게
                if (!result.isEmpty() && tagTableExists) {
                    try {
                        conn.setAutoCommit(false);
                        try (PreparedStatement del = conn.prepareStatement(
                                "TRUNCATE TABLE term.snomed_semantic_tag")) {
                            del.execute();
                        }
                        try (PreparedStatement ins = conn.prepareStatement(
                                "INSERT INTO term.snomed_semantic_tag " +
                                "(tag, concept_count, effective_time) VALUES (?, ?, '')")) {
                            for (Map<String, Object> m : result) {
                                ins.setString(1, (String) m.get("name"));
                                ins.setLong  (2, ((Number) m.get("count")).longValue());
                                ins.addBatch();
                            }
                            ins.executeBatch();
                        }
                        conn.commit();
                        conn.setAutoCommit(true);
                    } catch (Exception ex) {
                        try { conn.rollback(); conn.setAutoCommit(true); } catch (Exception ignored) {}
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!result.isEmpty()) semanticTagCache = result;
        return result;
    }

    private String toSemanticTag(String semanticType) {
        if (semanticType == null) return "";
        switch (semanticType.toUpperCase()) {
            case "PROCEDURE": return "procedure";
            case "DISORDER":  return "disorder";
            case "FINDING":   return "finding";
            default:          return semanticType.toLowerCase();
        }
    }
}
