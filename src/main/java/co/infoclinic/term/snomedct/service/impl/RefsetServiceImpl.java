package co.infoclinic.term.snomedct.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import co.infoclinic.term.common.utils.SNOMEDCTUtils;
import co.infoclinic.term.snomedct.model.entity.Referenceset;
import co.infoclinic.term.snomedct.repository.RefsetRepository;
import co.infoclinic.term.snomedct.service.RefsetService;
import co.infoclinic.term.snomedct.service.TransitiveClosureService;

/**
 * Reference Set 서비스
 */
@Service("RefsetSvc")
@Transactional
public class RefsetServiceImpl implements RefsetService {

	/** Logger */
	Logger log = LoggerFactory.getLogger(RefsetServiceImpl.class);

	@Autowired
	private RefsetRepository referencesetRepository;
	
	/** DI: Transitive Closure service */
	@Autowired
	private TransitiveClosureService tcSvc;

	/* ---------------------------------------- */
	/* Command Service */
	/* ---------------------------------------- */



	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * co.infoclinic.term.snomedct.service.RefsetService#getReferencesetIdList()
	 */
	@Override
	public List<String> getReferencesetIdList() {
		Set<String> refsetIdSet = referencesetRepository.findRefsetId();
		return refsetIdSet != null ? new ArrayList<String>(refsetIdSet) : new ArrayList<String>();
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see co.infoclinic.term.snomedct.service.RefsetService#
	 * getReferencesetIdExistList()
	 */
	@Override
	public List<String> getReferencesetIdExistList() {

		Set<String> existSet = new HashSet<String>();
		List<String> idList = getReferencesetIdList();

		List<String> availableTimes = tcSvc.getAvailableEffectiveTimes();
		String latestEt = availableTimes.isEmpty() ? "" : availableTimes.get(0);

		int idListSize = idList.size();
		for (int i = 0; i < idListSize; i++) {
			String id = idList.get(i);
			List<String> ancestorPaths = tcSvc.getParentPathListByConceptId(id, latestEt);
			// 부모 경로가 1개 존재하는 경우
			if (ancestorPaths != null && ancestorPaths.size() == 1) {
				String ancestorPath = ancestorPaths.get(0);
				// 경로내 Referenceset(900000000000455006)의 위치
				int idx = ancestorPath.indexOf(SNOMEDCTUtils.MetadataType.Referenceset);
				if (idx > 0) {
					int lastSpace = ancestorPath.indexOf("~", idx);
					if (lastSpace == -1)
						existSet.add(id);
					else {
						String trimPath = ancestorPath.substring(lastSpace + 1); // ~제거위함
						if (trimPath.contains("~")) {
							String[] paths = trimPath.split("~");
							for (String p : paths)
								existSet.add(p);
						} else
							existSet.add(trimPath);
					}
				}
			}
		}
		existSet.addAll(idList);
		return existSet != null ? new ArrayList<String>(existSet) : new ArrayList<String>();
	}


	@Override
	public List<Referenceset> getRefsetIds(List<String> referencedComponentIdList,
			List<String> languageReferencesetIdList, String effectiveTime) {
		return referencesetRepository.findByReferencedComponentIdsAndRefsetIdsAndEffectiveTime(
				referencedComponentIdList, languageReferencesetIdList, effectiveTime);
	}
	
}
