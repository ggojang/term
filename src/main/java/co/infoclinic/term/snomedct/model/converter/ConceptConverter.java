package co.infoclinic.term.snomedct.model.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import co.infoclinic.term.common.model.dto.Value;
import co.infoclinic.term.snomedct.model.dto.ConceptViewDTO;
import co.infoclinic.term.snomedct.model.dto.RefsetMemberViewDTO;
import co.infoclinic.term.snomedct.model.entity.Concept;
import co.infoclinic.term.snomedct.model.entity.Description;

@Component
public class ConceptConverter extends BaseConverter {
  
  public static ConceptViewDTO toViewDTO(Concept entity, String languageCode, String effectiveTime) {
    ConceptViewDTO dto = null;
    List<RefsetMemberViewDTO> reasonList = null;

    if (entity != null) {
      dto = new ConceptViewDTO();

      List<String> fieldIds = new ArrayList<String>();
      fieldIds.add(entity.getConceptId());
      fieldIds.add(entity.getDefinitionStatusId());
      fieldIds.add(entity.getModuleId());

      List<Description> descriptionsByFieldIds =
          getDescriptionsByConceptIds(fieldIds, languageCode, effectiveTime);

      Map<String, String> termMap = new HashMap<String, String>();
      for (Description d : descriptionsByFieldIds) {
        termMap.put(d.getConceptId(), d.getTerm());
      }

      // term
      String term = termMap.get(entity.getConceptId());

      // definition status
      String termDefState = termMap.get(entity.getDefinitionStatusId());
      Value valueDefState = createValueObject(entity.getDefinitionStatusId(), termDefState);
      
      // module
      String termModule = termMap.get(entity.getModuleId());
      Value valueModule = createValueObject(entity.getModuleId(), termModule);

      //////////

      // set conceptId
      dto.setConceptId(entity.getConceptId());

      // set active
      dto.setActive(entity.isActive());

      // set effectiveTime
      dto.setEffectiveTime(entity.getEffectiveTime());

      // set term
      dto.setTerm(term);

      // set definition status
      dto.setDefinitionStatus(valueDefState);

      // set module
      dto.setModule(valueModule);
      
      reasonList = getReasonList(entity.getConceptId(), entity.getEffectiveTime());
      if (reasonList != null && reasonList.size() > 0) {
        dto.setReasons(reasonList);
      }
    }

    return dto;
  }

  public static List<ConceptViewDTO> toViewDTOList(List<Concept> entities, String languageCode, String effectiveTime) {
    if (entities == null || entities.isEmpty()) {
      return new ArrayList<ConceptViewDTO>();
    }
    
    List<ConceptViewDTO> dtos = new ArrayList<ConceptViewDTO>();
    Concept entity = null;
    int entitiesSize = entities.size();
    for (int i = 0; i < entitiesSize; i++) {
      entity = entities.get(i);
      dtos.add(toViewDTO(entity, languageCode, effectiveTime));
    }
    
    return dtos;
  }
}
