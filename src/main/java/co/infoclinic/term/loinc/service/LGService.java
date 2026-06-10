package co.infoclinic.term.loinc.service;

import java.util.List;

import co.infoclinic.term.loinc.model.dto.LGDTO;

/**
 * LP service
 */
public interface LGService {
	
	/**
	 * LP의 Entity 조회 
	 * @param LP
	 */
	List<LGDTO> getLGListByCode(String code);
}
