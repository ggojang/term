package co.infoclinic.term.snomedct.repository.custom;

import java.util.List;
import java.util.Map;

import co.infoclinic.term.snomedct.model.dto.ConceptViewDTO;
import co.infoclinic.term.snomedct.model.dto.ConceptTreeDTO;
import co.infoclinic.term.snomedct.model.entity.Concept;

/**
 * The Concept Repository for Custom Query
 */
public interface ConceptRepositoryCustom {
	
	/**
	 * 어떠한 속성과 값으로 정의를 이루는 개념 목록 조회
	 * 
	 * @param map 속성과 값을 표현하는 객체
	 * @param effectiveTime 유효시작시간
	 * @return
	 */
	List<Concept> findByAttrVals(Map<String, Object> map, String effectiveTime);
	
	/**
	 * Entity 조회
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	ConceptViewDTO findByConceptIdAndEffectiveTime(String conceptId, String effectiveTime);
	
	/**
	 * Entity 조회 for tree
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	ConceptTreeDTO findByConceptIdAndEffectiveTime2(String conceptId, String effectiveTime);
	
	/**
	 * Entity ID의 경로 목록으로부터 Entity의 부모 목록 조회
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	List<ConceptViewDTO> findParentListByConceptIdAndEffectiveTime(String conceptId, String effectiveTime);
	
	
	/**
	 * Entity ID의 경로 목록으로부터 Entity의 자식 목록 조회
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	List<ConceptViewDTO> findChildrenByConceptIdAndEffectiveTime(String conceptId, String effectiveTime);
	
	/**
	 * Entity ID의 경로 목록으로부터 Entity의 자식 목록 조회
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @param offset
	 * @param limit
	 * @return
	 */
	List<ConceptTreeDTO> findChildrenByConceptIdAndEffectiveTime2(String conceptId, String effectiveTime);
	
	
	/**
	 * Entity ID의 경로 목록으로부터 Entity의 하위 목록 조회
	 * 
	 * @param paths
	 * @param effectiveTime
	 * @param offset
	 * @param limit
	 * @return
	 */
	List<ConceptViewDTO> findDescendantListByPathsAndEffectiveTime(List<String> paths, String effectiveTime, int offset, int limit);
	

	/**
	 * Entity ID의 목록으로부터 Entity의 상위 목록 조회
	 * 
	 * @param ids
	 * @param effectiveTime
	 * @param offset
	 * @param limit
	 * @return
	 */
	List<ConceptViewDTO> findAncestorListByIdsAndEffectiveTime(List<String> ids, String effectiveTime, int offset, int limit);
	
	
	/**
	 * Entity ID 및 목록으로부터 Entity 및 상위 목록 조회
	 * 
	 * @param conceptId
	 * @param ids
	 * @param effectiveTime
	 * @param offset
	 * @param limit
	 * @return
	 */
	List<ConceptViewDTO> findAncestorListOrSelfByConceptIdAndIdsAndEffectiveTime(String conceptId, List<String> ids, String effectiveTime, int offset, int limit);

}
