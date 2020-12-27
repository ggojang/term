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
 * HIERARCHY 테이블의 Entity
 */
@Entity
@Table(catalog = "loinc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinguisticVariant {
	
	/** 식별자 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "SEQ")
	private Long id;
	
	/** LOINC 식별자 */
	@Column(length = 10)
	private String code;
	
	/** 1번째 파트; 컴포넌트 또는 검체 */
	@Column(length = 255)
    private String component;

	/** 2번째 파트; 관찰된 속성(e.g. 질량, 개체) */
    @Column(length = 30)
    private String property;

    /** 3번째 파트; 측정 시간(e.g. 특정 시점/시간(point in time), 24시간) */
    @Column(length = 15)
    private String timeAspect;

    /** 4번째 파트; 시료 또는 시스템의 유형(e.g. 혈청, 소변) */
    @Column(length = 100)
    private String system;

    /** 5번째 파트; 측정 스케일(e.g. 질적, 정량적) */
    @Column(length = 30)
    private String scaleType;

    /** 6번째 파트; 측정 메소드 */
    @Column(length = 50)
    private String methodType;
    
    /** 분류 명 */
    @Column(length = 40)
    private String className;
    
    @Column(length = 40)
    private String shortName;
    
    @Column(length = 255)
    private String longCommonName;
    
    @Column(columnDefinition = "text")
    private String relatedNames2;
    
    /** ISO Language Code */
	@Column(length = 2)
	private String isoLang;
	
	/** ISO Country Code */
	@Column(length = 2)
	private String isoCountry;
	
	/** Lang */
	@Column(length = 30)
	private String lang;
}
