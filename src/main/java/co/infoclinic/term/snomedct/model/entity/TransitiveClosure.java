package co.infoclinic.term.snomedct.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transitive Closure Entity
 */
@Entity(name = "TC")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransitiveClosure {

	@Id
	@GeneratedValue
	@Column(name = "SEQ")
	private Long id;
	
	/** 자신 컨셉 아이디 */
	@Column
	private String conceptId;
	
	@Column
	private String term;
	
	/** 부모 컨셉 아이디 */
	@Column
	private String parentId;
	
	/** 루트로부터의 경로 */
	@Column
	private String path;
	
	/** children 수  */
	@Column
	private int childrenCount;
	
	/** descendant 수  */
	@Column
	private int descendantCount;
	
	/** 루트로부터 현재 경로의 깊이 */
	@Column
	private int depth;

	/** 릴리즈 기준 effectiveTime (예: "20241001") */
	@Column
	private String effectiveTime;
}