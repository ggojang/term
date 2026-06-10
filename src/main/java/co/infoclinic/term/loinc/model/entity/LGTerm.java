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
public class LGTerm {
	
	/** 
	 * "Category"
	 * "GroupId"
	 * "Archetype"
	 * "LoincNumber"
	 * "LongCommonName"
	 * "LG_NAME" // 추가
	 * */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "SEQ")//, columnDefinition = "COMMENT '시퀀스'")
	private Long id;
	
	/** Group Category */
	@Column(length = 10, name="Category")
	private String category;
	
	/** Group ID :  : LGxxxxxx-x */
	@Column(length = 10, name="LG_ID")
	private String LGId;

	/** Group Archetype */
    @Column(length = 10, name="ARCHETYPE")
    private String archetype;

    /** Loinc Number */
    @Column(length = 10, name="LOINC_NUMBER")
    private String LoincNumber;
    
    /** Loinc  Name */
	@Column(length = 255, name="LONG_COMMON_NAME")
    private String LongCommonName;
	
	/** Group Name */
	@Column(length = 255, name="LG_ID_NAME")
    private String LGIdName;
}