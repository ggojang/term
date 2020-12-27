package co.infoclinic.term.loinc.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LOINC Panel Entity
 */
@Entity
@Table(catalog = "loinc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Panel {

	@Id
	@GeneratedValue
	@Column(name = "SEQ")
	private String seq;
	
	@Column(length = 4)
	private String version;

	@Column(length = 255)
	private String path;
	
	@Column(length = 10)
	private String rootCode;
	
	@Column(length = 10)
	private String parentId;
	
	@Column(length = 10)
	private String parentCode;
	
	@Column(length = 255)
	private String parentName;
	
	@Column(length = 11)
	private String id;
	
	@Column
	private int sequence;
	
	@Column(length = 10) 
	private String code;
	
	@Column(length = 255) 
	private String name;
		
	@Column(length = 255)
	private String displayNameForForm;
	
	@Column(length = 255)
	private String observationRequiredInPanel;

	@Column(length = 10)
	private String observationIdInForm;

	//@Column
	//private String skipLogicTarget;

	//@Column
	//private String skipLogicTargetAnswer;

	@Column(length = 255)
	private String skipLogicHelpText;

	//@Column
	//private String answerRequired;

	//@Column
	//private String maxNumberOfAnswers;

	@Column(length = 20)
	private String defaultValue;

	@Column(length = 10)
	private String entryType;

	@Column(length = 10)
	private String dataTypeInForm;

	@Column(length = 255)
	private String dataTypeSource;

	@Column(length = 255)
	private String answerSequenceOverride;

	@Column(length = 255)
	private String conditionForInclusion;

	@Column(length = 255)
	private String allowableAlternative;

	@Column(length = 20)
	private String observationCategory;

	@Column
	private String context;

	@Column(length = 255)
	private String consistencyChecks;

	@Column(length = 255)
	private String relevanceEquation;

	@Column(length = 255)
	private String codingInstructions;

	@Column(length = 10)
	private String questionCardinality;

	@Column(length = 10)
	private String answerCardinality;
	
	@Column(length = 10)
	private String answerListIdOverride;
	
	@Column(length = 10)
	private String answerListTypeOverride;  
	
	@Column
	private String externalCopyrightNotice;
	
}
