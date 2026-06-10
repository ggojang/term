package co.infoclinic.term.icd10.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDTO {
  
  private String codeSystem;
  
  private String version;
  
  private String code;
  
  private String preferredName;
  
  private List<String> motalities;
  
  private List<String> mobidities;
  
  private List<PropertyValueDTO> inclusions;
  
  private List<PropertyValueDTO> exclusions;
  
  private PropertyValueDTO attribute;
  
  
  /*
  private String id;
  
  private String code;
  
  private String property;
  
  private String name;
  
  private String language;
  
  private String type;
  
  private String value;
  */
}
