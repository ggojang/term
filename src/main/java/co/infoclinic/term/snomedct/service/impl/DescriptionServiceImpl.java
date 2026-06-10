package co.infoclinic.term.snomedct.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import co.infoclinic.term.common.utils.SNOMEDCTComponentTypeEnum;
import co.infoclinic.term.common.utils.SNOMEDCTIdentifierGenerator;
import co.infoclinic.term.snomedct.model.converter.DescriptionConverter;
import co.infoclinic.term.snomedct.model.converter.RefsetMemberConverter;
import co.infoclinic.term.snomedct.model.dto.DescriptionDTO;
import co.infoclinic.term.snomedct.model.dto.LanguageRefsetDTO;
import co.infoclinic.term.snomedct.model.entity.Description;
import co.infoclinic.term.snomedct.model.entity.Referenceset;
import co.infoclinic.term.snomedct.repository.DescriptionRepository;
import co.infoclinic.term.snomedct.service.DescriptionService;
import co.infoclinic.term.snomedct.service.RefsetMemberService;
import co.infoclinic.term.snomedct.service.RefsetService;
import co.infoclinic.term.snomedct.service.TransitiveClosureService;

/**
 * The Description Service
 */
@Service("SCTDescSvc")
@Transactional
public class DescriptionServiceImpl implements DescriptionService {

	/** Logger */
	Logger log = LoggerFactory.getLogger(DescriptionServiceImpl.class);

	/** DI: Description Repository */
	@Autowired
	private DescriptionRepository descriptionRepository;
	
	/** DI: Transitive Closure Service */
	@Autowired
	private TransitiveClosureService tcSvc;
	
	/** DI: Refset Service */
	@Autowired
	private RefsetService referencesetService;
	
