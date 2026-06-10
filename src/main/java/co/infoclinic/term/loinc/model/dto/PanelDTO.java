package co.infoclinic.term.loinc.model.dto;

import java.util.List;
import co.infoclinic.term.common.model.dto.Value;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Panel DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanelDTO {

	private PanelContentDTO panel;
	
	private List<Value> memberOfThesePanels;
}
