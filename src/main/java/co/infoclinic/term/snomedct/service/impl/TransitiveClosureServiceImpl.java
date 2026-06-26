package co.infoclinic.term.snomedct.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.utils.SNOMEDCTComponentTypeEnum;
import co.infoclinic.term.snomedct.repository.TransitiveClosureRepository;
import co.infoclinic.term.snomedct.service.TransitiveClosureService;

/**
 * The Transitive Closure Service
 */
@Service("SCTClosureSvc")
public class TransitiveClosureServiceImpl implements TransitiveClosureService {

	Logger log = LoggerFactory.getLogger(TransitiveClosureServiceImpl.class);

	@Autowired
	private TransitiveClosureRepository tcRepo;

	/** 언어 Refset ID 목록 캐시 (effectiveTime → List) */
	private final Map<String, List<String>> languageRefsetIdCache = new HashMap<>();

	/** TC에 존재하는 effectiveTime 목록 캐시 (앱 기동 시 1회 로드) */
	private volatile List<String> availableTimesCache = null;


	@Override
	public List<String> getAvailableEffectiveTimes() {
		if (availableTimesCache == null) {
			synchronized (this) {
				if (availableTimesCache == null) {
					List<String> times = tcRepo.findDistinctEffectiveTimes();
					availableTimesCache = times != null ? times : new ArrayList<>();
				}
			}
		}
		return availableTimesCache;
	}

	/** TC effectiveTime 캐시를 초기화 (TC 배치 완료 후 호출) */
	public void invalidateAvailableTimesCache() {
		availableTimesCache = null;
	}


	@Override
	public List<String> getPathListByConceptIds(List<String> conceptIds, String effectiveTime) {
		if (conceptIds.isEmpty()) return new ArrayList<>();
		List<String> paths = tcRepo.findPathListByConceptIds(conceptIds, effectiveTime);
		return paths != null ? paths : new ArrayList<>();
	}


	@Override
	public List<String> getPathListByConceptId(String conceptId, String effectiveTime) {
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) return new ArrayList<>();
		List<String> paths = tcRepo.findPathListByConceptId(conceptId, effectiveTime);
		return paths != null ? paths : new ArrayList<>();
	}


	@Override
	public List<String> getParentPathListByConceptId(String conceptId, String effectiveTime) {
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) return new ArrayList<>();
		List<String> parentPaths = tcRepo.findParentPathListByConceptId(conceptId, effectiveTime);
		return parentPaths != null ? parentPaths : new ArrayList<>();
	}


	// 국제 표준 Language Refset ID (TC 유무와 무관하게 항상 포함)
	private static final List<String> DEFAULT_LANG_REFSET_IDS = Arrays.asList(
		"900000000000508004", // Great Britain English language reference set
		"900000000000509007"  // United States of America English language reference set
	);

	@Override
	public List<String> getLanguageRefsetIdList(String effectiveTime) {
		if (languageRefsetIdCache.containsKey(effectiveTime)) {
			return languageRefsetIdCache.get(effectiveTime);
		}
		List<String> langRefsetIds = tcRepo.findLanguageRefsetIdList(effectiveTime);
		if (langRefsetIds == null || langRefsetIds.isEmpty()) {
			// TC가 없는 날짜이거나 TC에서 Language Refset을 찾지 못한 경우 표준 고정값 사용
			langRefsetIds = new ArrayList<>(DEFAULT_LANG_REFSET_IDS);
		}
		if (!langRefsetIds.contains("247781000300103")) {
			langRefsetIds.add("247781000300103"); // KR language refset
		}
		languageRefsetIdCache.put(effectiveTime, langRefsetIds);
		return langRefsetIds;
	}


	@Override
	public Map<String, Integer> getCountMapByConceptId(String conceptId, String effectiveTime) {
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) return new HashMap<>();
		Map<String, Integer> countMap = new HashMap<>();
		int childrenCount = tcRepo.findChildrenCountByConceptId(conceptId, effectiveTime);
		int descendantCount = tcRepo.findDescendantCountByConceptId(conceptId, effectiveTime);
		countMap.put("Children", childrenCount);
		countMap.put("Descendant", descendantCount);
		return countMap;
	}


	@Override
	public int isSubsumption(String criteriaId, String conceptId, String effectiveTime) {
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(criteriaId)
				|| !SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) return 0;
		return tcRepo.findCountByCriteriaIdAndConceptId(criteriaId, conceptId, effectiveTime);
	}


	@Override
	public int getDescendantCount(String conceptId, String effectiveTime) {
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) return 0;
		return tcRepo.findDescendantCountByConceptId(conceptId, effectiveTime);
	}


	@Override
	public List<String> getAncestorIdWithFocusConceptIdSet(String conceptId, String effectiveTime) {
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) return new ArrayList<>();

		Set<String> ancestorIdSet = new HashSet<>();
		List<String> ancestorPathList = getParentPathListByConceptId(conceptId, effectiveTime);
		if (ancestorPathList != null) {
			for (String ancestorPath : ancestorPathList) {
				if (ancestorPath.indexOf("~") != -1) {
					ancestorIdSet.addAll(Arrays.asList(ancestorPath.split("~")));
				} else {
					ancestorIdSet.add(ancestorPath);
				}
			}
		}
		ancestorIdSet.add(conceptId);
		return new ArrayList<>(ancestorIdSet);
	}
}
