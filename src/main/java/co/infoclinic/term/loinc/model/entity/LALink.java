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
public class LALink {
	
	/** 
	 * LOINC_NUMBER
	 * LONG_COMMON_NAME
	 * ANSWER_LIST_ID
	 * ANSWER_LIST_NAME
	 * ANSWER_LIST_LINK_TYPE
	 * APPLICABLE_CONTEXT
	 * */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "SEQ")//, columnDefinition = "COMMENT '시퀀스'")
	private Long id;
	
	@Column(length = 10, name="LOINC_NUMBER")
	private String loincNumber;
	
	@Column(length = 255, name="LONG_COMMON_NAME")
	private String longCommonName;
	
	@Column(length = 10, name="ANSWER_LIST_ID")
	private String LAID;
	
    @Column(length = 255, name="ANSWER_LIST_NAME")
    private String LAName;
    
    @Column(length = 20, name="ANSWER_LIST_LINK_TYPE")
    private String LALinkType;
    
	@Column(length = 255, name="APPLICABLE_CONTEXT")
    private String applicableContext;
}