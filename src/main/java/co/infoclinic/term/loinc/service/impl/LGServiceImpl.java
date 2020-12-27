package co.infoclinic.term.loinc.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import co.infoclinic.term.common.utils.LOINCUtils;
import co.infoclinic.term.loinc.model.converter.LGConverter;
import co.infoclinic.term.loinc.model.dto.LGDTO;
import co.infoclinic.term.loinc.model.entity.LG;
import co.infoclinic.term.loinc.repository.LGRepository;
import co.infoclinic.term.loinc.service.LGService;

/**
 * LG lg Service
 */
@Service("LGSvc")
public class LGServiceImpl implements LGService {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(LGService.class);
	
	/** DI: LG Repository */
	@Autowired
	private LGRepository LGRepo;

	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.service.LoincService#getlgByCode(java.lang.String)
	 */
	@Override
	public List<LGDTO> getLGListByCode(String code) {
		if (StringUtils.isEmpty(code) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return new ArrayList<LGDTO>();
		}
		
		List<LGDTO> dtos = new ArrayList<LGDTO>();
		
		List<LG> lgs = LGRepo.findListByLGID(code);
		if (lgs != null) {
			dtos = convertToDTOList(lgs);
		}
		
		
		return dtos;
	}
	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------
	
	private List<LGDTO> convertToDTOList(List<LG> lgs) {
		return LGConverter.toDTOList(lgs);
	}

}
