package co.infoclinic.term.loinc.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.utils.LOINCUtils;
import co.infoclinic.term.loinc.model.converter.LGPConverter;
import co.infoclinic.term.loinc.model.dto.LGPDTO;
import co.infoclinic.term.loinc.model.entity.LGP;
import co.infoclinic.term.loinc.repository.LGPRepository;
import co.infoclinic.term.loinc.service.LGPService;

/**
 * LGP LGP Service
 */
@Service("LGPSvc")
public class LGPServiceImpl implements LGPService {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(LGPService.class);
	
	/** DI: LGP Repository */
	@Autowired
	private LGPRepository LGPRepo;

	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.service.LoincService#getLGPByCode(java.lang.String)
	 */
	@Override
	public LGPDTO getLGPByCode(String code) {
		if (StringUtils.isEmpty(code) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return new LGPDTO();
		}
		
		LGPDTO dto = new LGPDTO();
		
		LGP lgp = LGPRepo.findByLGID(code);
		if (lgp != null) {
			dto = convertToDTO(lgp);
		}
		
		
		return dto;
	}
	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------
	
	private LGPDTO convertToDTO(LGP lgp) {
		return LGPConverter.toDTO(lgp);
	}

}
