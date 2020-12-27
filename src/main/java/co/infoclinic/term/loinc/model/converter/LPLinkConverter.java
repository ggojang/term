package co.infoclinic.term.loinc.model.converter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.loinc.model.entity.LPLink;
import co.infoclinic.term.loinc.model.dto.LPLinkDTO;
/**
 * LP DTO와 Entity간 객체 변환기
 */
@Component(value = "LPLinkCvt")
public final class LPLinkConverter {

	static final Logger log = LoggerFactory.getLogger(LPLinkConverter.class);
 
	
	/**
	 * LP Entity List를 DTO List로 변환
	 * @param srcs
	 * @return
	 */
	public static List<LPLinkDTO> toDTOList(List<LPLink> lplinks) {
		List<LPLinkDTO> dtos = new ArrayList<LPLinkDTO>();
		
		int lplinksSize = lplinks.size();
		for (int i = 0; i < lplinksSize; i++) {
			dtos.add(toDTO(lplinks.get(i)));
		}
		//System.out.println("Convert - dtos : " + dtos);
		return dtos;
	}
	
	
	
	/**
	 * LP Link Entity를 DTO로 변환
	 * @param src
	 * @return
	 */
	public static LPLinkDTO toDTO(LPLink lplink) {
		LPLinkDTO dto = new LPLinkDTO();
		
		dto.setLoincNumber(lplink.getLoincNumber());
		dto.setLongCommonName(lplink.getLongCommonName());
		dto.setPartNumber(lplink.getPartNumber());
		dto.setPartName(lplink.getPartName());
		dto.setPartCodeSystem(lplink.getPartCodeSystem());
		dto.setPartTypeName(lplink.getPartTypeName());
		dto.setLinkTypeName(lplink.getLinkTypeName());
		dto.setProperty(lplink.getProperty());
		
		return dto;
	}
}
