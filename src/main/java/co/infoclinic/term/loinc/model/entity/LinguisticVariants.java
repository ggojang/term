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
public class LinguisticVariants {
	
	/** 식별자 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "SEQ")
	private Long id;
	
	/** ISO 언어코드 */
	@Column(length = 2)
	private String isoLang;
	
	/** ISO 국가코드 */
	@Column(length = 2)
	private String isoCountry;
	
	/** 언어 이름 */
	@Column(length = 30)
	private String lang;
	
	/** 생산자 */
	@Column(columnDefinition = "text")
	private String producer;
}
