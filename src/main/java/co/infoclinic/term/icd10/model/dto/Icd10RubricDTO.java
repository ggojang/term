package co.infoclinic.term.icd10.model.dto;

import java.util.List;
import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import co.infoclinic.term.icd10.model.dto.Icd10RubricKindDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Icd10RubricDTO {
	
	  private String codeSystem;

	  private String version;
	  
	  private String code;
	  
	  private String usageKind;
	  
	  private List<Icd10RubricKindDTO> kinds;   
	  
	  //private String paraType;

}
