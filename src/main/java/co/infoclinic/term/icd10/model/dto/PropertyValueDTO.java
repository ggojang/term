package co.infoclinic.term.icd10.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyValueDTO {

  private String property;
  
  private String value;
 
}
