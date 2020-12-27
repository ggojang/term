package co.infoclinic.term.loinc.model.converter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.loinc.model.entity.LGPAttr;
import co.infoclinic.term.loinc.model.dto.LGPAttrDTO;

/**
 * LGPAttr DTO와 LGPAttr간 객체 변환기
 */
@Component(value = "LGPAttrCvt")
public final class LGPAttrConverter {

	static final Logger log = LoggerFactory.getLogger(LGPAttrConverter.class);
 
	
	/**
	 * LGPAttr List를 DTO List로 변환
	 * @param srcs
	 * @return
	 */
	public static List<LGPAttrDTO> toDTOList(List<LGPAttr> LGPAttrs) {
		List<LGPAttrDTO> dtos = new ArrayList<LGPAttrDTO>();
		
		int LGPAttrsSize = LGPAttrs.size();
		for (int i = 0; i < LGPAttrsSize; i++) {
			dtos.add(toDTO(LGPAttrs.get(i)));
		}
		
		return dtos;
	}
	
	
	
	/**
	 * LPLINK를 DTO로 변환
	 * @param src
	 * @return
	 */
	public static LGPAttrDTO toDTO(LGPAttr LGPAttr) {
		LGPAttrDTO dto = new LGPAttrDTO();
		
		dto.setParentLGId(LGPAttr.getParentLGId());
		dto.setType(LGPAttr.getType());
		dto.setValue(LGPAttr.getValue());
		
		return dto;
	}
	
}
