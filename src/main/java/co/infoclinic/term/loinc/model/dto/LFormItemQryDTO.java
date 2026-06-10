package co.infoclinic.term.loinc.model.dto;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LForm Item (추가되지 않은 속성이 있음)
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
public class LFormItemQryDTO {

	/** (required) 질문과 섹션 코드. 질문 목록간(형제) 유일한 코드여야 합니다. '/'는 포함할 수 없습니다. */
	@NotNull
	@JsonProperty("questionCode")
	private String qstCd;
	
	/** (optional) 질문 코드의 코드시스템. Form 타입이 'LOINC'일 경우, 기본값은 'LOINC'입니다. */
	@JsonProperty("questionCodeSystem")
	private String qstCs;
	
	@JsonProperty("localQuestionCode")
	private String localQstCd;
	
	private String dataType;
	
	/** 만약 true일 경우, 질문이 아니라 섹션이다. 질문 목록과 섹션 목록의 배열의 속성을 지닌 'item'을 포함 할 수 있다. */
	private boolean header;
	
	private List<LFormUnitQryDTO> units;
	
	@JsonProperty("codingInstructions")
	private String codingInstr;
	
	@JsonProperty("copyrightNotice")
	private String cpyrNot;
	
	@JsonProperty("questionCardinality")
	private String qstCard;
	
	@JsonProperty("answerCardinality")
	private String ansCard;
	
	@JsonProperty("question")
	private String qst;
	
	//private LFormSkipLogicQryDTO skipLogic;
	
	//private String restrictions
	
	//...
}
