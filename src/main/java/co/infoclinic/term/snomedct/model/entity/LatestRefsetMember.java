package co.infoclinic.term.snomedct.model.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 최근 레퍼런스세트 멤버 클래스
 */
@Entity(name = "REFERENCESET_ACTIVE")
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LatestRefsetMember {
	
	@EmbeddedId
	private LatestRefsetMemberId id;
	
	@Column
	private String effectiveTime;

	@Column
	private String moduleId;

	@Column
	private String moduleName;

	@Column
	private String refsetId;

	@Column
	private String refsetName;

	@Column
	private String referencedComponentId;

	@Column
	private String referencedComponentName;

	@Column
	private boolean referencedComponentActive;
	
	@Column
	private String field1Id;
	
	@Column
	private String field1Value;
	
	@Column
	private String field2Id;
	
	@Column
	private String field2Value;
	
	@Column
	private String field3Id;
	
	@Column
	private String field3Value;
	
	@Column
	private String field4Id;
	
	@Column
	private String field4Value;
	
	@Column
	private String field5Id;
	
	@Column
	private String field5Value;
	
	@Column
	private String field6Id;
	
	@Column
	private String field6Value;

	@Column
	private String field7Id;
	
	@Column
	private String field7Value;
}
