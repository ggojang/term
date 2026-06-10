package co.infoclinic.term.snomedct.model.dto;

import java.util.List;
import co.infoclinic.term.common.model.dto.Value;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Concept Query DTO(Data Transfer Object)
 * <p>
 * 
 * @Data: 모든 필드에 대하여 Getter 메소드 생성
 * @NoArgsConstructor: 매개변수가 없는 생성자 생성
 * @AllArgsConstructor: 클래스 내에 있는 모든 필드를 매개변수로 받는 생성자 생성
 * @EqualsAndHashCode(callSuper = true): equals와 hashCode 메서드 생성; 부모에 구현된 메서드 호출
 *                              </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ConceptTreeDTO extends ComponentDTO {

	/** 식별자 */
	private String conceptId;

	/** 용어 */
	private String term;

	/** 시맨틱태그 */
	private String semanticTag;

	/** 모듈 정보 */
	private Value module;

	/** 정의 상태; fully-defined, primitive */
	private Value definitionStatus;

	/** 자식 수 */
	private int childrenCount;

	/** 자손 수 */
	private int descendantCount;
	
	/** 자손 배열 */
	public List<ConceptTreeDTO> children;
}