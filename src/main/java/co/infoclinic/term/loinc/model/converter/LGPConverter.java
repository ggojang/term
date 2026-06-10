package co.infoclinic.term.loinc.model.converter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.loinc.model.entity.LGP;
import co.infoclinic.term.loinc.model.dto.LGPDTO;

/**
 * LGP DTO와 LGP간 객체 변환기
 */
@Component(value = "LGPCvt")
public final class LGPConverter {

	static final Logger log = LoggerFactory.getLogger(LGPConverter.class);
 
	
	/**
	 * LGP List를 DTO List로 변환
	 * @param srcs
	 * @return
	 */
	public static List<LGPDTO> toDTOList(List<LGP> LGPs) {
		List<LGPDTO> dtos = new ArrayList<LGPDTO>();
		
		int LGPsSize = LGPs.size();
		for (int i = 0; i < LGPsSize; i++) {
			dtos.add(toDTO(LGPs.get(i)));
		}
		
		return dtos;
	}
	
	
	
	/**
	 * LPLINK를 DTO로 변환
	 * @param src
	 * @return
	 */
	public static LGPDTO toDTO(LGP LGP) {
		LGPDTO dto = new LGPDTO();
		
		dto.setParentLGId(LGP.getParentLGId());
		dto.setParentLG(LGP.getParentLG());
		dto.setStatus(LGP.getStatus());
		
		return dto;
	}
	
}
