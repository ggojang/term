package co.infoclinic.term.loinc.repository.custom;

import java.util.List;

import co.infoclinic.term.loinc.model.entity.LP;


/**
 * LP Custom Repository
 */
public interface LPRepositoryCustom {

	/**
	 * 파트의 값을 갖는 LP 목록 조회
	 * 
	 * @param LP
	 * @return
	 */
	List<LP> findListByCode(String code, int offset, int limit);
	
	
	/**
	 * 파트의 값을 갖는 LP 수 조회
	 * @param LP
	 * @return
	 */
	int findCountByValue(String value);
}
