package co.infoclinic.term.icd10.model.dto;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import co.infoclinic.term.icd10.model.dto.Icd10RubricDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Icd10SiblingsDTO {

	  	  private ArrayList<Icd10RubricDTO> siblings;  
  
}
