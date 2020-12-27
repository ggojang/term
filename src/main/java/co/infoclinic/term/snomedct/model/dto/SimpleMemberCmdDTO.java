package co.infoclinic.term.snomedct.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleMemberCmdDTO {

	private String conceptId;
	
	private String memberId;
	
	//private boolean active;
	
	private boolean includeSubtypes;
}
