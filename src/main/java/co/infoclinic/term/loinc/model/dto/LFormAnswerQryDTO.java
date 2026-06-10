package co.infoclinic.term.loinc.model.dto;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LForm Answer
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
public class LFormAnswerQryDTO {

	/** (optional) 목록 아이템의 라벨. 'a','b' 등과 같은 */
	private String label;
	
	/** (optional) 목록 아이템의 코드. 코드화된 응답 목록(answers)을 필요로 하지 않는다면 생략할 수 있습니다.  */
	private String code;
	
	/** (required) 목록 아이템의 이름. 목록내에서 유일해야 합니다.  */
	@NotNull
	private String text;
	
	/** (optional) Some forms have scored answers which get summed into a total field. See TOTALSCORE under calculationMethod below for how to specify which field holds the total. */
	private int score;
	
	/** (optional) If this answer is an "Other" item, then this "other" key provides a label like "Please specify" for an additional field that will be available with the user chooses this answer. */
	private String other;
	
}
