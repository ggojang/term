package co.infoclinic.term.snomedct.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.utils.SNOMEDCTComponentTypeEnum;
import co.infoclinic.term.snomedct.model.dto.ComponentDTO;
import co.infoclinic.term.snomedct.model.dto.ConceptViewDTO;
import co.infoclinic.term.snomedct.model.dto.DescriptionDTO;
import co.infoclinic.term.snomedct.service.ConceptService;
import co.infoclinic.term.snomedct.service.DescriptionService;
import co.infoclinic.term.snomedct.service.HistoryService;

/**
 * History Service Implementation
 * 
 * @author dongwon
 *
 */
@Service
public class HistoryServiceImpl implements HistoryService {
  
  Logger log = LoggerFactory.getLogger(HistoryServiceImpl.class);

  @Autowired
  private ConceptService conceptService;
  @Autowired
  private DescriptionService descriptionService;
 

  /*
   * componentId와 effectiveTime으로 History Data를 가져오는 메소드
   * 
   * (non-Javadoc)
   * @see com.infoclinic.infoterm.snomedct.services.history.service.HistoryService#getHistory(java.lang.String, java.lang.String)
   */
  @Override
  public List<ComponentDTO> getHistory(String componentId, String effectiveTime) {
	// SCTID 규칙을 따르지 않는 경우 반환
	if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(componentId)) {
      return new ArrayList<ComponentDTO>();
    }
    
    // 변수 선언
    List<ComponentDTO> histories = new ArrayList<ComponentDTO>();
    
    // valid check
    if (SNOMEDCTComponentTypeEnum.isValidIdentifier(componentId)) {
      histories = new ArrayList<ComponentDTO>();
      List<ConceptViewDTO> conceptViewDtoList = null;
      List<DescriptionDTO> descriptionDtoList = null;
      SNOMEDCTComponentTypeEnum sctCpntType = SNOMEDCTComponentTypeEnum.getById(componentId);
      switch (sctCpntType) {
        case CONCEPT:
          descriptionDtoList = descriptionService.getDescriptionList(componentId, effectiveTime);
          histories.addAll(descriptionDtoList);
          
          conceptViewDtoList = conceptService.getConceptList(componentId, effectiveTime);
          histories.addAll(conceptViewDtoList);
          break;
        case DESCRIPTION:
          descriptionDtoList = descriptionService.getDescriptionList(componentId, effectiveTime);
          histories.addAll(descriptionDtoList);
          break;
        case RELATIONSHIP:
          // TODO implements...
          //dtoList = relationshipService.getViewDTOList(sctId, effectiveTime);
          break;
        default:
          // TODO
          break;
      }
    }
    
    return histories;
  }
  
}