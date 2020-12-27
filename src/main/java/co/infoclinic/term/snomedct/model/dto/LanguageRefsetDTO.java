package co.infoclinic.term.snomedct.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LanguageRefsetDTO {

	private Long id;

	private String effectiveTime;
	
	private boolean active;
	
	private String moduleId;
	
	private String refsetId;
	
	private String referencedComponentId;
	
	private String acceptabilityId;
	
	private String referencesetId;
}
