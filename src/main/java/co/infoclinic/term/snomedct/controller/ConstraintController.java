package co.infoclinic.term.snomedct.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.infoclinic.term.snomedct.api.QryApi;
import co.infoclinic.term.snomedct.model.dto.ConceptViewDTO;
import co.infoclinic.term.snomedct.service.ConceptService;
import co.infoclinic.term.snomedct.service.SchemeService;
import co.infoclinic.term.snomedct.utils.ECL1ParserUtil;
import co.infoclinic.term.snomedct.utils.ECLParserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * ECL(Expression Constraint Language) API 컨트롤러
 * <p>
 * 지원 연산자:
 *   <  (descendantOf)       << (descendantOrSelfOf)
 *   <! (childOf)           <<! (childOrSelfOf)
 *   >  (ancestorOf)         >> (ancestorOrSelfOf)
 *   >! (parentOf)          >>! (parentOrSelfOf)
 *   ^  (memberOf)
 *   AND / OR / MINUS 복합 표현식
 *   *:attr=val  속성 제약 표현식
 * </p>
 */
@Api(value = "Constraint", description = "Constraint", tags = QryApi.API_TAGS_CONSTRAINT)
@RestController(value = "SCTCnstCtrl")
public class ConstraintController {

	Logger log = LoggerFactory.getLogger(ConstraintController.class);

	@Autowired
	private ConceptService conceptService;

	@Autowired
	private SchemeService schemeSvc;


	@ApiOperation(value = "Get Entity List by ECL")
	@RequestMapping(value = QryApi.API_GET_ENTITIES, method = RequestMethod.GET)
	public List<ConceptViewDTO> getEntityListByECL(
			@RequestParam(value = "ecl", required = true) String ecl,
			@RequestParam(value = QryApi.PARAM_PAGE, required = false, defaultValue = "1") int page,
			@RequestParam(value = QryApi.PARAM_SIZE, required = false, defaultValue = "1000") int size,
			@RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {

		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return new ArrayList<>();
		}

		String effectiveTime = schemeSvc.getEffectiveTime(ver);
		try {
			return evaluateECL(ecl.trim(), effectiveTime, page, size);
		} catch (Exception e) {
			log.warn("ECL parse error: {}", e.getMessage());
			return new ArrayList<>();
		}
	}

	// -------------------------------------------------------------------------
	// ECL 평가기 (재귀적으로 AND/OR/MINUS 처리)
	// -------------------------------------------------------------------------

	private List<ConceptViewDTO> evaluateECL(String ecl, String effectiveTime, int page, int size) {
		String type = detectType(ecl);

		if ("CONJUNCTION".equals(type)) {
			return evaluateCompound(ecl, "AND", effectiveTime, page, size, "AND");
		} else if ("DISJUNCTION".equals(type)) {
			return evaluateCompound(ecl, "OR", effectiveTime, page, size, "OR");
		} else if ("EXCLUSION".equals(type)) {
			return evaluateCompound(ecl, "MINUS", effectiveTime, page, size, "MINUS");
		} else {
			return evaluateSimple(ecl.trim(), effectiveTime, page, size);
		}
	}

	/** 복합 표현식: 공백+키워드+공백으로 분리하여 set 연산 적용 */
	private List<ConceptViewDTO> evaluateCompound(String ecl, String keyword,
			String effectiveTime, int page, int size, String op) {
		// 대소문자 무관 분리
		String[] parts = ecl.split("(?i)\\s+" + keyword + "\\s+");
		if (parts.length < 2) return evaluateSimple(ecl, effectiveTime, page, size);

		List<ConceptViewDTO> result = evaluateSimple(parts[0].trim(), effectiveTime, page, size);
		for (int i = 1; i < parts.length; i++) {
			List<ConceptViewDTO> right = evaluateSimple(parts[i].trim(), effectiveTime, page, size);
			result = applySetOp(result, right, op);
		}
		return result;
	}

