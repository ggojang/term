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
public class LPLink {
	
	/** 
	 * "LoincNumber"
	 * "LongCommonName"
	 * "PartNumber"
	 * "PartName"
	 * "PartCodeSystem"
	 * "PartTypeName"
	 * "LinkTypeName"
	 * "Property"
	 * */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "SEQ")//, columnDefinition = "COMMENT '시퀀스'")
	private Long id;
	
	/** LOINC Number : xxxxxx-x */
	@Column(length = 10, name="LOINC_NUMBER")
	private String loincNumber;
	
	/** LOINC Common Name :  */
	@Column(length = 255, name="LONG_COMMON_NAME")
	private String longCommonName;
	
	/** LOINC Part Number : LPxxxxxx-x */
	@Column(length = 10, name="PART_NUMBER")
	private String partNumber;
	
	/** Part Name */
    @Column(length = 255, name="PART_NAME")
    private String partName;

    /** Part Code System */
    @Column(length = 255, name="PART_CODE_SYSTEM")
    private String partCodeSystem;
    
    /** Part Type Name */
	@Column(length = 20, name="PART_TYPE_NAME")
    private String partTypeName;

	/** Link Type Name */
	@Column(length = 20, name="LINK_TYPE_NAME")
    private String linkTypeName;
	
    /** Property */
    @Column(length = 255, name="PROPERTY")
    private String property;
}