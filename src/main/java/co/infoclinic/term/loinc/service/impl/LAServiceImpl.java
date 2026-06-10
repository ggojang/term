package co.infoclinic.term.loinc.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.utils.LOINCUtils;
import co.infoclinic.term.loinc.model.dto.LADTO;
import co.infoclinic.term.loinc.model.dto.LAContentDTO;
import co.infoclinic.term.loinc.model.entity.LA;
import co.infoclinic.term.loinc.model.entity.LALink;
import co.infoclinic.term.loinc.repository.LARepository;
import co.infoclinic.term.loinc.repository.LALinkRepository;
import co.infoclinic.term.loinc.service.LAService;

/**
 * LA Entity Service
 */
@Service("LASvc")
public class LAServiceImpl implements LAService {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(LAService.class);
	
	/** DI: LA Repository */
	@Autowired
	private LARepository LARepo;
	
	@Autowired
	private LALinkRepository LALinkRepo;

	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.service.LoincService#getEntityByCode(java.lang.String)
	 */
	@Override
	public List<LADTO> getLAListByCode(String code) {
		if (StringUtils.isEmpty(code) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return new ArrayList<LADTO>();
		}
		
		List<LADTO> dtos = new ArrayList<LADTO>();
		//List<LALinkDTO> linkDtos = new ArrayList<LALinkDTO>();
		
		
		List<LALink> laLinks = LALinkRepo.findListByCode(code);
		
		
		if (laLinks.size() != 0) {
			
			for (int i=0; i < laLinks.size(); i++) {
				//System.out.println(laLinks.size() + ", laLinks.get[" + i +"] : " + laLinks.get(i));
				dtos.add(toLALinkListDTO(laLinks.get(i)));	
			}
				
		}
		
		return dtos;
	}
	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------
	
	private LADTO toLALinkListDTO(LALink laLink) {
		LADTO dto = new LADTO();
		
		dto.setLoincNumber(laLink.getLoincNumber());
		dto.setLongCommonName(laLink.getLongCommonName());
		dto.setLAID(laLink.getLAID());
		dto.setLAName(laLink.getLAName());
		dto.setLALinkType(laLink.getLALinkType());
		dto.setApplicableContext(laLink.getApplicableContext());
		
		List<LA> laContents = new ArrayList<LA>();
		
		laContents = LARepo.findListByCode(laLink.getLAID());
		
		if (laContents.size() != 0) {
			dto.setLa(toLAContentListDTO(laContents));
		}
		
		return dto;
	}
	
	private List<LAContentDTO> toLAContentListDTO(List<LA> laContents) {
	
		List<LAContentDTO> las = new ArrayList<LAContentDTO>();
		
		for (int i=0; i < laContents.size(); i++) {
			//System.out.println(laContents.size() + ", laContents.get[" + i +"] : " + laContents.get(i));

			LAContentDTO dto = new LAContentDTO();
		
			dto.setLAID(laContents.get(i).getLAID());
			dto.setLAName(laContents.get(i).getLAName());
			dto.setLAOid(laContents.get(i).getLAOid());
			dto.setExtDefinedYn(laContents.get(i).getExtDefinedYn());
			dto.setExtDefinedLACodeSystem(laContents.get(i).getExtDefinedLACodeSystem());
			dto.setExtDefinedLALink(laContents.get(i).getExtDefinedLALink());
			dto.setAnswerStringID(laContents.get(i).getAnswerStringID());
			dto.setLocalAnswerCode(laContents.get(i).getLocalAnswerCode());
			dto.setLocalAnswerCodeSystem(laContents.get(i).getLocalAnswerCodeSystem());
			dto.setSequenceNumber(laContents.get(i).getSequenceNumber());
			dto.setDisplayText(laContents.get(i).getDisplayText());
			dto.setExtCodeID(laContents.get(i).getExtCodeID());
			dto.setExtCodeDisplayName(laContents.get(i).getExtCodeDisplayName());
			dto.setExtCodeSystem(laContents.get(i).getExtCodeSystem());
			dto.setExtCodeSystemVersion(laContents.get(i).getExtCodeSystemVersion());
			dto.setExtCodeSystemCopyrightNotice(laContents.get(i).getExtCodeSystemCopyrightNotice());
			dto.setSubsequenceTextPrompt(laContents.get(i).getSubsequenceTextPrompt());
			dto.setDescription(laContents.get(i).getDescription());
			dto.setScore(laContents.get(i).getScore());
		
			las.add(dto);
		}
		return las;
	}

}
