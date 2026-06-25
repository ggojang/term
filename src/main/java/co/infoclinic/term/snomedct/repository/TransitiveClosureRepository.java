package co.infoclinic.term.snomedct.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.snomedct.model.entity.TransitiveClosure;

/**
 * The Transitive Closure Repository
 */
public interface TransitiveClosureRepository extends JpaRepository<TransitiveClosure, Long> {

	/**
	 * TC에 저장된 릴리즈 effectiveTime 목록 (최신순)
	 */
	@Query(value = "SELECT DISTINCT EFFECTIVE_TIME FROM TC ORDER BY EFFECTIVE_TIME DESC", nativeQuery = true)
	List<String> findDistinctEffectiveTimes();


	/**
	 * 여러 Entity Id의 경로 목록 조회
	 *
	 * @param conceptIds
	 * @param effectiveTime
	 * @return
	 */
	@Query(value = "SELECT CONCAT(PATH, '~', CONCEPT_ID) AS PATH " +
				   "FROM TC " +
				   "WHERE CONCEPT_ID IN (?1) AND EFFECTIVE_TIME = ?2", nativeQuery = true)
	List<String> findPathListByConceptIds(List<String> conceptIds, String effectiveTime);


	/**
	 * Entity Id의 경로 목록 조회
	 *
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	@Query(value = "SELECT CONCAT(PATH, '~', CONCEPT_ID) AS PATH " +
				   "FROM TC " +
				   "WHERE CONCEPT_ID = ?1 AND EFFECTIVE_TIME = ?2", nativeQuery = true)
	List<String> findPathListByConceptId(String conceptId, String effectiveTime);


	/**
	 * Entity Id의 부모 경로 목록 조회
	 *
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	@Query(value = "SELECT PATH " +
				   "FROM TC " +
				   "WHERE CONCEPT_ID = ?1 AND EFFECTIVE_TIME = ?2", nativeQuery = true)
	List<String> findParentPathListByConceptId(String conceptId, String effectiveTime);


	/**
	 * Language Refset Id의 목록 조회
	 *
	 * @param effectiveTime
	 * @return
	 */
	@Query(value = "SELECT CONCEPT_ID " +
				   "FROM TC " +
				   "WHERE PATH LIKE '138875005~900000000000441003~900000000000454005~900000000000455006~900000000000506000~%'" +
				   " AND EFFECTIVE_TIME = ?1", nativeQuery = true)
	List<String> findLanguageRefsetIdList(String effectiveTime);


	/**
	 * Entity Id간 포함여부 조회 (계층구조에서 Concept Id가 Criteria Id내에 포함되어 있는지 확인)
	 *
	 * @param criteriaId
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	@Query(value = "SELECT COUNT(CONCEPT_ID) " +
				   "FROM TC " +
				   "WHERE CONCEPT_ID = ?2 " +
				   "AND PATH LIKE CONCAT('%', ?1, '%') " +
				   "AND EFFECTIVE_TIME = ?3", nativeQuery = true)
	int findCountByCriteriaIdAndConceptId(String criteriaId, String conceptId, String effectiveTime);


	/**
	 * 엔티티 조회
	 *
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	@Query(value = "SELECT * " +
				   "FROM TC " +
				   "WHERE CONCEPT_ID = ?1 AND EFFECTIVE_TIME = ?2 " +
				   "LIMIT 1", nativeQuery = true)
	TransitiveClosure findByConceptId(String conceptId, String effectiveTime);


	/**
	 * 자손 수
	 *
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	@Query(value = "SELECT DESCENDANT_COUNT " +
				   "FROM TC " +
				   "WHERE CONCEPT_ID = ?1 AND EFFECTIVE_TIME = ?2 " +
				   "LIMIT 1", nativeQuery = true)
	int findDescendantCountByConceptId(String conceptId, String effectiveTime);
}
