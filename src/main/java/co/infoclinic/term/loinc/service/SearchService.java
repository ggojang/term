package co.infoclinic.term.loinc.service;

import org.springframework.data.domain.Page;

import co.infoclinic.term.loinc.model.dto.SearchResultDTO;

/**
 * The Search Service
 */
public interface SearchService {

	/**
	 * 검색 - 코드 또는 FSN의 일부 용어에 대한 검색결과를 반환
	 * 
	 * 코드: 검색어가 10자리 이하이면서, 대시(-)가 포함되어 있고 대시(-)뒤 한자리 숫자인 경우
	 * 용어: 나머지
	 * 
	 * @param q 검색어
	 * @param page
	 * @param size
	 */
	Page<SearchResultDTO> searchByWord(String word, int page, int size);
	
	
	/**
	 * 특정 필드 검색
	 * 
	 * @param q
	 * @param field
	 * @param page
	 * @param size
	 * @return
	 */
	Page<SearchResultDTO> searchByWordAndField(String word, String field, int page, int size);

}
