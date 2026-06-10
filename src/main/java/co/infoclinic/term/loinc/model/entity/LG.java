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
public class LG {
	
	/** 
	 * "ParentGroupId"
	 * "GroupId"
	 * "Group"
	 * "Archetype"
	 * "Status"
	 * "VersionFirstReleased"
	 * */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "SEQ")//, columnDefinition = "COMMENT '시퀀스'")
	private Long id;
	
	/** Parent Group Id : LPxxxxxx-x */
	@Column(length = 10, name="PARENT_LG_ID")
	private String parentLGId;
	
	/** LOINC Common Name :  */
	@Column(length = 10, name="LG_ID")
	private String LGId;
	
	/** LOINC Part Number : LPxxxxxx-x */
	@Column(length = 255, name="LG")
	private String LG;
	
	/** Part Name */
    @Column(length = 10, name="ARCHETYPE")
    private String archetype;

    /** Part Code System */
    @Column(length = 10, name="STATUS")
    private String status;
    
    /** Part Type Name */
	@Column(length = 8, name="VERSION_FIRST_RELEASED")
    private String versionFirstReleased;
}