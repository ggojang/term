package co.infoclinic.term.loinc.service;

import java.util.List;

import co.infoclinic.term.loinc.model.dto.LADTO;

/**
 * LP service
 */
public interface LAService {
	
	/**
	 * LP의 Entity 조회 
	 * @param LP
	 */
	List<LADTO> getLAListByCode(String code);
}
