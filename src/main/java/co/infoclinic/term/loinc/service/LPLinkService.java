package co.infoclinic.term.loinc.service;

import java.util.List;

import co.infoclinic.term.loinc.model.dto.LPLinkDTO;

/**
 * LPLink service
 */
public interface LPLinkService {
	
	/**
	 * LPLink의 Entity 조회 
	 * @param LPLink
	 */
	List<LPLinkDTO> getLPLinkListByCode(String code);
}
