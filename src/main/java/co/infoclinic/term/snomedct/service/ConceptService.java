package co.infoclinic.term.snomedct.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import co.infoclinic.term.common.model.dto.SubsumptionTestDTO;
import co.infoclinic.term.snomedct.model.dto.ConceptViewDTO;
import co.infoclinic.term.snomedct.model.dto.ConceptTreeDTO;
import co.infoclinic.term.snomedct.model.entity.Concept;

/**
 * The Concept Service
 */
public interface ConceptService {
	
	/**
	 * Entity ID의 Active인 Entity 조회
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	ConceptViewDTO getConcept(String conceptId, String effectiveTime);
	
	ConceptTreeDTO getConcept2(String conceptId, String effectiveTime);
	
	
	/**
	 * Entity ID의 모든 Entity 조회
	 * 
	 * @param componentId
	 * @param effectiveTime
	 * @return
	 */
	List<ConceptViewDTO> getConceptList(String componentId, String effectiveTime);


	/**
	 * Attribute와 Value 쌍으로 정의된 혹은 정의에 포함된 모든 Entity를 반환
	 * 
	 * @param attrVals
	 * @param effectiveTime
	 * @return
	 */
	List<ConceptViewDTO> getConceptList(Map<String, Object> attrVals, String effectiveTime);

	
	/**
	 * Entity의 자식 목록 조회
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	List<ConceptViewDTO> getChildren(String conceptId, String effectiveTime);

	/** 자식 + self 목록 (<<!) */
	List<ConceptViewDTO> getChildrenOrSelf(String conceptId, String effectiveTime);


	/**
	 * Entity의 부모 목록 조회
	 *
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	List<ConceptViewDTO> getParentList(String conceptId, String effectiveTime);

	/** 부모 + self 목록 (>>!) */
	List<ConceptViewDTO> getParentListOrSelf(String conceptId, String effectiveTime);

	/** refset 멤버 목록 (^) */
	List<ConceptViewDTO> getMemberOfList(String refsetId, String effectiveTime);

	
	/**
	 * Entity의 상위 목록 조회
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @param offset
	 * @param limit
	 * @return
	 */
	Page<ConceptViewDTO> getAncestorList(String conceptId, String effectiveTime, int offset, int limit);
	
	
	/**
	 * Entity 및 상위 목록 조회
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @param offset
	 * @param limit
	 * @return
	 */
	Page<ConceptViewDTO> getAncestorListOrSelf(String conceptId, String effectiveTime, int offset, int limit);

	
	/**
	 * Entity의 하위 목록 조회
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @param offset
	 * @param limit
	 * @return
	 */
	Page<ConceptViewDTO> getDescendantList(String conceptId, String effectiveTime, int offset, int limit);

	/** LIMIT 없이 전체 하위 목록 조회 (ECL 전용) */
	List<ConceptViewDTO> getAllDescendantList(String conceptId, String effectiveTime);

	/** self 포함, LIMIT 없이 전체 하위 목록 조회 (ECL 전용) */
	List<ConceptViewDTO> getAllDescendantListOrSelf(String conceptId, String effectiveTime);
	
	
	/**
	 * Entity 및 하위 목록 조회
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @param offset
	 * @param limit
	 * @return
	 */
	Page<ConceptViewDTO> getDescendantListOrSelf(String conceptId, String effectiveTime, int offset, int limit);


	/**
	 * Entity 및 하위 목록 조회 후 트리 리
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @param offset
	 * @param limit
	 * @return
	 */
	ConceptTreeDTO getDescendantTree(String conceptId, String effectiveTime);
	
	/**
	 * 포함관계 조회
	 * 
	 * @param criteriaId
	 * @param conceptId
	 * @return
	 */
	SubsumptionTestDTO subsumptionTest(String criteriaId, String conceptId);

	
	/**
	 * Entity의 Post-coordinated Expression 조회
	 * 
	 * @param code
	 * @param effectiveTime
	 * @return
	 */
	String getPostExpr(String code, String effectiveTime);

}
