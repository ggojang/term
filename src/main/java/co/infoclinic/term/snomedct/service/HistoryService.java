package co.infoclinic.term.snomedct.service;

import java.util.List;

import co.infoclinic.term.snomedct.model.dto.ComponentDTO;

/**
 * History Service
 */
public interface HistoryService {

	// ----------------------------------------
	// 조회
	// ----------------------------------------
	
	/**
	 * 
	 * @param componentId
	 * @param effectiveTime
	 * @return
	 */
	List<ComponentDTO> getHistory(String componentId, String effectiveTime);
}
