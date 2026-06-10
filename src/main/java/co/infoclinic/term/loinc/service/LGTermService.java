package co.infoclinic.term.loinc.service;

import java.util.List;

import co.infoclinic.term.loinc.model.dto.LGTermDTO;

/**
 * LGTerm service
 */
public interface LGTermService {
	
	/**
	 * LGTerm의 Entity 조회 
	 * @param LGTerm
	 */
	
	List<LGTermDTO> getLGTermListByCode(String code);
}
