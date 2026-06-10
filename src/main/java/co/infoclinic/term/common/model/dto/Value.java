package co.infoclinic.term.common.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value Object
 * 
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
@JsonInclude(Include.NON_NULL)
public class Value {

	/** Value의 식별자 */
	private String id;
	
	/** Value의 이름 */
	private String name;
}
