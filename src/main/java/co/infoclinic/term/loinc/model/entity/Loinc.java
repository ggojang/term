package co.infoclinic.term.loinc.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LOINC 테이블의 Entity
 */
@Entity
@Table(catalog = "loinc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loinc {

	/** LOINC 식별자 */
	@Id
	@Column(length = 10)
	private String code;
	
	/** 1번째 파트; 컴포넌트 또는 검체 */
	@Column(length = 255)
    private String component;

	/** 2번째 파트; 관찰된 속성(e.g. 질량, 개체) */
    @Column(length = 30)
    private String property;

    /** 3번째 파트; 측정 시간(e.g. 특정 시점/시간(point in time), 24시간) */
    @Column(length = 15)
    private String timeAspect;

    /** 4번째 파트; 시료 또는 시스템의 유형(e.g. 혈청, 소변) */
    @Column(length = 100)
    private String system;

    /** 5번째 파트; 측정 스케일(e.g. 질적, 정량적) */
    @Column(length = 30)
    private String scaleType;

    /** 6번째 파트; 측정 메소드 */
    @Column(length = 50)
    private String methodType;
	
    /** 분류 명 */
    @Column(length = 20)
    private String className;
    
    /** 가장 최근에 변경된 LOINC 버전 */
    @Column(length = 10)
    private String lastChangedVersion;
    
    
    @Column(length = 3)
    private String changeType;
    
    @Column(columnDefinition = "text")
    private String definitionDescription;

    @Column(length = 11)
    private String status;

    @Column(length = 255)
    private String consumerName;
    
    /** 분류 유형; 1:Laboratory, 2:Clinical, 3:Claims attachements, 4:Surveys */
    @Column
    private int classType;

    @Column(columnDefinition = "text")
    private String formula;
    /*
    @Column(length = 20)
    private String species;
	*/
    @Column(columnDefinition = "text")
    private String exampleAnswers;

    @Column(columnDefinition = "text")
    private String surveyQuestText;

    @Column(length = 50)
    private String surveyQuestSrc;

    @Column(length = 1)
    private String unitsRequired;

    @Column(length = 30)
    private String submittedUnits;

    @Column(columnDefinition = "text")
    private String relatedNames2;
    
    @Column(length = 40)
    private String shortName;

    @Column(length = 15)
    private String orderObs;

    @Column(length = 1)
    private String cdiscCommonTests;

    @Column(length = 50)
    private String hl7FieldSubfieldId;

    @Column(columnDefinition = "text")
    private String externalCopyrightNotice;

    @Column(length = 255)
    private String exampleUnits;
    
    @Column(length = 255)
    private String longCommonName;

    @Column(columnDefinition = "text")
    private String unitsAndRange;

    //@Column(length = 255)
    //private String documentSection;

    @Column(length = 255)
    private String exampleUcumUnits;

    @Column(length = 255)
    private String exampleSiUcumUnits;

    @Column(length = 9)
    private String statusReason;

    @Column(columnDefinition = "text")
    private String statusText;

    @Column(columnDefinition = "text")
    private String changeReasonPublic;

    @Column
    private int commonTestRank;

    @Column
    private int commonOrderRank;

    @Column
    private int commonSiTestRank;

    @Column(length = 15)
    private String hl7AttachmentStructure;

    @Column(length = 255)
    private String externalCopyrightLink;

    @Column(length = 255)
    private String panelType;

    @Column(length = 255)
    private String askAtOrderEntry;

    @Column(length = 50)
    private String associatedObservations;
    
    @Column(length = 255)
    private String firstReleasedVersion;
    
    @Column(length = 255, name = "VALID_HL7_ATTACHMENT_REQUEST")
    private String validHL7AttachmentRequest;
    
    @Column(length = 255)
    private String displayName;
}
