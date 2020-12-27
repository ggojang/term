package co.infoclinic.term.snomedct.model.dto;

import co.infoclinic.term.common.model.dto.Value;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipViewDTO {
  private boolean active;
  
  private String effectiveTime;

  private Value module;
  
  private String sourceId;
  
  private ConceptViewDTO destination;
  
  private String relationshipGroup;
  
  private ConceptViewDTO type;
  
  private Value characteristicType;
  
  private Value modifier;
}
