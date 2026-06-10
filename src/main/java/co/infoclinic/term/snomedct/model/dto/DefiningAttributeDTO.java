package co.infoclinic.term.snomedct.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefiningAttributeDTO {

	private String id;
	
	private String name;
	
	private List<DefiningRangeDTO> ranges;
}