	/** 단순 표현식 실행 */
	@SuppressWarnings("unchecked")
	private List<ConceptViewDTO> evaluateSimple(String ecl, String effectiveTime, int page, int size) {
		// ^ memberOf
		if (ecl.startsWith("^")) {
			String refsetId = ecl.substring(1).trim().split("\\s+")[0];
			return conceptService.getMemberOfList(refsetId, effectiveTime);
		}

		// 속성 제약 (* :attr=val 형태)
		Map<String, Object> paramMap = ECLParserUtil.getParamExpression(ecl);
		if (paramMap == null) return new ArrayList<>();

		if (paramMap.get("G") != null || paramMap.get("N") != null) {
			return (List<ConceptViewDTO>) conceptService.getConceptList(paramMap, effectiveTime);
		}

		String key = paramMap.keySet().iterator().next();
		String id  = paramMap.get(key).toString();

		switch (key) {
			case "DESCENDANTOF":       return conceptService.getDescendantList(id, effectiveTime, page, size).getContent();
			case "DESCENDANTORSELFOF": return conceptService.getDescendantListOrSelf(id, effectiveTime, page, size).getContent();
			case "CHILDOF":            return conceptService.getChildren(id, effectiveTime);
			case "CHILDORSELFOF":      return conceptService.getChildrenOrSelf(id, effectiveTime);
			case "ANCESTOF":           return conceptService.getAncestorList(id, effectiveTime, page, size).getContent();
			case "ANCESTORSELFOF":     return conceptService.getAncestorListOrSelf(id, effectiveTime, page, size).getContent();
			case "PARENTOF":           return conceptService.getParentList(id, effectiveTime);
			case "PARENTORSELFOF":     return conceptService.getParentListOrSelf(id, effectiveTime);
			case "SCTID": {
				List<ConceptViewDTO> r = new ArrayList<>();
				ConceptViewDTO c = conceptService.getConcept(id, effectiveTime);
				if (c != null) r.add(c);
				return r;
			}
			default: return new ArrayList<>();
		}
	}

	/** ECL1ParserUtil로 표현식 유형 감지 */
	private String detectType(String ecl) {
		try {
			// ^ memberOf는 별도 처리
			if (ecl.startsWith("^")) return "SIMPLE";
			ECL1ParserUtil.parseExpression(ecl);
			// parseExpression 호출 후 트리 검사 대신 키워드로 간이 판별
			String upper = ecl.toUpperCase();
			if (upper.matches(".*\\sAND\\s.*"))   return "CONJUNCTION";
			if (upper.matches(".*\\sOR\\s.*"))    return "DISJUNCTION";
			if (upper.matches(".*\\sMINUS\\s.*")) return "EXCLUSION";
		} catch (Exception e) {
			// fall through to SIMPLE
		}
		return "SIMPLE";
	}

	// -------------------------------------------------------------------------
	// Set 연산 (conceptId 기준)
	// -------------------------------------------------------------------------

	private List<ConceptViewDTO> applySetOp(List<ConceptViewDTO> left, List<ConceptViewDTO> right, String op) {
		// LinkedHashMap으로 순서 유지 + 중복 제거
		Map<String, ConceptViewDTO> leftMap = toMap(left);
		Map<String, ConceptViewDTO> rightMap = toMap(right);

		Set<String> keys = new LinkedHashSet<>();
		switch (op) {
			case "AND":
				keys.addAll(leftMap.keySet());
				keys.retainAll(rightMap.keySet());
				break;
			case "OR":
				keys.addAll(leftMap.keySet());
				keys.addAll(rightMap.keySet());
				break;
			case "MINUS":
				keys.addAll(leftMap.keySet());
				keys.removeAll(rightMap.keySet());
				break;
		}
		Map<String, ConceptViewDTO> merged = new LinkedHashMap<>(leftMap);
		merged.putAll(rightMap);
		return keys.stream().map(merged::get).collect(Collectors.toList());
	}

	private Map<String, ConceptViewDTO> toMap(List<ConceptViewDTO> list) {
		Map<String, ConceptViewDTO> map = new LinkedHashMap<>();
		if (list != null) list.forEach(c -> { if (c != null) map.put(c.getConceptId(), c); });
		return map;
	}
}
