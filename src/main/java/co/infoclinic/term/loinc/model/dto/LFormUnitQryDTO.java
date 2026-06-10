package co.infoclinic.term.loinc.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LForm Unit
 * 
 * 숫자 Answer 필드의 경우, 입력하는 수량에 대한 단위를 위한 선택적인 목록입니다.
 * 
 * For numeric answer fields, this is an optional list for the units for the quantity being entered.
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
public class LFormUnitQryDTO {

	/** 단위 이름 */
	private String name;
	
	/** 만약 true일 경우, 이 단위는 기본 단위가 될 것이며, 질문이 표시될 때 필드내에 보여지게 되는 것을 의미하며 사용자는 이 키를 선택하지 못합니다. false일 경우, 이 값을 생략 할 수 있습니다.  */
	@JsonProperty("default")
	private boolean initial;
}
