package co.infoclinic.term.loinc.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LOINC Part 테이블의 Entity
 */
@Entity
@Table(catalog = "loinc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LP {
	
	/** 
	 * "PartNumber"
	 * "PartTypeName"
	 * "PartName"
	 * "PartDisplayName"
	 * "Status" 
	 * */
	
	/** LOINC Part Code : LPxxxxxx-x */
	@Id
	@Column(length = 10)
	private String partNumber;
	
	/** Part Type Name */
	@Column(length = 20)
    private String partTypeName;

	/** Part Name */
    @Column(length = 255)
    private String partName;

    /** Part Display Name */
    @Column(length = 255)
    private String partDisplayName;

    /** Status */
    @Column(length = 10)
    private String status;
}