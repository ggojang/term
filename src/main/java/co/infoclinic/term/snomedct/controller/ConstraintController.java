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
import co.infoclinic.term.snomedct.repository.TransitiveClosureRepository;
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

	@Autowired
	private TransitiveClosureRepository tcRepo;


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

		// refinement 포함 여부 확인 (e.g. "64572001:363698007=66754008")
		if (id.contains(":")) {
			int colonIdx = id.indexOf(":");
			String focusId       = id.substring(0, colonIdx).trim();
			String refinementStr = id.substring(colonIdx + 1).trim();
			// 속성 조건을 만족하는 개념 목록을 먼저 구한 후 (보통 소수),
			// 각 개념이 focus concept의 constraint 범위에 속하는지 TC PATH로 검증
			List<ConceptViewDTO> attrList = evaluateRefinement(refinementStr, effectiveTime);
			return filterByConstraint(attrList, key, focusId, effectiveTime);
		}

		return evaluateByKeyAndId(key, id, effectiveTime, page, size);
	}

	/** operator key + conceptId로 목록 조회 */
	private List<ConceptViewDTO> evaluateByKeyAndId(String key, String id, String effectiveTime, int page, int size) {
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

	/**
	 * refinement 문자열 "attrId=valId,attrId2=valId2" 을 파싱해
	 * 속성 조건을 만족하는 개념 목록 반환
	 */
	@SuppressWarnings("unchecked")
	private List<ConceptViewDTO> evaluateRefinement(String refinementStr, String effectiveTime) {
		// 여러 속성이 콤마로 연결된 경우 각각 AND 교집합
		// 그룹 처리({...})는 일단 무시하고 non-group 속성만 처리
		Map<String, Object> nonGroup = new LinkedHashMap<>();
		String[] parts = refinementStr.split(",");
		for (String part : parts) {
			part = part.trim().replaceAll("\\{", "").replaceAll("\\}", "");
			if (!part.contains("=")) continue;
			String[] kv = part.split("=", 2);
			if (kv.length == 2) {
				nonGroup.put(kv[0].trim(), kv[1].trim());
			}
		}
		if (nonGroup.isEmpty()) return new ArrayList<>();
		Map<String, Object> paramMap = new LinkedHashMap<>();
		paramMap.put("N", nonGroup);
		return (List<ConceptViewDTO>) conceptService.getConceptList(paramMap, effectiveTime);
	}

	/**
	 * 속성 결과 목록을 constraint operator + focusId 기준으로 TC PATH 검증하여 필터링.
	 * 페이지 제한 없이 정확한 포함 여부를 판단한다.
	 */
	private List<ConceptViewDTO> filterByConstraint(List<ConceptViewDTO> candidates, String key, String focusId, String effectiveTime) {
		List<ConceptViewDTO> result = new ArrayList<>();
		for (ConceptViewDTO c : candidates) {
			String cid = c.getConceptId();
			boolean pass = false;
			switch (key) {
				case "DESCENDANTOF":
					pass = !cid.equals(focusId) && tcRepo.findCountByCriteriaIdAndConceptId(focusId, cid) > 0;
					break;
				case "DESCENDANTORSELFOF":
					pass = cid.equals(focusId) || tcRepo.findCountByCriteriaIdAndConceptId(focusId, cid) > 0;
					break;
				case "CHILDOF": {
					List<ConceptViewDTO> parents = conceptService.getParentList(cid, effectiveTime);
					pass = parents.stream().anyMatch(p -> focusId.equals(p.getConceptId()));
					break;
				}
				case "CHILDORSELFOF": {
					if (cid.equals(focusId)) { pass = true; break; }
					List<ConceptViewDTO> parents = conceptService.getParentList(cid, effectiveTime);
					pass = parents.stream().anyMatch(p -> focusId.equals(p.getConceptId()));
					break;
				}
				case "SCTID":
					pass = cid.equals(focusId);
					break;
				default:
					pass = true;
					break;
			}
			if (pass) result.add(c);
		}
		return result;
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
