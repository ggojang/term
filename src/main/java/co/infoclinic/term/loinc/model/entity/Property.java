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
 * PROPERTY 테이블의 Entity
 */
@Entity
@Table(schema = "loinc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Property {

	/** Property 식별자 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "SEQ")
	private Long id;
	
	/** 메인 Property의 분류 */
	@Column
	private String category;
	
	/** 값 */
	@Column
	private String value;
	
	/** 설명 */
	@Column
	private String description;
}
