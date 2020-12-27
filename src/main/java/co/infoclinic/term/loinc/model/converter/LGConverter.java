package co.infoclinic.term.loinc.model.converter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.loinc.model.entity.LG;
import co.infoclinic.term.loinc.model.dto.LGDTO;

/**
 * LG DTO와 LG간 객체 변환기
 */
@Component(value = "LGCvt")
public final class LGConverter {

	static final Logger log = LoggerFactory.getLogger(LGConverter.class);
 
	
	/**
	 * LG List를 DTO List로 변환
	 * @param srcs
	 * @return
	 */
	public static List<LGDTO> toDTOList(List<LG> lgs) {
		List<LGDTO> dtos = new ArrayList<LGDTO>();
		
		int lgsSize = lgs.size();
		for (int i = 0; i < lgsSize; i++) {
			dtos.add(toDTO(lgs.get(i)));
		}
		
		return dtos;
	}
	
	
	
	/**
	 * LPLINK를 DTO로 변환
	 * @param src
	 * @return
	 */
	public static LGDTO toDTO(LG lg) {
		LGDTO dto = new LGDTO();
		
		dto.setParentLGId(lg.getParentLGId());
		dto.setLGId(lg.getLGId());
		dto.setLG(lg.getLG());
		dto.setArchetype(lg.getArchetype());
		dto.setStatus(lg.getStatus());
		dto.setVersionFirstReleased(lg.getVersionFirstReleased());
		return dto;
	}
	
}
