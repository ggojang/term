package co.infoclinic.term.loinc.service;

import java.util.List;

import co.infoclinic.term.loinc.model.dto.LPMapDTO;

/**
 * LPMap service
 */
public interface LPMapService {
	
	/**
	 * LPMap의 Entity 조회 
	 * @param LPMap
	 */
	List<LPMapDTO> getLPMapListByCode(String code);

}
