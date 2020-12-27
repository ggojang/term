package co.infoclinic.term.loinc.model.dto;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LForm (추가되지 않은 속성이 있음)
 * 
 * 포맷 문서: https://github.com/lhncbc/lforms/blob/master/form_definition.md
 * 
 * NLM: National Library of Medicine(https://www.nlm.nih.gov/)
 * NIH: part of the National Institutes of Health(https://www.nih.gov/)
 * LHNCBC: Lister Hill National Center for Biomedical Communications(https://www.regenstrief.org/)
 * 
 * https://github.com/lhncbc/lforms
 * http://lhncbc.github.io/lforms/
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LFormQryDTO {

	/** Panel, Answer Lists, Answer 코드 */
	private String code;
	
	/** Form 유형, "LOINC"만 지원. 추후 추가될 예정 */
	private String type;
	
	/** (required) Form 이름 (사용자가 보게되는) */
	@NotNull
	private String name;
	
	/** (optional) 폼의 렌더링 형태. 현재는 'table'과 'list'만 지원하며, 기본값은 'table' */
	@JsonProperty("template")
	private String tpl;
	
	/** 만약 true일 경우, 질문이 아니라 섹션이다. 질문 목록과 섹션 목록의 배열의 속성을 지닌 'item'을 포함 할 수 있다. */
	private boolean header; 
	
	/** Form의 저작권 정보 */
	@JsonProperty("copyrightNotice")
	private String cpyrNot;
	
	/** 질문 목록과 섹션 목록의 배열. 질문 목록과 섹션 목록(하위 질문목록 포함)은 대부분 이 배열에서 동일하게 표현되지만, 섹션은 items를 포함할 수 있습니다. */
	private List<LFormItemQryDTO> items;
}
