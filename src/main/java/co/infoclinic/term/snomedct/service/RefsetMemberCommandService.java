package co.infoclinic.term.snomedct.service;

import co.infoclinic.term.snomedct.model.dto.RefsetMemberCmdDTO;

/**
 * Referenceset Member command service
 */
public interface RefsetMemberCommandService {
	
	/**
	 * 
	 * @param refsetId
	 * @param dto
	 * @return
	 */
	boolean addMemberList(String refsetId, RefsetMemberCmdDTO dto);
}
