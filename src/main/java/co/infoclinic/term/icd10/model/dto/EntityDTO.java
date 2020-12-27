package co.infoclinic.term.icd10.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityDTO {

  private String id;
  
  private String entity;
  
  private String parent;
  
  private String type;
  
  private String name;
}
