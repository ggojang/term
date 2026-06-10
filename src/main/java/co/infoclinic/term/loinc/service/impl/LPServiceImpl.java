package co.infoclinic.term.loinc.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.utils.LOINCUtils;
import co.infoclinic.term.loinc.model.converter.LPConverter;
import co.infoclinic.term.loinc.model.dto.LPDTO;
import co.infoclinic.term.loinc.model.entity.LP;
import co.infoclinic.term.loinc.repository.LPRepository;
import co.infoclinic.term.loinc.service.LPService;

/**
 * LP Entity Service
 */
@Service("LPSvc")
public class LPServiceImpl implements LPService {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(LPService.class);
	
	/** DI: LP Repository */
	@Autowired
	private LPRepository lpRepo;

	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.service.LoincService#getEntityByCode(java.lang.String)
	 */
	@Override
	public LPDTO getLPByCode(String code) {
		if (StringUtils.isEmpty(code) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return new LPDTO();
		}
		
		LPDTO dto = new LPDTO();
		
		LP lp = lpRepo.findByCode(code);
		if (lp != null) {
			dto = convertToDTO(lp);
		}
		
		
		return dto;
	}
	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------
	
	private LPDTO convertToDTO(LP lp) {
		return LPConverter.toDTO(lp);
	}

}
