package co.infoclinic.term.loinc.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.utils.LOINCUtils;
import co.infoclinic.term.loinc.model.converter.LGAttrConverter;
import co.infoclinic.term.loinc.model.dto.LGAttrDTO;
import co.infoclinic.term.loinc.model.entity.LGAttr;
import co.infoclinic.term.loinc.repository.LGAttrRepository;
import co.infoclinic.term.loinc.service.LGAttrService;

/**
 * LGAttr Entity Service
 */
@Service("LGAttrSvc")
public class LGAttrServiceImpl implements LGAttrService {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(LGAttrService.class);
	
	/** DI: LGAttr Repository */
	@Autowired
	private LGAttrRepository LGAttrRepo;

	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.service.LoincService#getEntityByCode(java.lang.String)
	 */
	@Override
	public List<LGAttrDTO> getLGAttrListByCode(String code) {
		if (StringUtils.isEmpty(code) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return new ArrayList<LGAttrDTO>();
		}
		
		List<LGAttrDTO> dtos = new ArrayList<LGAttrDTO>();
		
		List<LGAttr> lgAttrs = LGAttrRepo.findListByLGID(code);
		//log.info("ServiceImpl - LGAttrs : " + LGAttrs);
		if (lgAttrs != null) {
			dtos = convertToDTOList(lgAttrs);
		}
		
		return dtos;
	}
	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------
	
	private List<LGAttrDTO> convertToDTOList(List<LGAttr> lgAttrs) {
		return LGAttrConverter.toDTOList(lgAttrs);
	}

}
