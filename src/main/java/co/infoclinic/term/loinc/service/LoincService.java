package co.infoclinic.term.loinc.service;

import org.springframework.data.domain.Page;

import co.infoclinic.term.common.model.dto.SubsumptionTestDTO;
import co.infoclinic.term.loinc.model.dto.LoincDTO;
import co.infoclinic.term.loinc.utils.PartEnum;

/**
 * Loinc service
 */
public interface LoincService {
	
	/**
	 * LOINC 버전 조회
	 */
	String getVersion();

	
	/**
	 * LOINC의 Entity 조회 
	 * @param code
	 */
	LoincDTO getEntityByCode(String code);

	
	/**
	 * LOINC의 Entity 목록 조회
	 * 
	 * @param part
	 * @param value
	 * @param page
	 * @param size
	 * @return
	 */
	Page<LoincDTO> getEntityList(PartEnum part, String value, int page, int size);


	/**
	 * 포함관계 조회
	 * 
	 * @param criteriaId
	 * @param conceptId
	 * @return
	 */
	SubsumptionTestDTO subsumptionTest(String criteriaCode, String code);
}
