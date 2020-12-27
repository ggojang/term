package co.infoclinic.term.loinc.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.utils.LOINCUtils;
import co.infoclinic.term.loinc.model.converter.LPLinkConverter;
import co.infoclinic.term.loinc.model.dto.LPLinkDTO;
import co.infoclinic.term.loinc.model.entity.LPLink;
import co.infoclinic.term.loinc.repository.LPLinkRepository;
import co.infoclinic.term.loinc.service.LPLinkService;

/**
 * LPLink Entity Service
 */
@Service("LPLinkSvc")
public class LPLinkServiceImpl implements LPLinkService {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(LPLinkService.class);
	
	/** DI: LPLink Repository */
	@Autowired
	private LPLinkRepository LPLinkRepo;

	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.service.LoincService#getEntityByCode(java.lang.String)
	 */
	@Override
	public List<LPLinkDTO> getLPLinkListByCode(String code) {
		if (StringUtils.isEmpty(code) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return new ArrayList<LPLinkDTO>();
		}
		
		List<LPLinkDTO> dtos = new ArrayList<LPLinkDTO>();
		
		if (code.contains("LP")) {
			List<LPLink> lplinks = LPLinkRepo.findListByPartId(code);
			//log.info("ServiceImpl - lplinks : " + lplinks);
			if (lplinks != null) {
				dtos = convertToDTOList(lplinks);
			}
		} else {
			List<LPLink> lplinks = LPLinkRepo.findListByCode(code);
			//log.info("ServiceImpl - lplinks : " + lplinks);
			if (lplinks != null) {
				dtos = convertToDTOList(lplinks);
			}
		}
		
		return dtos;
	}
	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------
	
	private List<LPLinkDTO> convertToDTOList(List<LPLink> lplinks) {
		return LPLinkConverter.toDTOList(lplinks);
	}

}
