package co.infoclinic.term.snomedct.model.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefsetMemberDTO {

	private Long id;
	
	private String uuid;

	private String effectiveTime;
	
	private boolean active;
	
	private String moduleId;
	
	private String refsetId;
	
	private String referencedComponentId;
	
	private Map<String, String> extra;
	
}
