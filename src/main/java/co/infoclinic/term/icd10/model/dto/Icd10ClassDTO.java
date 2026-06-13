package co.infoclinic.term.icd10.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Icd10ClassDTO {

  private String code;
  
  private String version;
  
  private String classKind;
  
  private String usageKind;
  
  private String superClass;
  
  private String label;
  
  private String ref;
  
  private int childrenCount;
  
//  private int descendantCount;
  
  private String path;

  private String koreanLabel;

  private Boolean isKcdExt;
}
