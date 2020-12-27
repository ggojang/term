package co.infoclinic.term.snomedct.service;

import java.util.List;

import co.infoclinic.term.snomedct.model.dto.DescriptionDTO;
import co.infoclinic.term.snomedct.model.entity.Description;

/**
 * Description Service
 */
public interface DescriptionService {

	// ----------------------------------------
	// 조회
	// ----------------------------------------

	/**
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	List<DescriptionDTO> getDescriptionList(String conceptId, String effectiveTime);

	
	/**
	 * 
	 * @param componentId
	 * @param effectiveTime
	 * @param isLangGroup
	 * @return
	 */
	List<DescriptionDTO> getDescriptionList(String componentId, String effectiveTime, boolean isLangGroup);

	
	/**
	 * 
	 * @param componentId
	 * @param effectiveTime
	 * @param typeId
	 * @return
	 */
	List<DescriptionDTO> getDescriptionList(String componentId, String effectiveTime, String typeId);

	
	/**
	 * 
	 * @param conceptIds
	 * @param languageCode
	 * @param effectiveTime
	 * @return
	 */
	List<Description> getDescriptionListByConceptIdsAndLanguageCodeAndEffectiveTime(List<String> conceptIds, String languageCode, String effectiveTime);

	
	/**
	 * 
	 * @param descriptionIds
	 * @param languageCode
	 * @param effectiveTime
	 * @return
	 */
	List<Description> getDescriptionListByDescriptionIdsAndLanguageCodeAndEffectiveTime(List<String> descriptionIds, String languageCode, String effectiveTime);

	
	/**
	 * 
	 * @param conceptIds
	 * @param descriptionIds
	 * @param paths
	 * @param effectiveTime
	 * @return
	 */
	List<Description> expandAll(List<String> conceptIds, List<String> descriptionIds, List<String> paths, String effectiveTime);
	
	
	
	
	// ----------------------------------------
	// 생성, 수정, 제거
	// ----------------------------------------

	/**
	 * 
	 * @param d
	 * @return
	 */
	DescriptionDTO createDescription(DescriptionDTO d);

	
	/**
	 * 
	 * @param descriptionDtoList
	 * @return
	 */
	List<DescriptionDTO> createDescriptionList(List<DescriptionDTO> descriptionDtoList);

	
	/**
	 * 
	 * @param d
	 * @return
	 */
	DescriptionDTO updateDescription(DescriptionDTO d);

	
	/**
	 * 
	 * @param descriptionDtos
	 * @return
	 */
	List<DescriptionDTO> updateDescriptionList(List<DescriptionDTO> descriptionDtos);

	
	/**
	 * 
	 * @param id
	 * @return
	 */
	boolean deleteDescription(Long id);

	
	/**
	 * 
	 * @param descriptionDtos
	 * @return
	 */
	boolean deleteDescriptionList(List<DescriptionDTO> descriptionDtos);
}
