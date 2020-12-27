package co.infoclinic.term.icd10.service;

import org.springframework.data.domain.Page;

import co.infoclinic.term.icd10.model.dto.Icd10SearchResultDTO;

/**
 * The Search Service
 */
public interface Icd10SearchService {

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
	Page<Icd10SearchResultDTO> searchByWord(String word, int page, int size);

}