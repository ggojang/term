package co.infoclinic.term.loinc.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.utils.LOINCUtils;
import co.infoclinic.term.loinc.model.converter.HierarchyConverter;
import co.infoclinic.term.loinc.model.dto.HierarchyDTO;
import co.infoclinic.term.loinc.model.entity.Hierarchy;
import co.infoclinic.term.loinc.repository.HierarchyRepository;
import co.infoclinic.term.loinc.service.HierarchyService;

/**
 * LOINC Class & Laboratory Parts(Multi-axial) Hierarchy Service
 */
@Service("LncHierSvc")
public class HierarchyServiceImpl implements HierarchyService {

	/** Logger */
	Logger log = LoggerFactory.getLogger(HierarchyService.class);
	
	/** DI: Hierarchy repository */
	@Autowired
	private HierarchyRepository hierRepo;

	
	
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	/**
	 * 코드의 부모 목록을 반환하는 메소드
	 * 
	 * @param code LOINC code
	 */
	@Override
	public List<HierarchyDTO> getParentListByCode(String code) {
		if (StringUtils.isEmpty(code) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return new ArrayList<HierarchyDTO>();
		}
		
		List<HierarchyDTO> dtos;

		// 부모 목록
		List<Hierarchy> entities = hierRepo.findParentListByCode(code);

		// Entity List to DTO List
		dtos = convertToDTOList(entities);
		// 조건부 정렬(계층구조가 Class일 경우에만 이름순 정렬)
		//dtos = conditionalSortByName(dtos);

		return dtos;
	}

	
	/**
	 * 코드의 자식 목록을 반환하는 메소드
	 * 
	 * @param code LOINC code
	 * @param path 루트까지의 경로
	 * @param lang 언어; format: {ISO Language}-{ISO Country}; e.g. ko-KR(한국어),zh-CN(중국 대륙 간체)
	 */
	@Override
	public List<HierarchyDTO> getChildren(String code, String path, String lang) {
		if (StringUtils.isEmpty(code)) {
			return new ArrayList<HierarchyDTO>();
		}

		// group 키워드: hierarchy_lg 테이블에서 조회
		if ("group".equalsIgnoreCase(code)) {
			List<Hierarchy> entities = hierRepo.findChildrenInLGHierarchy("GROUP");
			return convertToDTOList(entities);
		}

		// parts 키워드: LP432695-7이 PARTS 루트
		if ("parts".equalsIgnoreCase(code)) {
			List<Hierarchy> entities = hierRepo.findChildrenByCode("LP432695-7");
			return convertToDTOList(entities);
		}

		// group 하위 노드(카테고리 이름)
		if (!code.matches(LOINCUtils.CODE_PATTERN) || code.contains(" ")) {
			List<Hierarchy> entities = hierRepo.findChildrenInLGHierarchy(code);
			return convertToDTOList(entities);
		}

		// 자식 목록
		List<HierarchyDTO> dtos;
		List<Hierarchy> entities;

		// lang(언어)가 없는 경우
		if ("".equals(lang)) {
			
			// path가 있는 경우
			if (!"".equals(path)) {
				String p = path + "~" + code;
				entities = hierRepo.findChildrenByCodeAndPath(code, p);
			}
			// path가 없는 경우
			else {
				entities = hierRepo.findChildrenByCode(code);
			}
			
		}
		// lang(언어)가 있는 경우
		else {
			
			// [ "ISO Language", "ISO Country"]; e.g. [ "ko", "KR" ]
			String isoCodes[] = lang.split("-");
			// ISO Language; e.g. ko
			String isoLang = isoCodes[0];
			// ISO Country; e.g. KR
			String isoCty =  isoCodes[1];
			
			
			// path가 있는 경우
			if (!"".equals(path)) {
				String p = path + "~" + code;
				entities = hierRepo.findChildrenByCodeAndPathAndLangAndCountry(code, p, isoLang, isoCty);
			}
			// path가 없는 경우
			else {
				entities = hierRepo.findChildrenByCodeAndLangAndCountry(code, isoLang, isoCty);
			}
		}

		// Entity List to DTO List
		dtos = convertToDTOList(entities);
		// 조건부 정렬(계층구조가 Class일 경우에만 이름순 정렬)
		//dtos = conditionalSortByName(dtos);

		return dtos;
	}
	
	
	/**
	 * Entity 자신을 포함한 하위 목록
	 * 
	 * @param code 
	 */
	@Override
	public List<HierarchyDTO> getDescendantList(String code, String path, boolean inclSelf) {
		if (StringUtils.isEmpty(code) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return new ArrayList<HierarchyDTO>();
		}
		
		// 자식 목록
		List<HierarchyDTO> dtos;
		List<Hierarchy> entities;

		// path가 있는 경우
		if (!"".equals(path)) {
			String p = path + "~" + code;
			if (inclSelf) {
				entities = hierRepo.findDescendantOrSelfListByCodeAndPath(code, p);
			} else {
				entities = hierRepo.findDescendantListByCodeAndPath(code, p);
			}
		}
		// path가 없는 경우
		else {
			if (inclSelf) {
				entities = hierRepo.findDescendantOrSelfListByCode(code);
			} else {
				entities = hierRepo.findDescendantListByCode(code);
			}
		}

		// Entity List to DTO List
		dtos = convertToDTOList(entities);
		// 조건부 정렬(계층구조가 Class일 경우에만 이름순 정렬)
		//dtos = conditionalSortByName(dtos);

		return dtos;
	}

	
	/**
	 * Entity의 상위 목록
	 * 
	 * @param code
	 */
	@Override
	public List<HierarchyDTO> getAncestorList(String code, boolean inclSelf) {
		if (StringUtils.isEmpty(code) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return new ArrayList<HierarchyDTO>();
		}
		
		List<HierarchyDTO> dtos;
		List<Hierarchy> entities = hierRepo.findAncestorListByCodeAndInclSelf(code, inclSelf);

		// Entity List to DTO List
		dtos = convertToDTOList(entities);

		return dtos;
	}
	

