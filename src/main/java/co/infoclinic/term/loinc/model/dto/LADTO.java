package co.infoclinic.term.loinc.model.dto;

import java.util.List;

import co.infoclinic.term.loinc.model.dto.LAContentDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Panel DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LADTO {
	
	private String loincNumber;

	private String longCommonName;

	private String LAID;

	private String LAName;

	private String LALinkType;

	private String applicableContext;
	
	private List<LAContentDTO> la;
	
}