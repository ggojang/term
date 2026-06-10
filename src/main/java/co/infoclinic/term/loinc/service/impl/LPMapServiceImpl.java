package co.infoclinic.term.loinc.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.utils.LOINCUtils;
import co.infoclinic.term.loinc.model.converter.LPMapConverter;
import co.infoclinic.term.loinc.model.dto.LPMapDTO;
import co.infoclinic.term.loinc.model.entity.LPMap;
import co.infoclinic.term.loinc.repository.LPMapRepository;
import co.infoclinic.term.loinc.service.LPMapService;

/**
 * LPMap Entity Service
 */
@Service("LPMapSvc")
public class LPMapServiceImpl implements LPMapService {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(LPMapService.class);
	
	/** DI: LPMap Repository */
	@Autowired
	private LPMapRepository LPMapRepo;

	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.service.LoincService#getEntityByCode(java.lang.String)
	 */
	@Override
	public List<LPMapDTO> getLPMapListByCode(String code) {
		if (StringUtils.isEmpty(code) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return new ArrayList<LPMapDTO>();
		}
		
		List<LPMapDTO> dtos = new ArrayList<LPMapDTO>();
		
		List<LPMap> lpmaps = LPMapRepo.findListByCode(code);
		if (lpmaps != null) {
			dtos = convertToDTOList(lpmaps);
		}
		
		
		return dtos;
	}
	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------
	
	private List<LPMapDTO> convertToDTOList(List<LPMap> lpmaps) {
		return LPMapConverter.toDTOList(lpmaps);
	}

}
