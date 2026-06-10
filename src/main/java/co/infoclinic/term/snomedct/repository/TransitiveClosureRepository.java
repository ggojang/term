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
	 * 여러 Entity Id의 경로 목록 조회
	 * 
	 * <pre>
	 * 87612001:Blood (Substance)의 자식/자손을 모두 가져오기
	 * SELECT CONCAT(PATH, "~", CONCEPT_ID) AS P
	 * FROM TC
	 * WHERE CONCEPT_ID IN ("87612001");
	 * </pre>
	 * 
	 * @param conceptIds
	 * @return
	 */
	@Query(value = "SELECT CONCAT(PATH, '~', CONCEPT_ID) AS PATH " +
				   "FROM TC " +
				   "WHERE CONCEPT_ID IN (?1)", nativeQuery = true)
	List<String> findPathListByConceptIds(List<String> conceptIds);
	
	
	/**
	 * Entity Id의 경로 목록 조회
	 * 
	 * @param conceptId
	 * @return
	 */
	@Query(value = "SELECT CONCAT(PATH, '~', CONCEPT_ID) AS PATH " +
			   	   "FROM TC " +
			   	   "WHERE CONCEPT_ID = ?1", nativeQuery = true)
	List<String> findPathListByConceptId(String conceptId);


	/**
	 * Entity Id의 부모 경로 목록 조회
	 * 
	 * @param conceptId
	 * @return
	 */
	@Query(value = "SELECT PATH " +
		   	   "FROM TC " +
		   	   "WHERE CONCEPT_ID = ?1", nativeQuery = true)
	List<String> findParentPathListByConceptId(String conceptId);
	
	
	/**
	 * Language Refset Id의 목록 조회
	 * 
	 * @param conceptId
	 * @return
	 */
	@Query(value = "SELECT CONCEPT_ID " +
		   	   	   "FROM TC " +
		   	   	   "WHERE PATH LIKE '138875005~900000000000441003~900000000000454005~900000000000455006~900000000000506000~%'", nativeQuery = true)
	List<String> findLanguageRefsetIdList();
	
	
	/**
	 * Entity Id간 포함여부 조회
	 * 
	 * 계층구조에서 Concept Id가 Criteria Id내에 포함되어 있는지 확인 
	 * 
	 * @param criteriaId
	 * @param conceptId
	 * @return
	 */
	@Query(value = "SELECT COUNT(CONCEPT_ID) " +
				   "FROM TC " +
				   "WHERE CONCEPT_ID = ?2 " +
				   "AND PATH LIKE CONCAT('%', ?1, '%')", nativeQuery = true)
	int findCountByCriteriaIdAndConceptId(String criteriaId, String conceptId);
	
	
	/**
	 * 엔티티 조회
	 * 
	 * @param conceptId
	 * @return
	 */
	@Query(value = "SELECT * " +
		   	   	   "FROM TC " +
		   	   	   "WHERE CONCEPT_ID = ?1 " +
		   	   	   "LIMIT 1", nativeQuery = true)
	TransitiveClosure findByConceptId(String conceptId);


	/**
	 * 자손 수
	 * 
	 * @param conceptId
	 * @return
	 */
	@Query(value = "SELECT DESCENDANT_COUNT " +
				   "FROM TC " +
				   "WHERE CONCEPT_ID = ?1 " +
				   "LIMIT 1", nativeQuery = true)
	int findDescendantCountByConceptId(String conceptId);
}
