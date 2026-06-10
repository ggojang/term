package co.infoclinic.term.loinc.model.dto;

import java.util.List;

import javax.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Panel Content DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanelContentDTO {
	
	private String version;

	private String path;
	
	private String rootCode;
	
	private String parentId;
	
	private String parentCode;
	
	private String parentName;
	
	private String id;
	
	private int sequence;
	
	private String code;
	
	private String name;
		
	private String displayNameForForm;
	
	private String observationRequiredInPanel;

	private String observationIdInForm;

	//private String skipLogicTarget;

	//private String skipLogicTargetAnswer;

	private String skipLogicHelpText;

	//private String answerRequired;

	//private String maxNumberOfAnswers;

	private String defaultValue;

	private String entryType;

	private String dataTypeInForm;

	private String dataTypeSource;

	private String answerSequenceOverride;

	private String conditionForInclusion;

	private String allowableAlternative;

	private String observationCategory;

	private String context;

	private String consistencyChecks;

	private String relevanceEquation;

	private String codingInstructions;

	private String questionCardinality;

	private String answerCardinality;
	
	private String answerListIdOverride;
	
	private String answerListTypeOverride;  
	
	private String externalCopyrightNotice;

	private List<PanelContentDTO> children;
}
