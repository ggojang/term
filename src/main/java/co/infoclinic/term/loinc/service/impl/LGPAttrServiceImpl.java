package co.infoclinic.term.loinc.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.utils.LOINCUtils;
import co.infoclinic.term.loinc.model.converter.LGPAttrConverter;
import co.infoclinic.term.loinc.model.dto.LGPAttrDTO;
import co.infoclinic.term.loinc.model.entity.LGPAttr;
import co.infoclinic.term.loinc.repository.LGPAttrRepository;
import co.infoclinic.term.loinc.service.LGPAttrService;

/**
 * LGPAttr LGPAttr Service
 */
@Service("LGPAttrSvc")
public class LGPAttrServiceImpl implements LGPAttrService {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(LGPAttrService.class);
	
	/** DI: LGPAttr Repository */
	@Autowired
	private LGPAttrRepository LGPAttrRepo;

	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.service.LoincService#getLGPAttrByCode(java.lang.String)
	 */
	@Override
	public LGPAttrDTO getLGPAttrByCode(String code) {
		if (StringUtils.isEmpty(code) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return new LGPAttrDTO();
		}
		
		LGPAttrDTO dto = new LGPAttrDTO();
		
		LGPAttr lgpAttr = LGPAttrRepo.findByLGID(code);
		if (lgpAttr != null) {
			dto = convertToDTO(lgpAttr);
		}
		
		
		return dto;
	}
	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------
	
	private LGPAttrDTO convertToDTO(LGPAttr lgpAttr) {
		return LGPAttrConverter.toDTO(lgpAttr);
	}

}
