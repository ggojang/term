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
public class LPMap {
	
	/** 
	 * "PartNumber"
	 * "PartName"
	 * "PartTypeName"
	 * "ExtCodeId"
	 * "ExtCodeDisplayName"
	 * "ExtCodeSystem"
	 * "Equivalence"
	 * "ContentOrigin"
	 * "ExtCodeSystemVersion"
	 * "ExtCodeSystemCopyrightNotice"
	 * */
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "SEQ")//, columnDefinition = "COMMENT '시퀀스'")
	private Long id;
	
	/** LOINC Part Number : LPxxxxxx-x */
	@Column(length = 10, name="PART_NUMBER")
	private String partNumber;
	
	/** Part Name */
    @Column(length = 255, name="PART_NAME")
    private String partName;
	
    /** Part Type Name */
	@Column(length = 20, name="PART_TYPE_NAME")
    private String partTypeName;
	
    /** Ext Code Id */
	@Column(length = 20, name="EXT_CODE_ID")
	private String extCodeId;
	
    /** Ext Code Display Name */
	@Column(length = 255, name="EXT_CODE_DISPLAY_NAME")
	private String extCodeDisplayName;
	
    /** Ext Code System */
	@Column(length = 50, name="EXT_CODE_SYSTEM")
	private String extCodeSystem;
	
    /** Equivalence */
	@Column(length = 20, name="EQUIVALENCE")
	private String equivalence;
	
    /** Content Origin */
	@Column(length = 20, name="CONTENT_ORIGIN")
	private String contentOrigin;
	
    /** Ext Code System Version */
	@Column(length = 100, name="EXT_CODE_SYSTEM_VERSION")
	private String extCodeSystemVersion;
	
    /** Ext Code System Copyright Notice */
	@Column(length = 1024, name="EXT_CODE_SYSTEM_COPYRIGHT_NOTICE")
	private String extCodeSystemCopyrightNotice;
	
}