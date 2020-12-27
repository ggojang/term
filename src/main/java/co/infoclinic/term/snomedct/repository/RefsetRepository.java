package co.infoclinic.term.snomedct.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.snomedct.model.entity.Referenceset;

/**
 * The Refset Repository
 */
public interface RefsetRepository extends AbstractRefsetRepository<Referenceset> {
	
	/**
	 * 멤버로 등록되어있는 활성화상태인 레퍼런스세트 멤버 목록 조회
	 * 
	 * @param referencedComponentId
	 * @param effectiveTime
	 * @return
	 */
	@Query(value = "SELECT d.* " + 
				   "FROM REFERENCESET d " + 
				   "INNER JOIN (" + 
				   "	SELECT REFERENCESET_ID, MAX(EFFECTIVE_TIME) as MAX_EFFECTIVE_TIME " + 
				   "	FROM REFERENCESET " + 
				   "	WHERE REFERENCED_COMPONENT_ID = ?1 " + 
				   "	AND EFFECTIVE_TIME <= ?2 " + 
				   "	GROUP BY REFSET_ID, REFERENCESET_ID " + ") as groupRef " + 
				   "ON d.REFERENCESET_ID = groupRef.REFERENCESET_ID " + 
				   "AND d.EFFECTIVE_TIME = groupRef.MAX_EFFECTIVE_TIME " + 
				   "AND d.ACTIVE = 1 ORDER BY SEQ", nativeQuery = true)
	List<Referenceset> findByReferencedComponentIdAndEffectiveTime(String referencedComponentId, String effectiveTime);
	
	
	/**
	 * 
	 * @param refsetId
	 * @param referencedComponentId
	 * @param effectiveTime
	 * @return
	 */
	@Query(value = "SELECT d.* " + 
				   "FROM REFERENCESET d " + 
				   "INNER JOIN (" + 
				   "	SELECT REFERENCESET_ID, MAX(EFFECTIVE_TIME) as MAX_EFFECTIVE_TIME " + 
				   "	FROM REFERENCESET " + 
				   "	WHERE REFSET_ID = ?1 " + 
				   "	AND REFERENCED_COMPONENT_ID = ?2 " +
				   "	AND EFFECTIVE_TIME <= ?3 " + 
				   "	GROUP BY REFERENCESET_ID " + 
				   ") as groupRef " + 
				   "ON d.REFERENCESET_ID = groupRef.REFERENCESET_ID " + 
				   "AND d.EFFECTIVE_TIME = groupRef.MAX_EFFECTIVE_TIME " + 
				   "AND d.ACTIVE = 1", nativeQuery = true)
	List<Referenceset> findByRefsetIdAndReferencedComponentIdAndEffectiveTime(String refsetId, String referencedComponentId, String effectiveTime);

	
	/**
	 * 
	 * @param referencedComponentIdList
	 * @param languageReferencesetIdList
	 * @param effectiveTime
	 * @return
	 */
	@Override
	@Query(value = "SELECT r.* " +
                 "FROM REFERENCESET r " +
                 "INNER JOIN ( " +
                 "   SELECT REFSET_ID, " +
                 "          REFERENCED_COMPONENT_ID, " +
                 "          MAX(EFFECTIVE_TIME) as MAX_EFFECTIVE_TIME " +
                 "   FROM REFERENCESET " +
                 "   WHERE REFERENCED_COMPONENT_ID IN (?1) " +
                 "   AND REFSET_ID IN (?2) " +
                 "   AND EFFECTIVE_TIME <= ?3 " +
                 "   GROUP BY REFSET_ID, REFERENCED_COMPONENT_ID " +
                 ") as groupRef " +
                 "ON r.REFSET_ID = groupRef.REFSET_ID " +
                 "AND r.REFERENCED_COMPONENT_ID = groupRef.REFERENCED_COMPONENT_ID " +
                 "AND r.EFFECTIVE_TIME = groupRef.MAX_EFFECTIVE_TIME", nativeQuery = true)
	List<Referenceset> findByReferencedComponentIdsAndRefsetIdsAndEffectiveTime(List<String> referencedComponentIdList, List<String> languageReferencesetIdList, String effectiveTime);

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.repository.AbstractRefsetRepository#findByRefsetIdsAndReferencedComponentIdAndEffectiveTime(java.util.List, java.lang.String, java.lang.String)
	 */
	@Override
	@Query(value = "SELECT * " + 
				"FROM REFERENCESET " + 
				"WHERE REFSET_ID IN (?1) AND REFERENCED_COMPONENT_ID = ?2 AND EFFECTIVE_TIME = ?3", nativeQuery = true)
	List<Referenceset> findByRefsetIdsAndReferencedComponentIdAndEffectiveTime(List<String> refsetIdList, String referencedComponentId, String effectiveTime);


	
}
