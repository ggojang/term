package co.infoclinic.term.loinc.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LoincDTO extends LoincSimpleDTO {
	
	private static final String CLS_TYPE1 = "Lab";
	private static final String CLS_TYPE2 = "Clinical";
	private static final String CLS_TYPE3 = "Attachment";
	private static final String CLS_TYPE4 = "Survey";
	

	/** 1번째 파트; 컴포넌트 또는 검체 */
    private String component;

	/** 2번째 파트; 관찰된 속성(e.g. 질량, 개체) */
    private String property;

    /** 3번째 파트; 측정 시간(e.g. 특정 시점/시간(point in time), 24시간) */
    private String time;

    /** 4번째 파트; 시료 또는 시스템의 유형(e.g. 혈청, 소변) */
    private String system;

    /** 5번째 파트; 측정 스케일(e.g. 질적, 정량적) */
    private String scale;

    /** 6번째 파트; 측정 메소드 */
    private String method;
	
    /** 가장 최근에 변경된 LOINC 버전 */
    private String lastChngdVer;
    
    private String chngType;
    
    private String defDesc;

    private String status;

    /** Consumer Name */
    private String consName;
    
    /** 분류 유형; 1:Laboratory, 2:Clinical, 3:Claims attachements, 4:Surveys */
    private String clsType;

    /** Orig: Formula */
    private String formula;

    /** Orig: Species */
    private String species;

    /** Orig: ExampleAnswers */
    private String exAnswers;

    /** Orig: SurveyQuestSrc */
    private String survQstTxt;

    /** Orig: SurveyQuestSrc */
    private String survQstSrc;

    /** Orig: UnitsRequired */
    private String unitsRqd;

    /** Orig: SubmittedUnits */
    private String submittedUnits;

    /** Orig: RelatedNames2 */
    private String relatedNames;
    
    /** Orig: ShortName */
    //private String shortName;

    /** Orig: OrderObs */
    private String orderObs;

    /** Orig: CdiscCommonTests */
    private String cdiscCmnTests;

    /** Orig: Hl7FieldSubfieldId */
    private String hl7FldSubfldId;

    /** Orig: ExternalCopyrightNotice */
    private String extCoprNotice;

    /** Orig: ExampleUnits */
    private String exUnits;
    
    /** Orig: LongCommonName */
    //private String longCommonName;

    /** Orig: UnitsAndRange */
    private String unitsAndRange;

    /** Orig: DocumentSection */
    //private String docSection;

    /** Orig: ExampleUcumUnits */
    private String exUcumUnits;

    /** Orig: ExampleSiUcumUnits */
    private String exSiUcumUnits;

    /** Orig: StatusReason */
    private String statusReason;

    /** Orig: StatusText */
    private String statusTxt;

    /** Orig: ChangeReasonPublic */
    private String chngReasonPublic;

    /** Orig: CommonTestRank */
    private int cmnTestRnk;

    /** Orig: CommonOrderRank */
    private int cmnOrderRnk;

    /** Orig: CommonSiTestRank */
    private int cmnSiTestRnk;

    /** Orig: hl7AttachmentStructure */
    private String hl7AttStru;

    /** Orig: ExternalCopyrightLink */
    private String extCoprLnk;

    /** Orig: PanelType */
    private String panelType;

    /** Orig: AskAtOrderEntry */
    private String askAtOrderEntry;

    /** Orig: AssociatedObservations */
    private String ascObs;
    
    private String firstRelVer;
    
    private String valHL7AttReq;
    
    private String dispName;
    
    
    public void setClsType(int clsType) {
    	if (clsType == 1) {
    		this.clsType = CLS_TYPE1;
    	} else if (clsType == 2) {
    		this.clsType = CLS_TYPE2;
    	} else if (clsType == 3) {
    		this.clsType = CLS_TYPE3;
    	} else if (clsType == 4) {
    		this.clsType = CLS_TYPE4;
    	} else {
    		this.clsType = "";
    	}
    }
}
