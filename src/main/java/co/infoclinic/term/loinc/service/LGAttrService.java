package co.infoclinic.term.loinc.service;

import java.util.List;

import co.infoclinic.term.loinc.model.dto.LGAttrDTO;

/**
 * LGAttr service
 */
public interface LGAttrService {
	
	/**
	 * LGAttr의 Entity 조회 
	 * @param LGAttr
	 */
	List<LGAttrDTO> getLGAttrListByCode(String code);
}
