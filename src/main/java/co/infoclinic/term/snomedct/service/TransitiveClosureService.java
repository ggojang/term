package co.infoclinic.term.snomedct.service;

import java.util.List;
import java.util.Map;

/**
 * The Transitive Closure Service
 */
public interface TransitiveClosureService {

	/**
	 * 각 컨셉아이디에 대하여 루트로부터 아이디까지의 모든 경로 목록 조회
	 * 
	 * @param conceptIds 컨셉아이디 목록
	 * @return 경로 목록
	 */
	List<String> getPathListByConceptIds(List<String> conceptIds);
	
	
	/**
	 * 자신으로부터 루트까지의 모든 경로 목록 조회
	 * 
	 * @param conceptId
	 * @return
	 */
	List<String> getPathListByConceptId(String conceptId);
	
	
	/**
	 * 부모로부터 루트까지의 모든 경로 목록 조회
	 * 
	 * @param conceptId
	 * @return
	 */
	List<String> getParentPathListByConceptId(String conceptId);
	
	
	/**
	 * 언어세트 아이디 목록 조회
	 * 
	 * @return
	 */
	List<String> getLanguageRefsetIdList();
	
	
	/**
	 * 
	 * @param conceptId
	 * @return
	 */
	Map<String, Integer> getCountMapByConceptId(String conceptId);
 	
	
	/**
	 * Entity ID간 포함여부 확인
	 * 
	 * @param criteriaId
	 * @param conceptId
	 * @return
	 */
	int isSubsumption(String criteriaId, String conceptId);
	
	
	/**
	 * 자손 수 확인
	 * @param conceptId
	 * @return
	 */
	int getDescendantCount(String conceptId);


	/**
	 * Entity ID 및 Ancestor ID 목록
	 * @param conceptId
	 * @return
	 */
	List<String> getAncestorIdWithFocusConceptIdSet(String conceptId);
}
