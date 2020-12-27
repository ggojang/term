package co.infoclinic.term.loinc.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.utils.LOINCUtils;
import co.infoclinic.term.loinc.model.converter.LinguisticVariantConverter;
import co.infoclinic.term.loinc.model.dto.LinguisticVariantDTO;
import co.infoclinic.term.loinc.model.entity.LinguisticVariant;
import co.infoclinic.term.loinc.repository.LinguisticVariantRepository;
import co.infoclinic.term.loinc.service.LinguisticVariantService;

/**
 * LinguisticVariant Entity Service
 */
@Service("LinguisticVariantSvc")
public class LinguisticVariantServiceImpl implements LinguisticVariantService {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(LinguisticVariantService.class);
	
	/** DI: LinguisticVariant Repository */
	@Autowired
	private LinguisticVariantRepository LinguisticVariantRepo;

	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.service.LoincService#getEntityByCode(java.lang.String)
	 */
	@Override
	public List<LinguisticVariantDTO> getLinguisticVariantListByCode(String code) {
		if (StringUtils.isEmpty(code) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return new ArrayList<LinguisticVariantDTO>();
		}
		
		List<LinguisticVariantDTO> dtos = new ArrayList<LinguisticVariantDTO>();
		
		List<LinguisticVariant> LinguisticVariants = LinguisticVariantRepo.findListByCode(code);
		//log.info("ServiceImpl - LinguisticVariants : " + LinguisticVariants);
		if (LinguisticVariants != null) {
			dtos = convertToDTOList(LinguisticVariants);
		}
		
		return dtos;
	}
	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------
	
	private List<LinguisticVariantDTO> convertToDTOList(List<LinguisticVariant> LinguisticVariants) {
		return LinguisticVariantConverter.toDTOList(LinguisticVariants);
	}

}
