package co.infoclinic.term.snomedct.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TermSearchResult {

	private boolean conceptActive;
	
	private boolean descriptionActive;
	
	private String conceptId;
	
	private String descriptionId;
	
	private String conceptEffectiveTime;
	
	private String descriptionEffectiveTime;
	
	private String definitionStatusId;
	
	private String typeId;
	
	private String fsn;
	
	private int length;
	
	private String semanticTag;
	
	private String term;
	
	private String lang;
	
	private String acceptabilityId; // 20200701 by Yu
}
