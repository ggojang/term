package co.infoclinic.term.snomedct.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefsetDTO {

	private ConceptDTO concept;
	
	private List<DescriptionDTO> descriptions;
	
	private List<RelationshipDTO> relationships;
}
