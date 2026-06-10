package co.infoclinic.term.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Subsumption Test DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubsumptionTestDTO {

	private String criteriaId;
	
	private String conceptId;
	
	private boolean result;
}
