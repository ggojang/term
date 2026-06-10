package co.infoclinic.term.snomedct.service;

import java.util.List;

import co.infoclinic.term.snomedct.model.dto.DefiningAttributeDTO;
import co.infoclinic.term.snomedct.model.dto.TermSearchResult;

/**
 * MRCM Service
 */
public interface MrcmService {
	
	// ----------------------------------------
	// 조회
	// ----------------------------------------
	
	/**
	 * 
	 * @param conceptId
	 * @return
	 */
	List<DefiningAttributeDTO> getAllowDefiningAttributeList(String conceptId);
	
	
	/**
	 * 
	 * @param conceptId
	 * @return
	 */
	List<DefiningAttributeDTO> getSanctionedAttributeIdNameList(String conceptId);
	
	
	/**
	 * 
	 * @param conceptIdList
	 * @return
	 */
	List<DefiningAttributeDTO> getSanctionedAttributeIdNameList(List<String> conceptIdList);

	
	/**
	 * 
	 * @param attr
	 * @param q
	 * @param size
	 * @return
	 */
	List<TermSearchResult> getValueList(String attr, String q, int size);
}
