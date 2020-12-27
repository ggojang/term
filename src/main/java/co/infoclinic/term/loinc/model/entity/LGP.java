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
public class LGP {
	
	/** 
	 * "ParentGroupId"
	 * "ParentGroup"
	 * "Status"
	 * */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "SEQ")//, columnDefinition = "COMMENT '시퀀스'")
	private Long id;
	
	/** Parent Group Id : LPxxxxxx-x */
	@Column(length = 10, name="PARENT_LG_ID")
	private String parentLGId;
	
	/** LOINC Common Name :  */
	@Column(length = 255, name="PARENT_LG")
	private String parentLG;
	
	/** LOINC Part Number : LPxxxxxx-x */
	@Column(length = 10, name="STATUS")
	private String status;
}