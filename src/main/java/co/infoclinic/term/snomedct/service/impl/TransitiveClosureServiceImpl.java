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
import co.infoclinic.term.snomedct.model.entity.TransitiveClosure;
import co.infoclinic.term.snomedct.repository.TransitiveClosureRepository;
import co.infoclinic.term.snomedct.service.TransitiveClosureService;

/**
 * The Transitive Closure Service
 */
@Service("SCTClosureSvc")
public class TransitiveClosureServiceImpl implements TransitiveClosureService {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(TransitiveClosureServiceImpl.class);
	
	/** DI: TransitiveClosure repository */
	@Autowired
	private TransitiveClosureRepository tcRepo;

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.TransitiveClosureService#getPathListByConceptIds(java.util.List)
	 */
	@Override
	public List<String> getPathListByConceptIds(List<String> conceptIds) {
		List<String> paths = null;
		if (conceptIds.size() > 0) {
			paths =  tcRepo.findPathListByConceptIds(conceptIds);
		}
		
		return paths != null ? paths:new ArrayList<String>();
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.TransitiveClosureService#getPathListByConceptId(java.lang.String)
	 */
	@Override
	public List<String> getPathListByConceptId(String conceptId) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ArrayList<String>();
		}
		
		List<String> paths = tcRepo.findPathListByConceptId(conceptId);
		return paths != null ? paths:new ArrayList<String>();
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.TransitiveClosureService#getParentPathListByConceptId(java.lang.String)
	 */
	@Override
	public List<String> getParentPathListByConceptId(String conceptId) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ArrayList<String>();
		}
				
		List<String> parentPaths = tcRepo.findParentPathListByConceptId(conceptId);
		return parentPaths != null ? parentPaths:new ArrayList<String>();
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.TransitiveClosureService#getLanguageRefsetIdList()
	 */
	@Override
	public List<String> getLanguageRefsetIdList() {
		List<String> langRefsetIds = tcRepo.findLanguageRefsetIdList();
		
		if (langRefsetIds == null) {
			langRefsetIds = new ArrayList<String>();
		}
		// Korean Language Refset Id
		langRefsetIds.add("247781000300103");
		
		return langRefsetIds;// != null ? langRefsetIds:new ArrayList<String>();
	}


	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.TransitiveClosureService#getCountMapByConceptId(java.lang.String)
	 */
	@Override
	public Map<String, Integer> getCountMapByConceptId(String conceptId) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new HashMap<String, Integer>();
		}
		
		Map<String, Integer> countMap = new HashMap<String, Integer>();
		
		int childrenCount = 0;
		int descendantCount = 0;
		
		TransitiveClosure entity = tcRepo.findByConceptId(conceptId);
		if (entity != null) {
			childrenCount = entity.getChildrenCount();
			descendantCount = entity.getDescendantCount();
		}
		
		countMap.put("Children", childrenCount);
		countMap.put("Descendant", descendantCount);
		
		return countMap;
	}
	

	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.TransitiveClosureService#isSubsumption(java.lang.String, java.lang.String)
	 */
	@Override
	public int isSubsumption(String criteriaId, String conceptId) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(criteriaId) || !SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return 0;
		}
		
		return tcRepo.findCountByCriteriaIdAndConceptId(criteriaId, conceptId);
	}


	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.TransitiveClosureService#getDescendantCount(java.lang.String)
	 */
	@Override
	public int getDescendantCount(String conceptId) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return 0;
		}
				
		return tcRepo.findDescendantCountByConceptId(conceptId);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.TransitiveClosureService#getAncestorIdWithFocusConceptIdSet(java.lang.String)
	 */
	@Override
	public List<String> getAncestorIdWithFocusConceptIdSet(final String conceptId) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ArrayList<String>();
		}

		Set<String> ancestorIdWithFocusConceptIdSet = new HashSet<String>();
		List<String> ancestorPathList = getParentPathListByConceptId(conceptId);
		if (ancestorPathList != null) {
			ancestorIdWithFocusConceptIdSet = new HashSet<String>();
			List<String> ancestorIdList = null;
			int ancestorPathListSize = ancestorPathList.size();
			for (int i = 0; i < ancestorPathListSize; i++) {
				String ancestorPath = ancestorPathList.get(i);
				if (ancestorPath.indexOf("~") != -1) {
					String[] ancestorIdArray = ancestorPath.split("~");
					ancestorIdList = Arrays.asList(ancestorIdArray);
					ancestorIdWithFocusConceptIdSet.addAll(ancestorIdList);
				} else {
					ancestorIdWithFocusConceptIdSet.add(ancestorPath);
				}
			}
		}
		ancestorIdWithFocusConceptIdSet.add(conceptId);
		return new ArrayList<String>(ancestorIdWithFocusConceptIdSet);
	}

}
