package co.infoclinic.term.snomedct.service;

import java.util.List;
import java.util.Map;

/**
 * The Transitive Closure Service
 */
public interface TransitiveClosureService {

	/**
	 * TC에 저장된 릴리즈 effectiveTime 목록 (최신순)
	 */
	List<String> getAvailableEffectiveTimes();


	/**
	 * 각 컨셉아이디에 대하여 루트로부터 아이디까지의 모든 경로 목록 조회
	 *
	 * @param conceptIds    컨셉아이디 목록
	 * @param effectiveTime 릴리즈 기준 날짜 (예: "20241001")
	 * @return 경로 목록
	 */
	List<String> getPathListByConceptIds(List<String> conceptIds, String effectiveTime);


	/**
	 * 자신으로부터 루트까지의 모든 경로 목록 조회
	 *
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	List<String> getPathListByConceptId(String conceptId, String effectiveTime);


	/**
	 * 부모로부터 루트까지의 모든 경로 목록 조회
	 *
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	List<String> getParentPathListByConceptId(String conceptId, String effectiveTime);


	/**
	 * 언어세트 아이디 목록 조회
	 *
	 * @param effectiveTime
	 * @return
	 */
	List<String> getLanguageRefsetIdList(String effectiveTime);


	/**
	 * children/descendant 수 Map 조회
	 *
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	Map<String, Integer> getCountMapByConceptId(String conceptId, String effectiveTime);


	/**
	 * Entity ID간 포함여부 확인
	 *
	 * @param criteriaId
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	int isSubsumption(String criteriaId, String conceptId, String effectiveTime);


	/**
	 * 자손 수 확인
	 *
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	int getDescendantCount(String conceptId, String effectiveTime);


	/**
	 * Entity ID 및 Ancestor ID 목록
	 *
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	List<String> getAncestorIdWithFocusConceptIdSet(String conceptId, String effectiveTime);
}
