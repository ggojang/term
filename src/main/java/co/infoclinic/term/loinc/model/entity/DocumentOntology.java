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

@Entity
@Table(catalog = "loinc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentOntology {

	/** 시퀀스 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "SEQ")
	private Long id;
	
	/** LOINC code */
	@Column(length = 10)
	private String code;
	
	/** LOINC Part code */
	@Column(length = 10)
	private String partCode;
	
	/** LOINC Part Type name */
	@Column(length = 50)
	private String partTypeName;
	
	/** LOINC Part Sequence order */
	@Column
	private int partSeq;
	
	/** LOINC Part name */
	@Column(length = 255)
	private String partName;
}
