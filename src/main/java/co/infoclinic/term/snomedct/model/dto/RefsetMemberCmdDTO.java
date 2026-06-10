package co.infoclinic.term.snomedct.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 레퍼런스세트 멤버들을 추가하기 위해 사용하는 클래스
 * 
 * @author dongwon
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefsetMemberCmdDTO {
	
	/* 레퍼런스세트 아이디 */
	private String refsetId;
	
	/* 모듈 아이디 */
	private String moduleId;
	
	/* 활성 멤버 목록 */
	private List<SimpleMemberCmdDTO> activeMbrs;
	
	/* 비활성 멤버 목록 */
	private List<SimpleMemberCmdDTO> inactiveMbrs;
	
	/* 삭제 멤버 목록 */
	private List<SimpleMemberCmdDTO> deletedMbrs;
}
