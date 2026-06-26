package co.infoclinic.term.snomedct.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transitive Closure Entity — 직접 IS-A 관계 단위로 저장
 * 계층 탐색(조상/자손)은 재귀 CTE로 실시간 계산
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

	@Column
	private String childId;

	@Column
	private String parentId;

	@Column
	private String validFrom;

	@Column
	private String validTo;
}
