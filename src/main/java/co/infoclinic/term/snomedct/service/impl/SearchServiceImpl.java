package co.infoclinic.term.snomedct.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.utils.MatchType;
import co.infoclinic.term.common.utils.SNOMEDCTComponentTypeEnum;
import co.infoclinic.term.common.utils.SNOMEDCTUtils;
import co.infoclinic.term.common.utils.StateType;
import co.infoclinic.term.snomedct.model.dto.SearchResults;
import co.infoclinic.term.snomedct.model.dto.SemanticTag;
import co.infoclinic.term.snomedct.model.dto.TermSearchResult;
import co.infoclinic.term.snomedct.service.SearchService;
import io.searchbox.core.SearchResult;

/**
 * SNOMED CT 검색 서비스 (PostgreSQL SEARCH_INDEX 기반)
 *
 * 원래 Elasticsearch + Jest 구현에서 PostgreSQL pg_trgm / FTS 기반으로 전환.
 *
 * SEARCH_INDEX 테이블 검색 전략:
 *   - PARTIAL  : pg_trgm LIKE '%q%' 또는 to_tsvector FTS (단어 경계 없는 부분 매치)
 *   - FULLTEXT : term ILIKE word (완전 소문자 일치)
 *   - REGEX    : term ~* regexp  (PostgreSQL POSIX 정규식)
 *   - 숫자(ID) : concept_id / description_id 정확 일치
 *
 * 정렬: term_length ASC, term ASC (원 ES 정렬 length/term.raw_lc 동일)
 * SemanticTag 집계: GROUP BY semantic_tag 후 COUNT
 */
@Service("SCTSrchSvc")
public class SearchServiceImpl implements SearchService {

    Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

    @Autowired
    private DataSource dataSource;

    // =========================================================================
    // searchTerm: /search/SNOMEDCT
    // =========================================================================

