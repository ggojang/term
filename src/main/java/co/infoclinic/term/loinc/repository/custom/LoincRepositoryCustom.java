package co.infoclinic.term.loinc.repository.custom;

import java.util.List;

import co.infoclinic.term.loinc.model.entity.Loinc;
import co.infoclinic.term.loinc.utils.PartEnum;

/**
 * Entity Custom Repository
 */
public interface LoincRepositoryCustom {

	/**
	 * 파트의 값을 갖는 Entity 목록 조회
	 * 
	 * @param part
	 * @param value
	 * @return
	 */
	List<Loinc> findListByPartAndValue(PartEnum part, String value, int offset, int limit);
	
	
	/**
	 * 파트의 값을 갖는 Enity 수 조회
	 * @param part
	 * @param value
	 * @return
	 */
	int findCountByPartAndValue(PartEnum part, String value);
}
