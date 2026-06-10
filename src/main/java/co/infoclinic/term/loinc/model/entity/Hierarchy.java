package co.infoclinic.term.loinc.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

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
public class Hierarchy {

	/** 시퀀스 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "SEQ")//, columnDefinition = "COMMENT '시퀀스'")
	private Long id;

	/** 타입 명 */
	@Column//(columnDefinition = "COMMENT '타입 명; 0:None, 1:CLASS, 2:PART'")
	private int type;

	/** 루트까지의 경로 */
	@Column(length = 255)//, columnDefinition = "COMMENT '루트까지의 경로'")
	private String path;
	
	/** 자식 수 */
	@Column
	private int childrenCount;
	
	/** 자손 수 */
	@Column
	private int descendantCount;

	/** 같은 노드에서의 정렬 순서 */
	@Column//(columnDefinition = "COMMENT '같은 노드에서의 정렬 순서'")
	private int sequence;

	/** 부모 식별자/코드 */
	@Column(length = 255)//, columnDefinition = "COMMENT '부모 식별자/코드'")
	private String parent;

	/** 코드 */
	@NotNull
	@Column(length = 50)//, columnDefinition = "COMMENT '코드'"), 30 20200410 by Yu
	private String code;

	/** 코드 명 */
	@NotNull
	@Column(length = 255)//, columnDefinition = "COMMENT '코드 명'")
	private String name;
	
	/** 선호하는 이름; Major파트를 합친 이름 */
	@NotNull
	@Column(columnDefinition = "text")//, columnDefinition = "COMMENT '선호하는 이름; Major파트를 합친 이름'")
	private String preferredName;
}
