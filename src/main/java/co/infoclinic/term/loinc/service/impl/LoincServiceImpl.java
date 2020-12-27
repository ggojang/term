package co.infoclinic.term.loinc.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.model.dto.SubsumptionTestDTO;
import co.infoclinic.term.common.utils.LOINCUtils;
import co.infoclinic.term.loinc.model.converter.LoincConverter;
import co.infoclinic.term.loinc.model.dto.LoincDTO;
import co.infoclinic.term.loinc.model.entity.Loinc;
import co.infoclinic.term.loinc.repository.LoincRepository;
import co.infoclinic.term.loinc.service.HierarchyService;
import co.infoclinic.term.loinc.service.LoincService;
import co.infoclinic.term.loinc.utils.PartEnum;

/**
 * Loinc Entity Service
 */
@Service("LncEntitySvc")
public class LoincServiceImpl implements LoincService {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(LoincService.class);
	
	/** DI: Loinc Repository */
	@Autowired
	private LoincRepository loincRepo;
	
	@Autowired
	private HierarchyService hierSvc;

	
	
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.service.LoincService#getVersion()
	 */
	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "2.67";
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.service.LoincService#getEntityByCode(java.lang.String)
	 */
	@Override
	public LoincDTO getEntityByCode(String code) {
		if (StringUtils.isEmpty(code) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return new LoincDTO();
		}
		
		LoincDTO dto = new LoincDTO();
		
		Loinc entity = loincRepo.findByCode(code);
		if (entity != null) {
			dto = convertToDTO(entity);
		}
		
		
		return dto;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.service.LoincService#getEntityList(co.infoclinic.term.loinc.utils.PartEnum, java.lang.String, int, int)
	 */
	@Override
	public Page<LoincDTO> getEntityList(PartEnum part, String value, int page, int size) {
		if (page < 1 || size < 1) {
			return new PageImpl<LoincDTO>(new ArrayList<LoincDTO>());
		}
		
		List<LoincDTO> dtos = new ArrayList<LoincDTO>();
		
		int offset = (page - 1) * size;
		int limit = size;
		int totalSize = loincRepo.findCountByPartAndValue(part, value);
		if (totalSize > 0) {
			List<Loinc> entities = loincRepo.findListByPartAndValue(part, value, offset, limit);
			if (entities != null) {
				dtos = convertToDTOList(entities);
			}
		}
		return new PageImpl<LoincDTO>(dtos, new PageRequest(page - 1, size), totalSize);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.service.LoincService#subsumptionTest(java.lang.String, java.lang.String)
	 */
	@Override
	public SubsumptionTestDTO subsumptionTest(String criteriaCode, String code) {
		SubsumptionTestDTO dto = new SubsumptionTestDTO();
		dto.setCriteriaId(criteriaCode);
		dto.setConceptId(code);
		
		if (StringUtils.isEmpty(criteriaCode) || StringUtils.isEmpty(code) || criteriaCode.matches(LOINCUtils.CODE_PATTERN) || code.matches(LOINCUtils.CODE_PATTERN)) {
			return dto;
		}
		
		// 포함여부 조회
		boolean isSubsumes = hierSvc.isSubsumptionTest(criteriaCode, code) > 0 ? true:false;
		dto.setResult(isSubsumes);
		
		return dto;
	}
	
	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------

	private List<LoincDTO> convertToDTOList(List<Loinc> entities) {
		return LoincConverter.toDTOList(entities);
	}
	
	private LoincDTO convertToDTO(Loinc entity) {
		return LoincConverter.toDTO(entity);
	}

}
