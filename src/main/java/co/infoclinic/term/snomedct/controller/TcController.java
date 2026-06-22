package co.infoclinic.term.snomedct.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