    @Override
    public SearchResults searchTerm(final MatchType matchType, final StateType stateType,
            final List<String> semanticFilter, final String q, Pageable pageable) throws Exception {

        final String word = q.trim().toLowerCase();
        boolean isNumeric = StringUtils.isNumericSpace(word);

        // 활성 상태 조건
        String activeWhere = buildActiveWhere(stateType);

        // 검색어 조건 + 파라미터 (PreparedStatement용)
        String termWhere;
        String termParam = word;   // 단순 값; 특수케이스는 아래서 덮어씀
        boolean idSearch = false;
        String idCol = null;

        if (isNumeric && word.length() >= 6 && word.length() <= 18) {
            SNOMEDCTComponentTypeEnum typ = SNOMEDCTComponentTypeEnum.getById(word);
            if (SNOMEDCTComponentTypeEnum.CONCEPT.equals(typ)) {
                termWhere = "concept_id = ?";
                idCol = "concept_id"; idSearch = true;
            } else if (SNOMEDCTComponentTypeEnum.DESCRIPTION.equals(typ)) {
                termWhere = "description_id = ?";
                idCol = "description_id"; idSearch = true;
            } else {
                termWhere = buildTermWhere(matchType, word);
            }
        } else {
            termWhere = buildTermWhere(matchType, word);
        }

        // 시맨틱태그 필터
        String semWhere = buildSemanticWhere(semanticFilter);

        String baseWhere = "WHERE " + activeWhere
                + " AND (" + termWhere + ")"
                + (semWhere.isEmpty() ? "" : " AND " + semWhere);

        // 전체 건수 + 시맨틱태그 집계
        String countSql = "SELECT COUNT(*) FROM term.search_index " + baseWhere;
        String semSql   = "SELECT semantic_tag, COUNT(*) AS cnt FROM term.search_index "
                        + baseWhere
                        + " GROUP BY semantic_tag ORDER BY cnt DESC LIMIT 100";
        String dataSql  = "SELECT concept_id, description_id,"
                        + " component_effective_time, concept_effective_time, description_effective_time,"
                        + " concept_active, description_active, type, fsn, term_length, semantic_tag,"
                        + " term, lang, acceptability_id, primitive"
                        + " FROM term.search_index " + baseWhere
                        + " ORDER BY term_length ASC, term ASC"
                        + " LIMIT ? OFFSET ?";

        SearchResults results = new SearchResults();
        try (Connection conn = dataSource.getConnection()) {
            // ── 건수 ──
            long total = 0;
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                setTermParam(ps, 1, idSearch ? word : termParam, matchType, idSearch);
                setSemParams(ps, idSearch ? 2 : nextParamIdx(matchType, idSearch), semanticFilter);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) total = rs.getLong(1);
                }
            }

            if (total == 0) {
                results.setPage(new PageImpl<>(new ArrayList<>(), pageable, 0));
                results.setSemanticTags(new ArrayList<>());
                return results;
            }

            // ── 시맨틱태그 집계 ──
            List<SemanticTag> semTags = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(semSql)) {
                setTermParam(ps, 1, idSearch ? word : termParam, matchType, idSearch);
                setSemParams(ps, idSearch ? 2 : nextParamIdx(matchType, idSearch), semanticFilter);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        SemanticTag st = new SemanticTag();
                        st.setName(rs.getString("semantic_tag"));
                        st.setCount((int) rs.getLong("cnt"));
                        semTags.add(st);
                    }
                }
            }

            // ── 결과 행 ──
            List<TermSearchResult> rows = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(dataSql)) {
                int idx = 1;
                idx = setTermParam(ps, idx, idSearch ? word : termParam, matchType, idSearch);
                idx = setSemParams(ps, idx, semanticFilter);
                ps.setInt(idx++, pageable.getPageSize());
                ps.setLong(idx,  pageable.getOffset());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows.add(mapRow(rs));
                    }
                }
            }

            results.setPage(new PageImpl<>(rows, pageable, total));
            results.setSemanticTags(semTags);
        }
        return results;
    }

    // =========================================================================
    // getValueListByMrcmAttr: MRCM value 검색 (mrcm.js)
    // =========================================================================

    @Override
    public List<TermSearchResult> getValueListByMrcmAttr(String attr, String q, int size) {
        if (attr == null || q == null || !StringUtils.isNumeric(attr)) {
            return new ArrayList<>();
        }

        String termWhere;
        boolean idSearch = false;
        if (StringUtils.isNumeric(q) && q.length() >= 6) {
            termWhere = "concept_id = ?";
            idSearch = true;
        } else {
            termWhere = "term ILIKE ?";
        }

        String sql = "SELECT concept_id, description_id,"
                   + " component_effective_time, concept_effective_time, description_effective_time,"
                   + " concept_active, description_active, type, fsn, term_length, semantic_tag,"
                   + " term, lang, acceptability_id, primitive"
                   + " FROM term.search_index"
                   + " WHERE component_active = 1"
                   + " AND mrcm_attrs LIKE ?"
                   + " AND (" + termWhere + ")"
                   + " ORDER BY term_length ASC LIMIT ?";

        List<TermSearchResult> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + attr + "%");
            if (idSearch) {
                ps.setString(2, q);
            } else {
                ps.setString(2, "%" + q.toLowerCase() + "%");
            }
            ps.setInt(3, size);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (Exception e) {
            log.error("getValueListByMrcmAttr error", e);
        }
        return list;
    }

    // =========================================================================
    // getSuggestResultListByQueryAndSize: 자동완성 전체 범위
    // =========================================================================

    @Override
    public List<TermSearchResult> getSuggestResultListByQueryAndSize(String q, int size) {
        if (q == null || q.isEmpty()) return new ArrayList<>();

        boolean isId = StringUtils.isNumeric(q) && q.length() >= 6 && q.length() <= 18;
        String termWhere;

        if (isId) {
            SNOMEDCTComponentTypeEnum typ = SNOMEDCTComponentTypeEnum.getById(q);
            if (SNOMEDCTComponentTypeEnum.CONCEPT.equals(typ)) {
                termWhere = "concept_id = ?";
            } else if (SNOMEDCTComponentTypeEnum.DESCRIPTION.equals(typ)) {
                termWhere = "description_id = ?";
            } else {
                isId = false;
                termWhere = "term ILIKE ?";
            }
        } else {
            termWhere = "term ILIKE ?";
        }

        String sql = "SELECT concept_id, description_id,"
                   + " component_effective_time, concept_effective_time, description_effective_time,"
                   + " concept_active, description_active, type, fsn, term_length, semantic_tag,"
                   + " term, lang, acceptability_id, primitive"
                   + " FROM term.search_index"
                   + " WHERE component_active = 1 AND (" + termWhere + ")"
                   + " ORDER BY term_length ASC LIMIT ?";

        List<TermSearchResult> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isId ? q : "%" + q.toLowerCase() + "%");
            ps.setInt(2, size);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TermSearchResult r = new TermSearchResult();
                    r.setConceptId(rs.getString("concept_id"));
                    r.setDescriptionId(rs.getString("description_id"));
                    r.setTerm(rs.getString("term"));
                    r.setFsn(rs.getString("fsn"));
                    r.setLength(rs.getInt("term_length"));
                    r.setLang(rs.getString("lang"));
                    r.setAcceptabilityId(rs.getString("acceptability_id"));
                    int prim = rs.getInt("primitive");
                    r.setDefinitionStatusId(prim == 1
                            ? SNOMEDCTUtils.DefinitionStatus.Primitive
                            : SNOMEDCTUtils.DefinitionStatus.Defined);
                    list.add(r);
                }
            }
        } catch (Exception e) {
            log.error("getSuggestResultListByQueryAndSize error", e);
        }
        return list;
    }

    // =========================================================================
    // getSuggestResultListByDescendantOrSelfIdAndQueryAndSize: 범위 한정 자동완성
    //   원본 ES 구현: ancestorPath wildcard → TC 테이블의 PATH 컬럼 LIKE 대체
    // =========================================================================

    @Override
    public List<TermSearchResult> getSuggestResultListByDescendantOrSelfIdAndQueryAndSize(
            String descendantOrSelfId, String q, int size) {

        if (q == null || !SNOMEDCTComponentTypeEnum.isValidIdentifier(descendantOrSelfId)) {
            return new ArrayList<>();
        }

        boolean isId = StringUtils.isNumeric(q) && q.length() >= 6 && q.length() <= 18;
        String termWhere;
        if (isId) {
            SNOMEDCTComponentTypeEnum typ = SNOMEDCTComponentTypeEnum.getById(q);
            if (SNOMEDCTComponentTypeEnum.CONCEPT.equals(typ)) {
                termWhere = "si.concept_id = ?";
            } else if (SNOMEDCTComponentTypeEnum.DESCRIPTION.equals(typ)) {
                termWhere = "si.description_id = ?";
            } else {
                isId = false;
                termWhere = "si.term ILIKE ?";
            }
        } else {
            termWhere = "si.term ILIKE ?";
        }

        // TC 테이블의 PATH에 descendantOrSelfId가 포함된 개념만 대상으로 함
        String sql = "SELECT si.concept_id, si.description_id,"
                   + " si.component_effective_time, si.concept_effective_time, si.description_effective_time,"
                   + " si.concept_active, si.description_active, si.type, si.fsn, si.term_length,"
                   + " si.semantic_tag, si.term, si.lang, si.acceptability_id, si.primitive"
                   + " FROM term.search_index si"
                   + " WHERE si.component_active = 1"
                   + " AND si.concept_id IN ("
                   + "   SELECT concept_id FROM term.tc WHERE path LIKE ?"
                   + " )"
                   + " AND (" + termWhere + ")"
                   + " ORDER BY si.term_length ASC LIMIT ?";

        List<TermSearchResult> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + descendantOrSelfId + "%");
            ps.setString(2, isId ? q : "%" + q.toLowerCase() + "%");
            ps.setInt(3, size);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TermSearchResult r = new TermSearchResult();
                    r.setConceptId(rs.getString("concept_id"));
                    r.setDescriptionId(rs.getString("description_id"));
                    r.setTerm(rs.getString("term"));
                    r.setFsn(rs.getString("fsn"));
                    r.setLength(rs.getInt("term_length"));
                    r.setLang(rs.getString("lang"));
                    r.setAcceptabilityId(rs.getString("acceptability_id"));
                    r.setConceptActive(rs.getInt("concept_active") == 1);
                    r.setDescriptionActive(rs.getInt("description_active") == 1);
                    list.add(r);
                }
            }
        } catch (Exception e) {
            log.error("getSuggestResultListByDescendantOrSelfIdAndQueryAndSize error", e);
        }
        return list;
    }

    // =========================================================================
    // getSearchResult: 더 이상 ES 사용 안 함 → dummy 반환
    // =========================================================================

    @Override
    public SearchResult getSearchResult(String idx, String q) {
        log.warn("getSearchResult: ES 의존 메서드는 더 이상 지원하지 않습니다. idx={}", idx);
        return null;
    }

    // =========================================================================
    // 내부 헬퍼
    // =========================================================================

    private String buildActiveWhere(StateType state) {
        if (StateType.ACTIVE.equals(state))   return "component_active = 1";
        if (StateType.INACTIVE.equals(state)) return "component_active = 0";
        return "(component_active = 1 OR component_active = 0)";  // BOTH
    }

    private String buildTermWhere(MatchType matchType, String word) {
        if (MatchType.PARTIAL.equals(matchType)) {
            // 단어가 여러 개면 FTS, 하나면 trgm LIKE
            if (word.contains(" ")) {
                return "to_tsvector('english', term) @@ plainto_tsquery('english', ?)";
            }
            return "term ILIKE ?";
        } else if (MatchType.REGEX.equals(matchType)) {
            return "term ~* ?";
        } else {  // FULLTEXT
            return "LOWER(term) = ?";
        }
    }

    private String buildSemanticWhere(List<String> filters) {
        if (filters == null || filters.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("semantic_tag IN (");
        for (int i = 0; i < filters.size(); i++) {
            sb.append(i == 0 ? "?" : ",?");
        }
        sb.append(")");
        return sb.toString();
    }

    /** PreparedStatement에 term 검색 파라미터 설정, 다음 인덱스 반환 */
    private int setTermParam(PreparedStatement ps, int idx, String word,
                              MatchType matchType, boolean idSearch) throws Exception {
        if (idSearch) {
            ps.setString(idx++, word);
        } else if (MatchType.PARTIAL.equals(matchType)) {
            if (word.contains(" ")) {
                ps.setString(idx++, word);   // plainto_tsquery
            } else {
                ps.setString(idx++, "%" + word + "%");  // ILIKE
            }
        } else if (MatchType.REGEX.equals(matchType)) {
            ps.setString(idx++, word);
        } else {  // FULLTEXT
            ps.setString(idx++, word);
        }
        return idx;
    }

    /** 시맨틱 필터 파라미터 설정, 다음 인덱스 반환 */
    private int setSemParams(PreparedStatement ps, int idx, List<String> filters) throws Exception {
        if (filters == null) return idx;
        for (String f : filters) {
            ps.setString(idx++, f);
        }
        return idx;
    }

    /** term 파라미터 다음 인덱스 계산 (시맨틱 파라미터 시작점) */
    private int nextParamIdx(MatchType matchType, boolean idSearch) {
        return 2;  // term 파라미터는 항상 1개
    }

    /** ResultSet → TermSearchResult 변환 */
    private TermSearchResult mapRow(ResultSet rs) throws Exception {
        TermSearchResult r = new TermSearchResult();
        r.setConceptId(rs.getString("concept_id"));
        r.setDescriptionId(rs.getString("description_id"));
        r.setConceptEffectiveTime(rs.getString("concept_effective_time"));
        r.setDescriptionEffectiveTime(rs.getString("description_effective_time"));
        r.setConceptActive(rs.getInt("concept_active") == 1);
        r.setDescriptionActive(rs.getInt("description_active") == 1);
        r.setTerm(rs.getString("term"));
        r.setFsn(rs.getString("fsn"));
        r.setSemanticTag(rs.getString("semantic_tag"));
        r.setLength(rs.getInt("term_length"));
        r.setLang(rs.getString("lang"));
        r.setAcceptabilityId(rs.getString("acceptability_id"));
        int prim = rs.getInt("primitive");
        r.setDefinitionStatusId(prim == 1
                ? SNOMEDCTUtils.DefinitionStatus.Primitive
                : SNOMEDCTUtils.DefinitionStatus.Defined);
        int typeVal = rs.getInt("type");
        // typeId: 1=FSN, 3=Synonym, 4=Definition
        if (typeVal == 1)      r.setTypeId("900000000000003001");
        else if (typeVal == 3) r.setTypeId("900000000000013009");
        else if (typeVal == 4) r.setTypeId("900000000000550004");
        return r;
    }
}
