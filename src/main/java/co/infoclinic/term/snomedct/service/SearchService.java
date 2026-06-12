package co.infoclinic.term.snomedct.service;

import java.util.List;
import org.springframework.data.domain.Pageable;

import co.infoclinic.term.common.utils.MatchType;
import co.infoclinic.term.common.utils.StateType;
import co.infoclinic.term.snomedct.model.dto.SearchResults;
import co.infoclinic.term.snomedct.model.dto.TermSearchResult;
import io.searchbox.core.SearchResult;  // 하위 호환성 유지; 구현체는 null 반환

/**
 * SNOMED CT 검색 서비스 인터페이스
 */
public interface SearchService {

	
	/**
	 * 용어 검색
	 * 
	 * @param match
	 * @param state
	 * @param semanticFilter
	 * @param q
	 * @param pageable
	 * @return
	 * @throws Exception
	 */
	SearchResults searchTerm(final MatchType match, final StateType state, final List<String> semanticFilter,
			final String q, Pageable pageable) throws Exception;

	
	/**
	 * MRCM Attribute의 Range와 검색어에 대한 결과를 반환하는 메소드 
	 * 
	 * @param attr tig p272에 있는 속성 아이디
	 * @param q 검색어(SCTID 또는 문자열)
	 * @param size 한 번에 반환되는 검색 결과의 최대 개수
	 * 
	 * @return
	 */
	List<TermSearchResult> getValueListByMrcmAttr(String attr, String q, int size);
	
	
	/**
	 * 전체 범위를 대상으로 자동완성 검색 결과를 반환하는 메소드
	 * 
	 * @param q 검색어(SCTID 또는 문자열)
	 * @param size 한 번에 반환되는 검색 결과의 최대 개수
	 * @return
	 */
	List<TermSearchResult> getSuggestResultListByQueryAndSize(String q, int size);

	
	/**
	 * 특정 컨셉 하위를 대상으로 자동완성 검색결과를 반환하는 메소드 
	 * 
	 * @param descendantOrSelfId 검색 범위를 한정하는 식별자(SCTID)
	 * @param q 검색어(SCTID 또는 문자열)
	 * @param size 한 번에 반환되는 검색 결과의 최대 개수
	 * @return
	 */
	List<TermSearchResult> getSuggestResultListByDescendantOrSelfIdAndQueryAndSize(String descendantOrSelfId, String q, int size);

	
	/**
	 * 쿼리의 검색 결과를 반환하는 메소드
	 * 
	 * @param idx
	 * @param q
	 * @return
	 */
	SearchResult getSearchResult(String idx, String q);
}
