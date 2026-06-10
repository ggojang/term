package co.infoclinic.term.loinc.service;

import co.infoclinic.term.loinc.model.dto.LGPDTO;

/**
 * LP service
 */
public interface LGPService {
	
	/**
	 * LP의 Entity 조회 
	 * @param LP
	 */
	LGPDTO getLGPByCode(String code);
}
