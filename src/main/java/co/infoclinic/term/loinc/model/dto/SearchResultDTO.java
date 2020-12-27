package co.infoclinic.term.loinc.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LOINC 검색 결과를 담는 DTO(Data Transfer Object)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDTO {

	private String code;
	
	private String fsn;
	
	private String shortName;
	
	private String status;
}
