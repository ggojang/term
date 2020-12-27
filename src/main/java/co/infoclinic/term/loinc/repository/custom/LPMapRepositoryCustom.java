package co.infoclinic.term.loinc.repository.custom;

import java.util.List;

import co.infoclinic.term.loinc.model.entity.LPMap;


/**
 * LPMap Custom Repository
 */
public interface LPMapRepositoryCustom {

	/**
	 * 파트의 값을 갖는 LPMap 목록 조회
	 * 
	 * @param code
	 * @return
	 */
	List<LPMap> findListByCode(String code, int offset, int limit);
	
	
	/**
	 * 파트의 값을 갖는 LPMap 수 조회
	 * @param LPMap
	 * @return
	 */
	int findCountByValue(String value);
}
