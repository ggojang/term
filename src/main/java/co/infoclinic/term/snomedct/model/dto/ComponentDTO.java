package co.infoclinic.term.snomedct.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SNOMED CT Component DTO(Data Transfer Object)
 * <p>
 * @Data: 모든 필드에 대하여 Getter 메소드 생성
 * @NoArgsConstructor: 매개변수가 없는 생성자 생성
 * @AllArgsConstructor: 클래스 내에 있는 모든 필드를 매개변수로 받는 생성자 생성
 * @JsonInclude(Include.NON_NULL): JSON으로 반환할 때, 필드 값이 Null인 필드는 대상에서 제외
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentDTO {

	/** Component의 유효 시작 시간 */
	private String effectiveTime;
	
	/** Component의 활성화 여부 */
	private boolean active;
	
	/** 사유 목록 필드 */
	@JsonInclude(Include.NON_NULL)
	private List<RefsetMemberViewDTO> reasons;
}
