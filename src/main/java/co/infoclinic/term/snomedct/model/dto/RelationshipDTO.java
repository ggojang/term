package co.infoclinic.term.snomedct.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipDTO {

  private Long id;
  
  private String relationshipId;
  
  private String effectiveTime;
  
  private boolean active;
  
  private String moduleId;
  
  private String sourceId;
  
  private String destinationId;
  
  private String relationshipGroup;
  
  private String typeId;
  
  private String characteristicTypeId;
  
  private String modifierId;
}
