package co.infoclinic.term.loinc.model.converter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.loinc.model.entity.LP;
//import co.infoclinic.term.loinc.model.entity.LPLink;
//import co.infoclinic.term.loinc.model.entity.LPMap;
import co.infoclinic.term.loinc.model.dto.LPDTO;

/**
 * LPLINK DTO와 LPLINK간 객체 변환기
 */
@Component(value = "LPCvt")
public final class LPConverter {

	static final Logger log = LoggerFactory.getLogger(LPConverter.class);
 
	
	/**
	 * LPLINK List를 DTO List로 변환
	 * @param srcs
	 * @return
	 */
	public static List<LPDTO> toDTOList(List<LP> entities) {
		List<LPDTO> dtos = new ArrayList<LPDTO>();
		
		int srcsSize = entities.size();
		for (int i = 0; i < srcsSize; i++) {
			dtos.add(toDTO(entities.get(i)));
		}
		
		return dtos;
	}
	
	
	
	/**
	 * LPLINK를 DTO로 변환
	 * @param src
	 * @return
	 */
	public static LPDTO toDTO(LP entity) {
		LPDTO dto = new LPDTO();
		
		dto.setPartNumber(entity.getPartNumber());
		dto.setPartTypeName(entity.getPartTypeName());
		dto.setPartName(entity.getPartName());
		dto.setPartDisplayName(entity.getPartDisplayName());
		dto.setStatus(entity.getStatus());
		
		return dto;
	}
	
}
