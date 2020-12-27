package co.infoclinic.term.snomedct.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.infoclinic.term.common.utils.SNOMEDCTComponentTypeEnum;
import co.infoclinic.term.snomedct.model.converter.MrcmConverter;
import co.infoclinic.term.snomedct.model.dto.DefiningAttributeDTO;
import co.infoclinic.term.snomedct.model.dto.DefiningRangeDTO;
import co.infoclinic.term.snomedct.model.dto.TermSearchResult;
import co.infoclinic.term.snomedct.model.entity.Description;
import co.infoclinic.term.snomedct.model.entity.MrcmConstraints;
import co.infoclinic.term.snomedct.repository.MrcmRepository;
import co.infoclinic.term.snomedct.service.MrcmService;
import co.infoclinic.term.snomedct.service.SearchService;
import co.infoclinic.term.snomedct.service.TransitiveClosureService;

import co.infoclinic.term.snomedct.model.entity.Description;
import co.infoclinic.term.snomedct.repository.DescriptionRepository;

import co.infoclinic.term.snomedct.model.entity.Scheme;
import co.infoclinic.term.snomedct.repository.SchemeRepository;


@Service
@Transactional
public class MrcmServiceImpl implements MrcmService {
	
	static final Logger log = LoggerFactory.getLogger(MrcmServiceImpl.class);
	
	@Autowired
	private MrcmRepository mrcmRepository;
	@Autowired
	private SearchService srchSvc;
	@Autowired
	private TransitiveClosureService tcSvc;
	
	@Autowired
	private SchemeRepository schemeRepository;
	@Autowired
	private DescriptionRepository descriptionRepository;
	

