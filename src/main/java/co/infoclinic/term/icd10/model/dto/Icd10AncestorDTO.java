package co.infoclinic.term.icd10.model.dto;

import java.util.List;
import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//import co.infoclinic.term.icd10.model.dto.Icd10RubricKindDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Icd10AncestorDTO {
	
	  private String code;
	  
	  private String version;
	  
	  private String classKind;
	  
	  private String usageKind;
	  
	  private String superClass;
	      
	  private String label;
	  
	  private String ref;
	  
	  private int childrenCount;
	  
	  private int descendantCount;

	  private String path;

	  private String koreanLabel;

	  private Boolean isKcdExt;

}