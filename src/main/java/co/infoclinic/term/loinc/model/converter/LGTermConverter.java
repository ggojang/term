package co.infoclinic.term.loinc.model.converter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.loinc.model.entity.LGTerm;
import co.infoclinic.term.loinc.model.dto.LGTermDTO;

/**
 * LGTerm DTO와 LGTerm간 객체 변환기
 */
@Component(value = "LGTermCvt")
public final class LGTermConverter {

	static final Logger log = LoggerFactory.getLogger(LGTermConverter.class);
 
	
	/**
	 * LGTerm List를 DTO List로 변환
	 * @param srcs
	 * @return
	 */
	public static List<LGTermDTO> toDTOList(List<LGTerm> lgTerms) {
		List<LGTermDTO> dtos = new ArrayList<LGTermDTO>();
		
		int lgTermsSize = lgTerms.size();
		for (int i = 0; i < lgTermsSize; i++) {
			dtos.add(toDTO(lgTerms.get(i)));
		}
		
		return dtos;
	}
	
	
	
	/**
	 * LPLINK를 DTO로 변환
	 * @param src
	 * @return
	 */
	public static LGTermDTO toDTO(LGTerm lgTerm) {
		LGTermDTO dto = new LGTermDTO();
		
		dto.setCategory(lgTerm.getCategory());
		dto.setLGId(lgTerm.getLGId());
		dto.setArchetype(lgTerm.getArchetype());
		dto.setLoincNumber(lgTerm.getLoincNumber());
		dto.setLongCommonName(lgTerm.getLongCommonName());
		dto.setLGIdName(lgTerm.getLGIdName());
		return dto;
	}
	
}
