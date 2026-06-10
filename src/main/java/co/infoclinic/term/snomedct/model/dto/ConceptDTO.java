package co.infoclinic.term.snomedct.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class ConceptDTO extends ComponentDTO {

	private Long id;
	
	private String moduleId;
	
	private String definitionStatusId;
	
	private String conceptId;
}