	@Override
	public List<DefiningAttributeDTO> getSanctionedAttributeIdNameList(String conceptId) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ArrayList<DefiningAttributeDTO>();
		}
		
		List<DefiningAttributeDTO> definingAttributeDTOList = new ArrayList<DefiningAttributeDTO>();
		
		DefiningRangeDTO range = null;
		List<DefiningRangeDTO> rangeList = null;
    	Map<String, String> maps = new HashMap<String, String>();
    	Map<String, List<DefiningRangeDTO>> valueMap = new HashMap<String, List<DefiningRangeDTO>>();
    	List<MrcmConstraints> mrcmList = mrcmRepository.findBySourceId(conceptId);
  
    	
    	for(MrcmConstraints constraint : mrcmList) {
    		maps.put(constraint.getAttributeId(), constraint.getAttributeName());
    		
    		rangeList = valueMap.get(constraint.getAttributeId());
    		if (rangeList == null) {
    			rangeList = new ArrayList<DefiningRangeDTO>();
    			valueMap.put(constraint.getAttributeId(), rangeList);
    		}
    		
    		range = new DefiningRangeDTO();
    		range.setId(constraint.getValueId());
    		range.setName(constraint.getValueName());
 
    		rangeList.add(range);
        }
    	
    	DefiningAttributeDTO definingAttributeDTO = null;
    	Iterator<String> itr = maps.keySet().iterator();
    	while (itr.hasNext()) {
    		String key = itr.next();
    		String value = maps.get(key);
    		    		
    		definingAttributeDTO = new DefiningAttributeDTO();
    		definingAttributeDTO.setId(key);
    		definingAttributeDTO.setName(value);
    		definingAttributeDTO.setRanges(valueMap.get(key));
    		
    		definingAttributeDTOList.add(definingAttributeDTO);
    	}
    	return definingAttributeDTOList;
	}
	

	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.MrcmService#getAllowDefiningAttributeList(java.lang.String)
	 */
	@Override
	public List<DefiningAttributeDTO> getAllowDefiningAttributeList(String conceptId) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ArrayList<DefiningAttributeDTO>();
		}
		
		List<DefiningAttributeDTO> allowDefiningAttributeList = null;
		
		// 1. 13887500(최상위 컨셉:SNOMED CT Concept) 부터 파라메터 conceptId까지의 경로에 해당하는 모든 컨셉아이디 리스트 구성
		List<String> ancestorIdWithFocusConceptIdSet = tcSvc.getAncestorIdWithFocusConceptIdSet(conceptId);
		
		// start // 20190919
		// 20190731부터 clinical finding과 disorder 그리고 procedure와 evaluation procedure의 MRCM이 각각존재
		// semantic tag이 disorder 또는 evaluation procedure인 경우 clinical finding과 procedure 삭제
		//
		// 20200616 
		// React hook으로 재개발 중에 finding/procedure with explicit context 이하에서 Subject relationship context (attribute),   
		// Temporal context (attribute) 메인의 range값 person과 Temporal context value (qualifier value)이 중복되어 2019091와 같이 조치
		//
		// DescriptionRepository 함수사용 
		//
		// List<Description> findByConceptIdAndEffectiveTime(@Param("conceptId") String conceptId,@Param("effectiveTime") String effectiveTime);
		// typeId : 900000000000003001 == FSN , term = e.g appendicitis (disorder)
		
		Scheme scheme = schemeRepository.findLatest();
		List<Description> descriptionList = descriptionRepository.findByConceptIdAndEffectiveTime(conceptId, scheme.getDate());
		
		String semanticTag = "";
		for (Description desc : descriptionList) {
			if (desc.getTypeId().equals("900000000000003001")) {
				Pattern STPattern = Pattern.compile("\\([a-z ]+\\)$");
				Matcher STMatcher = STPattern.matcher(desc.getTerm());
				if(STMatcher.find()){
					semanticTag = STMatcher.group(); // (disorder)
				}
			}
		}
		
		if (semanticTag.equals("(disorder)")) {
			ancestorIdWithFocusConceptIdSet.remove("404684003"); // clinical finding
			//System.out.println("ancesterId: " + ancestorIdWithFocusConceptIdSet);
		} else if (semanticTag.equals("(procedure)")) {
			if (ancestorIdWithFocusConceptIdSet.contains("386053000")) { // evaluation procedure
				ancestorIdWithFocusConceptIdSet.remove("71388002"); // procedure
				//System.out.println("ancesterId: " + ancestorIdWithFocusConceptIdSet);
			} else if (ancestorIdWithFocusConceptIdSet.contains("387713003")) { // surgical procedure
				ancestorIdWithFocusConceptIdSet.remove("71388002"); // procedure				
			}
		} else if (semanticTag.equals("(situation)")) { // situation
			if (ancestorIdWithFocusConceptIdSet.contains("413350009")) { // 413350009 | Finding with explicit context |
				ancestorIdWithFocusConceptIdSet.remove("243796009"); // situation with explicit context
			} else if (ancestorIdWithFocusConceptIdSet.contains("129125009")) { // 129125009 | Procedure with explicit context |
				ancestorIdWithFocusConceptIdSet.remove("243796009"); // situation with explicit context				
			}
		}
		// end // 20190919
		
		
		// 2. 경로에 대상이되는 AttributeDTO 리스트 가져오기
		allowDefiningAttributeList = getSanctionedAttributeIdNameList(ancestorIdWithFocusConceptIdSet);
		
		return allowDefiningAttributeList;
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.MrcmService#getSanctionedAttributeIdNameList(java.util.List)
	 */
	@Override
	public List<DefiningAttributeDTO> getSanctionedAttributeIdNameList(List<String> conceptIdList) {
		if (conceptIdList == null || conceptIdList.isEmpty()) {
			return null;
		}
		
		List<MrcmConstraints> mrcmList = mrcmRepository.findBySourceIdList(conceptIdList);
	
		return MrcmConverter.mapEntitiesIntoDTOList(mrcmList);
	}

	/*
	 * MRCM Attribute의 Range와 검색어에 대한 결과를 반환하는 메소드 
	 * 
	 * @param attr tig p272에 있는 속성 아이디
	 * @param q 검색어 
	 * @param size 불러올 최대 크기
	 * 
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.MrcmService#getValueList(java.lang.String, java.lang.String)
	 */
	@Override
	public List<TermSearchResult> getValueList(String attr, String q, int size) {
		return srchSvc.getValueListByMrcmAttr(attr, q, size);
	}

	
}
