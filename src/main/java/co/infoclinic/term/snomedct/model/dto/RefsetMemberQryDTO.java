package co.infoclinic.term.snomedct.model.dto;

import java.util.List;

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
public class RefsetMemberQryDTO {
	
	private String uuid;
	
	private String effectiveTime;

	private Value module;
	
	private Value refset;
	
	private boolean referencedComponentActive;
	
	private Value referencedComponent;
	
	private List<Value> fields;
}