	/** DI: Member Service */
	@Autowired
	private RefsetMemberService referencesetMemberService;

	
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	

	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.DescriptionService#getDescriptionList(java.lang.String, java.lang.String)
	 */
	@Override
	public List<DescriptionDTO> getDescriptionList(String conceptId, String effectiveTime) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ArrayList<DescriptionDTO>();
		}

		List<Description> descriptions = null;
		descriptions = descriptionRepository.findByConceptIdAndEffectiveTime(conceptId, effectiveTime);

		return convertToDTOList(descriptions);
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.DescriptionService#getDescriptionList(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public List<DescriptionDTO> getDescriptionList(String componentId, String effectiveTime, boolean isLangGroup) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(componentId)) {
			return new ArrayList<DescriptionDTO>();
		}

		List<Description> descriptions = null;
		descriptions = getDescriptionListByComponentIdAndEffectiveTimeAndActive(componentId, effectiveTime, true);

		return addLanguageReferencesetToDescription(descriptions, effectiveTime, isLangGroup);
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.DescriptionService#getDescriptionList(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<DescriptionDTO> getDescriptionList(String conceptId, String effectiveTime, String typeId) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ArrayList<DescriptionDTO>();
		}

		List<Description> descs = descriptionRepository.findByConceptIdAndEffectiveTimeAndTypeId(conceptId,
				effectiveTime, typeId);

		return convertToDTOList(descs);
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.DescriptionService#getDescriptionListByConceptIdsAndLanguageCodeAndEffectiveTime(java.util.List, java.lang.String, java.lang.String)
	 */
	@Override
	public List<Description> getDescriptionListByConceptIdsAndLanguageCodeAndEffectiveTime(List<String> conceptIds, String languageCode, String effectiveTime) {
		if (conceptIds == null || conceptIds.isEmpty()) {
			return new ArrayList<Description>();
		}

		return descriptionRepository.findByConceptIdsAndLanguageCodeAndTypeIdAndEffectiveTime(conceptIds, languageCode,
				effectiveTime);
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.DescriptionService#getDescriptionListByDescriptionIdsAndLanguageCodeAndEffectiveTime(java.util.List, java.lang.String, java.lang.String)
	 */
	@Override
	public List<Description> getDescriptionListByDescriptionIdsAndLanguageCodeAndEffectiveTime(
			List<String> descriptionIds, String languageCode, String effectiveTime) {
		if (descriptionIds == null || descriptionIds.size() == 0) {
			return new ArrayList<Description>();
		}

		return descriptionRepository.findByDescriptionIdsAndLanguageCodeAndTypeIdAndEffectiveTime(descriptionIds,
				languageCode, effectiveTime);
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.DescriptionService#expandAll(java.util.List, java.util.List, java.util.List, java.lang.String)
	 */
	@Override
	public List<Description> expandAll(List<String> conceptIds, List<String> descriptionIds, List<String> paths,
			String effectiveTime) {

		if (conceptIds.isEmpty()) {
			conceptIds.add("");
		}
		if (descriptionIds.isEmpty()) {
			descriptionIds.add("");
		}

		List<Description> descs = new ArrayList<Description>();

		// focus 대상들의 description 목록가져온 후 추가
		descs.addAll(descriptionRepository.findByConceptIdsAndDescriptionIdsAndEffectiveTime(conceptIds, descriptionIds,
				effectiveTime));
		// 하위 대상들의 description 목록을 가져온 후 추가
		descs.addAll(descriptionRepository.findByPaths(paths));

		return descs;
	}

	
	/* ---------------------------------------- */
	/* Command Service */
	/* ---------------------------------------- */

	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.DescriptionService#createDescription(co.infoclinic.term.snomedct.model.dto.DescriptionDTO)
	 */
	@Override
	public DescriptionDTO createDescription(DescriptionDTO dto) {
		DescriptionDTO result = new DescriptionDTO();

		Description entity = convertToEntity(dto);

		// generate and set id
		String sctId = generateNewId();
		entity.setDescriptionId(sctId);

		// save description
		Description responseEntity = saveEntity(entity);

		List<LanguageRefsetDTO> responseLanguageReferencesetDtos = Lists.newArrayList();

		Long responseEntityId = responseEntity.getId();
		if (responseEntityId != null) {
			// save language referenceset
			List<LanguageRefsetDTO> languageReferencesetDtos = dto.getLanguageReferencesetList();
			responseLanguageReferencesetDtos = referencesetMemberService
					.createLanguageReferencesetMemberList(languageReferencesetDtos, sctId);
		} else {
			try {
				log.error("Create Description Error");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// entity to dto
		result = convertToDTO(responseEntity);
		result.setLanguageReferencesetList(responseLanguageReferencesetDtos);

		return result;
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.DescriptionService#createDescriptionList(java.util.List)
	 */
	@Override
	public List<DescriptionDTO> createDescriptionList(List<DescriptionDTO> dtos) {
		List<DescriptionDTO> responseDtos = Lists.newArrayList();

		for (DescriptionDTO dto : MoreObjects.firstNonNull(dtos, Collections.<DescriptionDTO>emptyList())) {
			DescriptionDTO responseDto = createDescription(dto);
			responseDtos.add(responseDto);
		}

		return responseDtos;
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.DescriptionService#updateDescription(co.infoclinic.term.snomedct.model.dto.DescriptionDTO)
	 */
	@Override
	public DescriptionDTO updateDescription(DescriptionDTO dto) {
		DescriptionDTO result = new DescriptionDTO();

		Description entity = convertToEntity(dto);

		// save
		Description responseEntity = saveEntity(entity);

		// save language referenceset
		List<LanguageRefsetDTO> languageReferencesetDtos = dto.getLanguageReferencesetList();
		List<LanguageRefsetDTO> responseLanguageReferencesetDtos = referencesetMemberService
				.updateLanguageReferencesetMemberList(languageReferencesetDtos);
		// entity to dto
		result = convertToDTO(responseEntity);
		result.setLanguageReferencesetList(responseLanguageReferencesetDtos);

		return result;
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.DescriptionService#updateDescriptionList(java.util.List)
	 */
	@Override
	public List<DescriptionDTO> updateDescriptionList(List<DescriptionDTO> dtos) {
		List<DescriptionDTO> responseDtos = new ArrayList<DescriptionDTO>();

		for (DescriptionDTO dto : MoreObjects.firstNonNull(dtos, Collections.<DescriptionDTO>emptyList())) {
			DescriptionDTO responseDto = updateDescription(dto);
			responseDtos.add(responseDto);
		}

		return responseDtos;
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.DescriptionService#deleteDescription(java.lang.Long)
	 */
	@Override
	public boolean deleteDescription(Long id) {
		descriptionRepository.delete(id);
		return true;
	}
	

	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.DescriptionService#deleteDescriptionList(java.util.List)
	 */
	@Override
	public boolean deleteDescriptionList(List<DescriptionDTO> dtos) {
		List<Description> entities = convertToEntityList(dtos);
		descriptionRepository.deleteInBatch(entities);
		return true;
	}

	/* ---------------------------------------- */
	/* Private Methods */
	/* ---------------------------------------- */

	/**
	 * Description의 새로운 아이디를 반환하는 메소드
	 * 
	 * @return
	 */
	private String generateNewId() {
		return SNOMEDCTIdentifierGenerator.create(SNOMEDCTComponentTypeEnum.DESCRIPTION, null);
	}

	
	/**
	 * Description을 저장하는 메소드
	 * 
	 * @param entity
	 * @return
	 */
	private Description saveEntity(Description entity) {
		return descriptionRepository.save(entity);
	}

	
	/**
	 * 
	 * @param dto
	 * @return
	 */
	private Description convertToEntity(DescriptionDTO dto) {
		return DescriptionConverter.toEntity(dto);
	}


	/**
	 * 
	 * @param entity
	 * @return
	 */
	private DescriptionDTO convertToDTO(Description entity) {
		return DescriptionConverter.toDTO(entity, false);
	}

	
	/**
	 * 
	 * @param entities
	 * @return
	 */
	private List<DescriptionDTO> convertToDTOList(List<Description> entities) {
		return DescriptionConverter.toDTOList(entities, false);
	}

	
	/**
	 * 
	 * @param dtos
	 * @return
	 */
	private List<Description> convertToEntityList(List<DescriptionDTO> dtos) {
		return DescriptionConverter.toEntityList(dtos);
	}

	
	/**
	 * 
	 * @param descriptionList
	 * @param effectiveTime
	 * @param isLangGroup
	 * @return
	 */
	private List<DescriptionDTO> addLanguageReferencesetToDescription(final List<Description> descriptionList,
			String effectiveTime, boolean isLangGroup) {
		// parameter의 descriptionList가 null이거나 비어있다면 null을 반환
		if (descriptionList == null || descriptionList.size() == 0) {
			return new ArrayList<DescriptionDTO>();
		}

		// 반환되는 변수
		List<DescriptionDTO> dtos = new ArrayList<DescriptionDTO>();
		// descriptionId별 Description를 담는 맵 변수
		Map<String, Description> descriptionIdDescriptionMap = new HashMap<String, Description>();
		Description description = null;
		int descriptionListSize = descriptionList.size();
		// descriptionList를 가지고 descriptionId별 Description 맵 구성
		for (int i = 0; i < descriptionListSize; i++) {
			description = descriptionList.get(i);
			String descriptionId = description.getDescriptionId();
			descriptionIdDescriptionMap.put(descriptionId, description);
		}

		// SNOMEDCT Hiearchy에서 (SNOMED CT Model Component>Foundation metadata
		// concept>Reference set>Language type reference set)
		// 하위에 정의되어있는 모든 Concept의 Id 가져오기 (From Search Service)
		List<String> languageRefsetIdList = tcSvc.getLanguageRefsetIdList();

		// ReferenceSet리스트 가져오기
		List<Referenceset> langRefsetList = referencesetService.getRefsetIds(
				new ArrayList<String>(descriptionIdDescriptionMap.keySet()), // descriptionId의 리스트
				languageRefsetIdList, // Language type reference set 하위에 정의되어있는 모든 ConceptId 리스트
				effectiveTime); // 적용일자

		DescriptionDTO dto = null;
		List<LanguageRefsetDTO> langDtos = null;
		LanguageRefsetDTO langDto = null;
		Referenceset langRefset = null;
		Description sourceDescription = null;

		if (isLangGroup) {
			// langRefsetList를 referencedComponentId별 Map을 구성한다.
			Map<String, List<LanguageRefsetDTO>> refCpntIdlangRefsetDTOsMap = new HashMap<String, List<LanguageRefsetDTO>>();
			// List<>
			int langRefsetListSize = langRefsetList.size();
			for (int i = 0; i < langRefsetListSize; i++) {
				langRefset = langRefsetList.get(i);
				String referencedComponentId = langRefset.getReferencedComponentId();
				if (!refCpntIdlangRefsetDTOsMap.containsKey(referencedComponentId)) {
					langDtos = new ArrayList<LanguageRefsetDTO>();
					refCpntIdlangRefsetDTOsMap.put(referencedComponentId, langDtos);
				} else {
					langDtos = refCpntIdlangRefsetDTOsMap.get(referencedComponentId);
				}

				langDto = RefsetMemberConverter.toLangDTO(langRefset);
				langDtos.add(langDto);
			}

			// descriptionIdDescriptionMap에서 각 descriptionId별 Description을 불러내어
			// LanguageReferencset을 셋팅한다.
			Iterator<String> itr = descriptionIdDescriptionMap.keySet().iterator();
			while (itr.hasNext()) {
				String descriptionId = itr.next();
				sourceDescription = descriptionIdDescriptionMap.get(descriptionId);
				dto = DescriptionConverter.toDTO(sourceDescription, false);

				langDtos = refCpntIdlangRefsetDTOsMap.get(descriptionId);
				dto.setLanguageReferencesetList(langDtos);
				dtos.add(dto);
			}
		} else {
			int langRefsetListSize = langRefsetList.size();
			for (int i = 0; i < langRefsetListSize; i++) {
				// Mapper를 사용해서 Description Entity를 DTO로 매핑
				langRefset = langRefsetList.get(i);
				String referencedComponentId = langRefset.getReferencedComponentId();
				sourceDescription = descriptionIdDescriptionMap.get(referencedComponentId);
				dto = DescriptionConverter.toDTO(sourceDescription, false);
				langDto = RefsetMemberConverter.toLangDTO(langRefset);

				langDtos = new ArrayList<LanguageRefsetDTO>();
				langDtos.add(langDto);
				dto.setLanguageReferencesetList(langDtos);
				dtos.add(dto);
			}
		}
		return dtos;
	}
	
	
	/**
	 * 
	 * Description 목록 조회
	 * 
	 * @param componentId
	 * @param effectiveTime
	 * @param isActive
	 * @return
	 */
	private List<Description> getDescriptionListByComponentIdAndEffectiveTimeAndActive(String componentId, String effectiveTime, boolean isActive) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(componentId)) {
			return new ArrayList<Description>();
		}

		// VALIDATION
		// 1. 파라메터로부터 받아온 componentId의 Component Type확인
		List<Description> descriptionList = new ArrayList<Description>();
		SNOMEDCTComponentTypeEnum componentType = SNOMEDCTComponentTypeEnum.getById(componentId);
		switch (componentType) {
		case CONCEPT:
			// ConceptId, 기타파라메터로 Description 가져오기
			descriptionList = descriptionRepository
					.findDescriptionListByEffectiveTimeAndConceptIdAndActive(effectiveTime, componentId, isActive);
			break;
		case DESCRIPTION:
			// DescriptionId, 기타파라메터로 Description 가져오기
			descriptionList = descriptionRepository
					.findDescriptionListByEffectiveTimeAndDescriptionIdAndActive(effectiveTime, componentId, isActive);
			break;
		// case RELATIONSHIP:
		// log.error("not supported relationship type");
		// break;
		default:
			log.error("not supported type : " + componentType);
			break;
		}
		return descriptionList;
	}

}
