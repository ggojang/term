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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;

import co.infoclinic.term.snomedct.api.QryApi;
import co.infoclinic.term.snomedct.model.dto.ConceptViewDTO;
import co.infoclinic.term.snomedct.repository.TransitiveClosureRepository;
import co.infoclinic.term.snomedct.service.ConceptService;
import co.infoclinic.term.snomedct.service.SchemeService;
import co.infoclinic.term.snomedct.repository.custom.ConceptRepositoryCustom;
import co.infoclinic.term.snomedct.utils.ECL2ParserUtil;
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

	@Autowired
	@Qualifier("conceptRepositoryImpl")
	private ConceptRepositoryCustom conceptRepo;


	@ApiOperation(value = "Get Entity List by ECL")
	@RequestMapping(value = QryApi.API_GET_ENTITIES, method = RequestMethod.GET)
	public ResponseEntity<?> getEntityListByECL(
			@RequestParam(value = "ecl", required = true) String ecl,
			@RequestParam(value = QryApi.PARAM_PAGE, required = false, defaultValue = "1") int page,
			@RequestParam(value = QryApi.PARAM_SIZE, required = false, defaultValue = "1000") int size,
			@RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {

		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return ResponseEntity.ok(new ArrayList<>());
		}

		String effectiveTime = schemeSvc.getEffectiveTime(ver);
		try {
			List<ConceptViewDTO> result = evaluateECL(ecl.trim(), effectiveTime, page, size);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
			log.warn("ECL parse error: {}", msg);
			Map<String, String> body = new LinkedHashMap<String, String>();
			body.put("error", "ECL syntax error");
			body.put("message", msg);
			return ResponseEntity.badRequest().body(body);
		}
	}

	// -------------------------------------------------------------------------
	// ECL 평가기 (재귀적으로 AND/OR/MINUS 처리)
	// -------------------------------------------------------------------------

	private List<ConceptViewDTO> evaluateECL(String ecl, String effectiveTime, int page, int size) {
		String type = detectType(ecl);

		if (ECL2ParserUtil.TYPE_CONJUNCTION.equals(type)) {
			return evaluateCompound(ecl, "AND", effectiveTime, page, size, "AND");
		} else if (ECL2ParserUtil.TYPE_DISJUNCTION.equals(type)) {
			return evaluateCompound(ecl, "OR", effectiveTime, page, size, "OR");
		} else if (ECL2ParserUtil.TYPE_EXCLUSION.equals(type)) {
			return evaluateCompound(ecl, "MINUS", effectiveTime, page, size, "MINUS");
		} else if (ECL2ParserUtil.TYPE_DOTTED.equals(type)) {
			return evaluateDotted(ecl, effectiveTime);
		} else if (ECL2ParserUtil.TYPE_COMPOUNDFOCUS.equals(type)) {
			return evaluateCompoundFocus(ecl, effectiveTime, page, size);
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

		// * : refinement — pipe 표기 제거 후 문자열 파싱 (* 를 attributeValue로 허용)
		String eclStripped = ecl.replaceAll("\\s*\\|[^|]*\\|\\s*", " ").replaceAll("\\s+", " ").trim();
		if (eclStripped.startsWith("*")) {
			int ci = eclStripped.indexOf(':');
			if (ci >= 0) {
				return evaluateRefinement(eclStripped.substring(ci + 1).trim(), effectiveTime);
			}
			return new ArrayList<>();
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

	/** operator key + conceptId로 목록 조회 (LIMIT 없이 전체 반환) */
	private List<ConceptViewDTO> evaluateByKeyAndId(String key, String id, String effectiveTime, int page, int size) {
		switch (key) {
			case "DESCENDANTOF":       return conceptService.getAllDescendantList(id, effectiveTime);
			case "DESCENDANTORSELFOF": return conceptService.getAllDescendantListOrSelf(id, effectiveTime);
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
	 * refinement 문자열을 파싱해 속성 조건을 만족하는 개념 목록 반환.
	 *
	 * ECL2 지원:
	 *   - {@code R TYPE_ID = valExpr}     : reverse 속성 (DESTINATION_ID 개념 반환)
	 *   - {@code TYPE_ID = <<valId}        : 값 계층 제약
	 *   - {@code TYPE_ID = *}              : 와일드카드 값 (buildAttrValCondition 처리)
	 */
	@SuppressWarnings("unchecked")
	private List<ConceptViewDTO> evaluateRefinement(String refinementStr, String effectiveTime) {
		Map<String, Object> nonGroup = new LinkedHashMap<>();
		List<ConceptViewDTO> reverseResult = null;

		String[] parts = refinementStr.split(",");
		for (String part : parts) {
			part = part.trim().replaceAll("[{}]", "");
			if (!part.contains("=")) continue;
			String[] kv = part.split("=", 2);
			if (kv.length != 2) continue;

			String attrPart = kv[0].trim();
			String valPart  = kv[1].trim();

			// ECL2: reverse flag (R or r)
			boolean isReverse = attrPart.matches("(?i)r\\s+.*");
			if (isReverse) {
				attrPart = attrPart.replaceFirst("(?i)r\\s+", "").trim();
			}

			// 속성 연산자 (<<, <) 제거
			if (attrPart.startsWith("<<")) attrPart = attrPart.substring(2).trim();
			else if (attrPart.startsWith("<"))  attrPart = attrPart.substring(1).trim();

			// 값 제약 연산자 분리
			String valOperator = "SELF";
			if (valPart.startsWith("<<")) { valOperator = "DESCENDANTORSELFOF"; valPart = valPart.substring(2).trim(); }
			else if (valPart.startsWith("<"))  { valOperator = "DESCENDANTOF";       valPart = valPart.substring(1).trim(); }

			String attrId = attrPart;
			String valId  = valPart;

			if (isReverse) {
				// SOURCE_ID 집합을 구한 뒤 그 DESTINATION_ID 를 반환
				List<String> sourceIds = resolveConstraintToIds(valId, valOperator, effectiveTime);
				List<String> destIds = sourceIds.isEmpty()
						? conceptRepo.findAttrDestIdsBySourcesAll(attrId, effectiveTime)
						: conceptRepo.findAttrDestIdsBySources(sourceIds, attrId, effectiveTime);
				List<ConceptViewDTO> rList = conceptRepo.findConceptViewByIds(destIds, effectiveTime);
				reverseResult = (reverseResult == null) ? rList : applySetOp(reverseResult, rList, "AND");
			} else {
				// 일반 속성: 값 제약 연산자가 있는 경우 expandValue
				if (!"SELF".equals(valOperator) && !"*".equals(valId)) {
					List<String> expandedIds = resolveConstraintToIds(valId, valOperator, effectiveTime);
					// 값 집합을 OR 로 처리: 개별 항목을 nonGroup 에 추가하면 AND 가 됨
					// → 임시 비교를 위해 IN 쿼리 대신 여러 OR 조건이 필요하므로 별도 처리
					List<ConceptViewDTO> attrRes = evaluateAttrWithExpandedValues(attrId, expandedIds, effectiveTime);
					reverseResult = (reverseResult == null) ? attrRes : applySetOp(reverseResult, attrRes, "AND");
				} else {
					nonGroup.put(attrId, valId);
				}
			}
		}

		List<ConceptViewDTO> result = new ArrayList<>();
		if (!nonGroup.isEmpty()) {
			Map<String, Object> paramMap = new LinkedHashMap<>();
			paramMap.put("N", nonGroup);
			result = (List<ConceptViewDTO>) conceptService.getConceptList(paramMap, effectiveTime);
		}
		if (reverseResult != null) {
			result = result.isEmpty() ? reverseResult : applySetOp(result, reverseResult, "AND");
		}
		return result;
	}

	/**
	 * 값 제약(valId + operator)을 개념 ID 목록으로 확장한다.
	 * valId="*" 이면 빈 목록을 반환 (호출 측에서 wildcard 처리).
	 */
	private List<String> resolveConstraintToIds(String valId, String valOperator, String effectiveTime) {
		if ("*".equals(valId)) return new ArrayList<>();
		if ("SELF".equals(valOperator) || "SCTID".equals(valOperator)) {
			return java.util.Collections.singletonList(valId);
		}
		List<ConceptViewDTO> concepts = evaluateByKeyAndId(valOperator, valId, effectiveTime, 1, Integer.MAX_VALUE);
		return concepts.stream().map(ConceptViewDTO::getConceptId).collect(Collectors.toList());
	}

	/** 여러 가능한 DESTINATION_ID 값들에 대해 OR 로 속성 조건 검색 */
	@SuppressWarnings("unchecked")
	private List<ConceptViewDTO> evaluateAttrWithExpandedValues(String attrId, List<String> valueIds, String effectiveTime) {
		if (valueIds.isEmpty()) return new ArrayList<>();
		List<ConceptViewDTO> result = new ArrayList<>();
		for (String vid : valueIds) {
			Map<String, Object> nonGroup = new LinkedHashMap<>();
			nonGroup.put(attrId, vid);
			Map<String, Object> paramMap = new LinkedHashMap<>();
			paramMap.put("N", nonGroup);
			List<ConceptViewDTO> partial = (List<ConceptViewDTO>) conceptService.getConceptList(paramMap, effectiveTime);
			result = applySetOp(result, partial, "OR");
		}
		return result;
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

	/**
	 * ECL2ParserUtil 로 표현식 유형 감지.
	 * ECL2 신규: DOTTED / COMPOUNDFOCUS / 대소문자 무관 AND·OR·MINUS
	 */
	private String detectType(String ecl) {
		try {
			if (ecl.startsWith("^")) return ECL2ParserUtil.TYPE_SIMPLE;
			return ECL2ParserUtil.detectType(ecl);
		} catch (Exception e) {
			return ECL2ParserUtil.TYPE_SIMPLE;
		}
	}

	// ─── ECL2: Dotted expression ─────────────────────────────────────────────

	/**
	 * Dotted 표현식 평가: {@code <<X . TYPE_ID [. TYPE_ID2 ...]}
	 * 각 TYPE_ID 를 따라 SOURCE→DESTINATION 체인을 순차적으로 탐색한다.
	 */
	private List<ConceptViewDTO> evaluateDotted(String ecl, String effectiveTime) {
		String stripped = ecl.replaceAll("\\s*\\|[^|]*\\|\\s*", " ").replaceAll("\\s+", " ").trim();

		// " . " 을 구분자로 분리 (cardinality ".." 와 충돌 없음)
		String[] parts = stripped.split("\\s+\\.\\s+");
		if (parts.length < 2) return new ArrayList<>();

		// 첫 번째 부분: focus 표현식
		List<ConceptViewDTO> sources = evaluateSimple(parts[0].trim(), effectiveTime, 1, Integer.MAX_VALUE);
		if (sources.isEmpty()) return new ArrayList<>();

		List<String> ids = sources.stream().map(ConceptViewDTO::getConceptId).collect(Collectors.toList());

		// 이후 TYPE_ID 체인을 따라 이동
		for (int i = 1; i < parts.length; i++) {
			String typeId = extractSctId(parts[i].trim());
			if (typeId == null) return new ArrayList<>();
			ids = conceptRepo.findAttrDestIdsBySources(ids, typeId, effectiveTime);
			if (ids.isEmpty()) return new ArrayList<>();
		}

		return conceptRepo.findConceptViewByIds(ids, effectiveTime);
	}

	// ─── ECL2: Compound focus (+) ────────────────────────────────────────────

	/**
	 * Compound focus 평가: {@code <<X + <<Y [: refinement]}
	 * 각 focus의 결과를 OR-union 한 뒤 refinement(있으면)를 적용한다.
	 */
	private List<ConceptViewDTO> evaluateCompoundFocus(String ecl, String effectiveTime, int page, int size) {
		String stripped = ecl.replaceAll("\\s*\\|[^|]*\\|\\s*", " ").replaceAll("\\s+", " ").trim();

		// refinement 분리
		String focusPart = stripped;
		String refinementPart = null;
		int colonIdx = indexOfColonOutsideParens(stripped);
		if (colonIdx >= 0) {
			focusPart     = stripped.substring(0, colonIdx).trim();
			refinementPart = stripped.substring(colonIdx + 1).trim();
		}

		// " + " 로 focus 분리
		String[] foci = focusPart.split("\\s+\\+\\s+");
		List<ConceptViewDTO> union = new ArrayList<>();
		for (String f : foci) {
			union = applySetOp(union, evaluateSimple(f.trim(), effectiveTime, page, size), "OR");
		}

		if (refinementPart != null && !refinementPart.isEmpty()) {
			List<ConceptViewDTO> attrResult = evaluateRefinement(refinementPart, effectiveTime);
			union = applySetOp(union, attrResult, "AND");
		}
		return union;
	}

	/** 괄호 밖의 ':' 위치를 찾는다 (compound focus 에서 refinement 분리용) */
	private int indexOfColonOutsideParens(String s) {
		int depth = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '(' || c == '{') depth++;
			else if (c == ')' || c == '}') depth--;
			else if (c == ':' && depth == 0) return i;
		}
		return -1;
	}

	/** 문자열에서 앞부분 operator(<<, <, >>, >, ^) 와 공백을 제거하고 SCTID 또는 * 를 추출 */
	private String extractSctId(String token) {
		String t = token.replaceAll("^(<<|<!|<|>>|>!|>|\\^)\\s*", "").trim();
		if (t.isEmpty()) return null;
		return t;
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
