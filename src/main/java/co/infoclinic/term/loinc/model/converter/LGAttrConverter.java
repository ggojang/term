package co.infoclinic.term.loinc.model.converter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.loinc.model.entity.LGAttr;
import co.infoclinic.term.loinc.model.dto.LGAttrDTO;

/**
 * LGAttr DTO와 LGAttr간 객체 변환기
 */
@Component(value = "LGAttrCvt")
public final class LGAttrConverter {

	static final Logger log = LoggerFactory.getLogger(LGAttrConverter.class);
 
	
	/**
	 * LGAttr List를 DTO List로 변환
	 * @param srcs
	 * @return
	 */
	public static List<LGAttrDTO> toDTOList(List<LGAttr> lgAttrs) {
		List<LGAttrDTO> dtos = new ArrayList<LGAttrDTO>();
		
		int lgAttrsSize = lgAttrs.size();
		for (int i = 0; i < lgAttrsSize; i++) {
			dtos.add(toDTO(lgAttrs.get(i)));
		}
		
		return dtos;
	}
	
	
	
	/**
	 * LPLINK를 DTO로 변환
	 * @param src
	 * @return
	 */
	public static LGAttrDTO toDTO(LGAttr lgAttr) {
		LGAttrDTO dto = new LGAttrDTO();
		
		dto.setParentLGId(lgAttr.getParentLGId());
		dto.setLGId(lgAttr.getLGId());
		dto.setType(lgAttr.getType());
		dto.setValue(lgAttr.getValue());
		
		return dto;
	}
	
}
