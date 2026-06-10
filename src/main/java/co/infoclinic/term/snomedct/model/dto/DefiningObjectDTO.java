package co.infoclinic.term.snomedct.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefiningObjectDTO {

	private String id;
	
	private String name;
	
	//private List<DefiningAttributeDTO> attributes;
}
