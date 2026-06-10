package co.infoclinic.term.icd10.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ICD10 검색 결과를 담는 DTO(Data Transfer Object)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Icd10SearchResultDTO {

	private String code;
	
	private String kind;
	
	private String label;
}
