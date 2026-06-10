package co.infoclinic.term.loinc.model.converter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.loinc.model.entity.LPMap;
import co.infoclinic.term.loinc.model.dto.LPMapDTO;
/**
 * LP DTO와 Entity간 객체 변환기
 */
@Component(value = "LPMapCvt")
public final class LPMapConverter {

	static final Logger log = LoggerFactory.getLogger(LPMapConverter.class);
 
	
	/**
	 * LP Entity List를 DTO List로 변환
	 * @param srcs
	 * @return
	 */
	public static List<LPMapDTO> toDTOList(List<LPMap> lpmaps) {
		List<LPMapDTO> dtos = new ArrayList<LPMapDTO>();
		
		int lpmapsSize = lpmaps.size();
		for (int i = 0; i < lpmapsSize; i++) {
			dtos.add(toDTO(lpmaps.get(i)));
		}
		
		return dtos;
	}
	
	
	/**
	 * LP Map Entity를 DTO로 변환
	 * @param src
	 * @return
	 */
	public static LPMapDTO toDTO(LPMap lpmap) {
		LPMapDTO dto = new LPMapDTO();
		
		dto.setPartNumber(lpmap.getPartNumber());
		dto.setPartName(lpmap.getPartName());
		dto.setPartTypeName(lpmap.getPartTypeName());
		dto.setExtCodeId(lpmap.getExtCodeId());
		dto.setExtCodeDisplayName(lpmap.getExtCodeDisplayName());
		dto.setExtCodeSystem(lpmap.getExtCodeSystem());
		dto.setEquivalence(lpmap.getEquivalence());
		dto.setContentOrigin(lpmap.getContentOrigin());
		dto.setExtCodeSystemVersion(lpmap.getExtCodeSystemVersion());
		dto.setExtCodeSystemCopyrightNotice(lpmap.getExtCodeSystemCopyrightNotice());
		
		return dto;
	}
}
