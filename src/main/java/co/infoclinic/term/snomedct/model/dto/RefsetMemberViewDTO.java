package co.infoclinic.term.snomedct.model.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import co.infoclinic.term.common.model.dto.Value;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefsetMemberViewDTO {
	
	private Long id;
	
	private String uuid;

	private String effectiveTime;
	
	private boolean active;
	
	private Value module;
	
	private Value refset;
	
	private Value referencedComponent;
	
	private Map<String, Value> extra;
	
	private Value type;
}
