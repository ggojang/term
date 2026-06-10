package co.infoclinic.term.loinc.service;

import java.util.List;

import co.infoclinic.term.loinc.model.dto.LinguisticVariantDTO;

/**
 * LP service
 */
public interface LinguisticVariantService {
	
	/**
	 * LP의 Entity 조회 
	 * @param LP
	 */
	List<LinguisticVariantDTO> getLinguisticVariantListByCode(String code);
}
