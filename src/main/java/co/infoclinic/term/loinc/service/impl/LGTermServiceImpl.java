package co.infoclinic.term.loinc.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.utils.LOINCUtils;
import co.infoclinic.term.loinc.model.converter.LGTermConverter;
import co.infoclinic.term.loinc.model.dto.LGTermDTO;
import co.infoclinic.term.loinc.model.entity.LGTerm;
import co.infoclinic.term.loinc.repository.LGTermRepository;
import co.infoclinic.term.loinc.service.LGTermService;

/**
 * LGTerm Entity Service
 */
@Service("LGTermSvc")
public class LGTermServiceImpl implements LGTermService {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(LGTermService.class);
	
	/** DI: LGTerm Repository */
	@Autowired
	private LGTermRepository LGTermRepo;

	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.service.LoincService#getEntityByCode(java.lang.String)
	 */
	@Override
	public List<LGTermDTO> getLGTermListByCode(String code) {
		if (StringUtils.isEmpty(code) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return new ArrayList<LGTermDTO>();
		}
		
		List<LGTermDTO> dtos = new ArrayList<LGTermDTO>();
		
		if (code.contains("LG")) {
			List<LGTerm> lgTerms = LGTermRepo.findListByLGID(code);
			//log.info("ServiceImpl - LGTerms : " + LGTerms);
			if (lgTerms != null) {
				dtos = convertToDTOList(lgTerms);
			}
		} else {
			List<LGTerm> lgTerms = LGTermRepo.findListByCode(code);
			//log.info("ServiceImpl - LGTerms : " + LGTerms);
			if (lgTerms != null) {
				dtos = convertToDTOList(lgTerms);
			}
		}
		
		return dtos;
	}
	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------
	
	private List<LGTermDTO> convertToDTOList(List<LGTerm> lgTerms) {
		return LGTermConverter.toDTOList(lgTerms);
	}

}