	/**
	 * 코드의 계층 목록
	 * 
	 * @param code LOINC code
	 */
	@Override
	public List<HierarchyDTO> getPathListByCode(String code) {
		if (StringUtils.isEmpty(code) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return new ArrayList<HierarchyDTO>();
		}
		
		List<HierarchyDTO> dtos;
		List<Hierarchy> entities = hierRepo.findByCode(code);

		// Entity List to DTO List
		dtos = convertToDTOList(entities);

		return dtos;
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.service.HierarchyService#isSubsumptionTest(java.lang.String, java.lang.String)
	 */
	@Override
	public int isSubsumptionTest(String criteriaCode, String code) {
		if (StringUtils.isEmpty(criteriaCode) || StringUtils.isEmpty(code) || !criteriaCode.matches(LOINCUtils.CODE_PATTERN) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return 0;
		}
		
		return hierRepo.findCountByCriteriaCodeAndCode(criteriaCode, code);
	}
	
	
	
	
	// ------------------------------
	// Private methods
	// ------------------------------

	/**
	 * Hierarchy Entity 목록을 Hierarchy DTO 목록으로 변환
	 * 
	 * @param entities Entity 목록
	 * @return
	 */
	private List<HierarchyDTO> convertToDTOList(List<Hierarchy> entities) {
		return HierarchyConverter.toDTOList(entities);
	}

	
	/**
	 * 조건부 이름순 정렬; 계층구조가 Class의 하위일 경우
	 * 
	 * @param dtos
	 * @return
	 */
	private List<HierarchyDTO> conditionalSortByName(List<HierarchyDTO> dtos) {
		// CLASS 타입의 계층구조일 경우에는 Sequence가 적용되지 않으므로 이름순으로 정렬한다.
		if (dtos.size() > 0 && dtos.get(0).getPath().contains("CLASS")) {
			sortByName(dtos);
		}

		return dtos;
	}

	
	/**
	 * 이름순 정렬
	 * 
	 * @param dtos
	 * @return
	 */
	private List<HierarchyDTO> sortByName(List<HierarchyDTO> dtos) {
		Collections.sort(dtos, new Comparator<HierarchyDTO>() {
			@Override
			public int compare(HierarchyDTO o1, HierarchyDTO o2) {
				// TODO Auto-generated method stub
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		return dtos;
	}

}
