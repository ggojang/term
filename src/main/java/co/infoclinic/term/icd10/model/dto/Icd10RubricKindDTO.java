package co.infoclinic.term.icd10.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Icd10RubricKindDTO {
	
  private String kind;
  
  private String modCode;
  
  private String id;
	
  private String ftype;
  
  private String paraType;
  
  private String lang;
  
  private String label;
  
  private String ref;

}
