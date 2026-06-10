package co.infoclinic.term.loinc.service;

import co.infoclinic.term.loinc.model.dto.LGPAttrDTO;

/**
 * LP service
 */
public interface LGPAttrService {
	
	/**
	 * LP의 Entity 조회 
	 * @param LP
	 */
	LGPAttrDTO getLGPAttrByCode(String code);
}
