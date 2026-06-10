package co.infoclinic.term.loinc.service;

import co.infoclinic.term.loinc.model.dto.LPDTO;

/**
 * LP service
 */
public interface LPService {
	
	/**
	 * LP의 Entity 조회 
	 * @param LP
	 */
	LPDTO getLPByCode(String code);
}
