package co.infoclinic.term.loinc.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LOINC Simple DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoincSimpleDTO {

	private String code;
	
	private String prefName;
	
	/** 분류 명; */
	private String clsName;
	
	private String shortName;
	
	private String longName;
}
