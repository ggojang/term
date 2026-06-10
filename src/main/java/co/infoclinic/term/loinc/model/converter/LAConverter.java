package co.infoclinic.term.loinc.model.converter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.loinc.model.entity.LA;
import co.infoclinic.term.loinc.model.dto.LAContentDTO;
/**
 * LP DTO와 Entity간 객체 변환기
 */
@Component(value = "LACvt")
public final class LAConverter {

	static final Logger log = LoggerFactory.getLogger(LAConverter.class);
 
	
	/**
	 * LA List를 DTO List로 변환
	 * @param srcs
	 * @return
	 */
	public static List<LAContentDTO> toDTOList(List<LA> las) {
		List<LAContentDTO> dtos = new ArrayList<LAContentDTO>();
		
		int lasSize = las.size();
		for (int i = 0; i < lasSize; i++) {
			dtos.add(toDTO(las.get(i)));
		}
		//System.out.println("Convert - dtos : " + dtos);
		return dtos;
	}
	
	
	
	/**
	 * LA를 DTO로 변환
	 * @param src
	 * @return
	 */
	public static LAContentDTO toDTO(LA la) {
		LAContentDTO dto = new LAContentDTO();
		
		dto.setLAID(la.getLAID());
		dto.setLAName(la.getLAName());
		dto.setLAOid(la.getLAOid());
		dto.setExtDefinedYn(la.getExtDefinedYn());
		dto.setExtDefinedLACodeSystem(la.getExtDefinedLACodeSystem());
		dto.setExtDefinedLALink(la.getExtDefinedLALink());
		dto.setAnswerStringID(la.getAnswerStringID());
		dto.setLocalAnswerCode(la.getLocalAnswerCode());
		dto.setLocalAnswerCodeSystem(la.getLocalAnswerCodeSystem());
		dto.setSequenceNumber(la.getSequenceNumber());
		dto.setDisplayText(la.getDisplayText());
		dto.setExtCodeID(la.getExtCodeID());
		dto.setExtCodeDisplayName(la.getExtCodeDisplayName());
		dto.setExtCodeSystem(la.getExtCodeSystem());
		dto.setExtCodeSystemVersion(la.getExtCodeSystemVersion());
		dto.setExtCodeSystemCopyrightNotice(la.getExtCodeSystemCopyrightNotice());
		dto.setSubsequenceTextPrompt(la.getSubsequenceTextPrompt());
		dto.setDescription(la.getDescription());
		dto.setScore(la.getScore());
		
		return dto;
	}
}
