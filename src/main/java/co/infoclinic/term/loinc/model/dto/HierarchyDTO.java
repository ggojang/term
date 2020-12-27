package co.infoclinic.term.loinc.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HierarchyDTO {

	/** Loinc code */
	private String code;
	
	/**   */
	private String name;
	
	/**  */
	private String prefName;
	
	/** Fully Specified Name */
	private String fsn;
	
	/** Long Common Name */
	private String longName;
	
	/** 부모 코드 */
	private String parent;
	
	/** 루트까지의 경로 */
	private String path;

	/** 자식 수 */
	private int chdCnt;
	
	/** 자손 수 */
	private int desCnt;
}
