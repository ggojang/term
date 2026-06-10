package co.infoclinic.term.snomedct.model.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import co.infoclinic.term.snomedct.model.dto.DescriptionDTO;
import co.infoclinic.term.snomedct.model.dto.RefsetMemberViewDTO;
import co.infoclinic.term.snomedct.model.entity.Description;

@Component
public class DescriptionConverter extends BaseConverter {
	
  @SuppressWarnings("null")
  public static DescriptionDTO toDTO(Description entity, boolean isIncludeReason) {
    DescriptionDTO dto = null;
    List<RefsetMemberViewDTO> reasonList = null;
    
    if (entity != null) {
      dto = new DescriptionDTO();
      
      dto.setId(entity.getId());
      dto.setActive(entity.isActive());
      dto.setModuleId(entity.getModuleId());
      dto.setEffectiveTime(entity.getEffectiveTime());
      dto.setTypeId(entity.getTypeId());
      dto.setLanguageCode(entity.getLanguageCode());
      dto.setConceptId(entity.getConceptId());
      dto.setDescriptionId(entity.getDescriptionId());
      dto.setCaseSignificanceId(entity.getCaseSignificanceId());
      dto.setTerm(entity.getTerm());
      
      if (isIncludeReason) {
        reasonList = getReasonList(entity.getDescriptionId(), entity.getEffectiveTime());
        if (reasonList != null || reasonList.size() > 0) {
          dto.setReasons(reasonList);
        }
      }
    }
    
    return dto;
  }
  
  public static List<DescriptionDTO> toDTOList(List<Description> entities, boolean isIncludeReason) {
    if (entities == null || ((Collection<?>) entities).size() == 0) {
      return null;
    }
    
    List<DescriptionDTO> descriptionDTOList = new ArrayList<DescriptionDTO>();
    Description description = null;
    int entitySize = entities.size();
    for (int i = 0; i < entitySize; i++) {
      description = entities.get(i);
      descriptionDTOList.add(toDTO(description, isIncludeReason));
    }
    return descriptionDTOList;
   }

  public static Description toEntity(DescriptionDTO dto) {
    if (dto == null) {
      return new Description();
    }
    
    Description entity = new Description();
    entity.setId(dto.getId());
    entity.setActive(dto.isActive());
    entity.setModuleId(dto.getModuleId());
    entity.setEffectiveTime(dto.getEffectiveTime());
    entity.setTypeId(dto.getTypeId());
    entity.setLanguageCode(dto.getLanguageCode());
    entity.setConceptId(dto.getConceptId());
    entity.setDescriptionId(dto.getDescriptionId());
    entity.setCaseSignificanceId(dto.getCaseSignificanceId());
    entity.setTerm(dto.getTerm());
    
    return entity;
  }

  public static List<Description> toEntityList(List<DescriptionDTO> dtos) {
    if (dtos == null || dtos.size() == 0) {
      return null;
    }
    
    List<Description> entities = new ArrayList<Description>();
    DescriptionDTO dto = null;
    int dtosSize = dtos.size();
    for (int i = 0; i < dtosSize; i++) {
      dto = dtos.get(i);
      entities.add(toEntity(dto));
    }
    return entities;
  }
}
