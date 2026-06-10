package co.infoclinic.term.loinc.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LOINC Link 테이블의 Entity
 */
@Entity
@Table(catalog = "loinc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LA {
	
	/** 
	 * ANSWER_LIST_ID
	 * ANSWER_LIST_NAME
	 * ANSWER_LIST_OID
	 * EXT_DEFINED_YN
	 * EXT_DEFINED_ANSWER_LIST_CODE_SYSTEM
	 * EXT_DEFINED_ANSWER_LIST_LINK
	 * ANSWER_STRING_ID
	 * LOCAL_ANSWER_CODE
	 * LOCAL_ANSWER_CODE_SYSTEM
	 * SEQUENCE_NUMBER
	 * DISPLAY_TEXT
	 * EXT_CODE_ID
	 * EXT_CODE_DISPLAY_NAME
	 * EXT_CODE_SYSTEM
	 * EXT_CODE_SYSTEM_VERSION
	 * EXT_CODE_SYSTEM_COPYRIGHT_NOTICE
	 * SUBSEQUENCE_TEXT_PROMPT
	 * DESCRIPTION
	 * SCORE
	 * */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "SEQ")//, columnDefinition = "COMMENT '시퀀스'")
	private Long id;
	
	@Column(length = 10, name="ANSWER_LIST_ID")
	private String LAID;
	
	@Column(length = 255, name="ANSWER_LIST_NAME")
	private String LAName;
	
	@Column(length = 255, name="ANSWER_LIST_OID")
	private String LAOid;
	
    @Column(length = 1, name="EXT_DEFINED_YN")
    private String extDefinedYn;
    
    @Column(length = 255, name="EXT_DEFINED_ANSWER_LIST_CODE_SYSTEM")
    private String extDefinedLACodeSystem;
    
	@Column(length = 255, name="EXT_DEFINED_ANSWER_LIST_LINK")
    private String extDefinedLALink;

	@Column(length = 10, name="ANSWER_STRING_ID")
    private String answerStringID;
	
    @Column(length = 3, name="LOCAL_ANSWER_CODE")
    private String localAnswerCode;
    
    @Column(length = 255, name="LOCAL_ANSWER_CODE_SYSTEM")
	private String localAnswerCodeSystem;
	
	@Column(name="SEQUENCE_NUMBER")
	private int sequenceNumber;
	
	@Column(length = 255, name="DISPLAY_TEXT")
	private String displayText;
	
    @Column(length = 20, name="EXT_CODE_ID")
    private String extCodeID;
    
    @Column(length = 255, name="EXT_CODE_DISPLAY_NAME")
    private String extCodeDisplayName;
    
	@Column(length = 255, name="EXT_CODE_SYSTEM")
    private String extCodeSystem;

	@Column(length = 255, name="EXT_CODE_SYSTEM_VERSION")
    private String extCodeSystemVersion;
	
    @Column(length = 1024, name="EXT_CODE_SYSTEM_COPYRIGHT_NOTICE")
    private String extCodeSystemCopyrightNotice;
     
    @Column(length = 255, name="SUBSEQUENCE_TEXT_PROMPT")
    private String subsequenceTextPrompt;

	@Column(length = 1024, name="DESCRIPTION")
    private String description;
	
    @Column(length = 3, name="SCORE")
    private String score;
}