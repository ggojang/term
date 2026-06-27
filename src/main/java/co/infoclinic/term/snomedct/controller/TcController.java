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

    /**
     * GET /tc/SNOMEDCT/semanticTags
     * 1순위: search_index.semantic_tag (이미 추출된 컬럼, 빠름)
     * 2순위: description 테이블 FSN에서 정규식으로 추출 (search_index 미적재 환경 대응)
     */
    @ApiOperation(value = "SNOMED CT Semantic Tag 전체 목록 조회")
    @RequestMapping(value = "/tc/SNOMEDCT/semanticTags", method = RequestMethod.GET,
                    produces = "application/json")
    public List<Map<String, Object>> getSemanticTags() {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {

            // 1순위: search_index
            // SNOMED CT International 공식 semantic tag 화이트리스트 기반 조회
            // (KCD-9 확장코드의 임상명이 오염되어 패턴 필터 대신 whitelist 사용)
            String sql1 = "SELECT semantic_tag, COUNT(*) AS cnt " +
                          "FROM term.search_index " +
                          "WHERE semantic_tag IN (" +
                          " 'administrative concept','assessment scale','attribute'," +
                          " 'basic dose form','body structure'," +
                          " 'cell','cell structure','clinical drug','context-dependent category'," +
                          " 'core metadata concept','disposition','dose form'," +
                          " 'environment','environment / location','ethnic group','event'," +
                          " 'finding','foundation metadata concept','geographic location'," +
                          " 'inactive concept','intended site','life style','link assertion'," +
                          " 'linkage concept','medicinal product','medicinal product form'," +
                          " 'metadata','morphologic abnormality','namespace concept'," +
                          " 'navigational concept','observable entity','occupation','organism'," +
                          " 'OWL metadata concept','person','physical force','physical object'," +
                          " 'product','product name','qualifier value','racial group'," +
                          " 'record artifact','release characteristic','religion/philosophy'," +
                          " 'role','situation','SNOMED RT+CTV3','social concept'," +
                          " 'special concept','specimen','staging scale','state of matter'," +
                          " 'substance','supplier','transformation','tumor staging'," +
                          " 'unit of presentation','virtual clinical drug','procedure'," +
                          " 'disorder','regime/therapy'" +
                          ") " +
                          "GROUP BY semantic_tag ORDER BY semantic_tag";
            try (PreparedStatement ps = conn.prepareStatement(sql1);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name",  rs.getString("semantic_tag"));
                    m.put("count", rs.getLong("cnt"));
                    result.add(m);
                }
            }

            // 2순위: description 테이블 FSN에서 추출
            if (result.isEmpty()) {
                String sql2 = "SELECT tag, COUNT(*) AS cnt FROM (" +
                              "  SELECT SUBSTRING(term FROM '\\(([^)]+)\\)$') AS tag " +
                              "  FROM term.description " +
                              "  WHERE type_id = '900000000000003001' AND active = 1 " +
                              "    AND term ~ '\\([^)]+\\)$'" +
                              ") sub " +
                              "WHERE tag IN (" +
                              " 'administrative concept','assessment scale','attribute'," +
                              " 'basic dose form','body structure'," +
                              " 'cell','cell structure','clinical drug','context-dependent category'," +
                              " 'core metadata concept','disposition','dose form'," +
                              " 'environment','environment / location','ethnic group','event'," +
                              " 'finding','foundation metadata concept','geographic location'," +
                              " 'inactive concept','intended site','life style','link assertion'," +
                              " 'linkage concept','medicinal product','medicinal product form'," +
                              " 'metadata','morphologic abnormality','namespace concept'," +
                              " 'navigational concept','observable entity','occupation','organism'," +
                              " 'OWL metadata concept','person','physical force','physical object'," +
                              " 'product','product name','qualifier value','racial group'," +
                              " 'record artifact','release characteristic','religion/philosophy'," +
                              " 'role','situation','SNOMED RT+CTV3','social concept'," +
                              " 'special concept','specimen','staging scale','state of matter'," +
                              " 'substance','supplier','transformation','tumor staging'," +
                              " 'unit of presentation','virtual clinical drug','procedure'," +
                              " 'disorder','regime/therapy'" +
                              ") " +
                              "GROUP BY tag ORDER BY tag";
                try (PreparedStatement ps = conn.prepareStatement(sql2);
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
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
