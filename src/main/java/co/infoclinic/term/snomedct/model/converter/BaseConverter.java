package co.infoclinic.term.snomedct.model.converter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import co.infoclinic.term.common.model.dto.Value;
import co.infoclinic.term.common.utils.SNOMEDCTComponentTypeEnum;
import co.infoclinic.term.snomedct.model.dto.RefsetMemberViewDTO;
import co.infoclinic.term.snomedct.model.entity.Description;
import co.infoclinic.term.snomedct.service.DescriptionService;
import co.infoclinic.term.snomedct.service.RefsetMemberService;

public abstract class BaseConverter {
  
  static Logger log = LoggerFactory.getLogger(BaseConverter.class);
  
  private static DescriptionService descriptionService;
  private static RefsetMemberService referencesetMemberService;

  @Autowired
  private DescriptionService virtualDescriptionService;

  @Autowired
  private RefsetMemberService virtualReferencesetMemberService;
  
  @PostConstruct
  protected void init() {
    BaseConverter.descriptionService = virtualDescriptionService;
    BaseConverter.referencesetMemberService = virtualReferencesetMemberService;
  }

  /**
   * 
   * Duplicate : ReferenceSetMapper.getTerm
   * 
   * @param id
   * @param type
   * @return
   */
  protected static void addToComponentIdByType(List<String> conceptFieldIds,
      List<String> descriptionFieldIds, String componentId) {
    boolean isConcept =
        SNOMEDCTComponentTypeEnum.getById(componentId).equals(SNOMEDCTComponentTypeEnum.CONCEPT);

    if (isConcept) {
      conceptFieldIds.add(componentId);
    } else {
      descriptionFieldIds.add(componentId);
    }
  }

  protected static List<Description> getDescriptionsByConceptIds(List<String> conceptIds,
      String languageCode, String effectiveTime) {
    return descriptionService.getDescriptionListByConceptIdsAndLanguageCodeAndEffectiveTime(conceptIds, languageCode, effectiveTime);
  }

  protected static List<Description> getDescriptionsByDescriptionIds(List<String> descriptionIds,
      String effectiveTime, String languageCode) {
    return descriptionService.getDescriptionListByDescriptionIdsAndLanguageCodeAndEffectiveTime(descriptionIds, languageCode,
        effectiveTime);
  }

  protected static Value createValueObject(String id, String name) {
    return new Value(id, name);
  }
  
  protected static List<RefsetMemberViewDTO> getReasonList(String componentId, String effectiveTime) {
    List<RefsetMemberViewDTO> referencesetMemberViewDTOList = new ArrayList<RefsetMemberViewDTO>();
    
    List<RefsetMemberViewDTO> historicalReferencesetMemberViewDTOList = null;
    List<RefsetMemberViewDTO> componentReferencesetMemberViewDTOList = null;
    SNOMEDCTComponentTypeEnum componentType = SNOMEDCTComponentTypeEnum.getById(componentId);
    if (componentType != null) {
      historicalReferencesetMemberViewDTOList
        = referencesetMemberService.getHistoricalReferencesetMemberList(componentId, effectiveTime);
      
      if (componentType.equals(SNOMEDCTComponentTypeEnum.CONCEPT)) {
        componentReferencesetMemberViewDTOList = referencesetMemberService.getConceptInactivationReferencesetMemberList(componentId, effectiveTime);
      } else if (componentType.equals(SNOMEDCTComponentTypeEnum.DESCRIPTION)) {
        componentReferencesetMemberViewDTOList = referencesetMemberService.getDescriptionInactivationReferencesetMemberList(componentId, effectiveTime);
      } else if (componentType.equals(SNOMEDCTComponentTypeEnum.RELATIONSHIP)) {
        // TODO
      } else {
        log.error("BaseConverter Error");
      }
      
      if (historicalReferencesetMemberViewDTOList != null) {
        referencesetMemberViewDTOList.addAll(historicalReferencesetMemberViewDTOList);
      }
      
      if (componentReferencesetMemberViewDTOList != null) {
        referencesetMemberViewDTOList.addAll(componentReferencesetMemberViewDTOList);
      }
    }
    
    return referencesetMemberViewDTOList;
  }
}
