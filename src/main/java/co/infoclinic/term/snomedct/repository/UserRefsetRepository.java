package co.infoclinic.term.snomedct.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.snomedct.model.entity.UserReferenceset;

/**
 * User Referenceset Repository
 */
public interface UserRefsetRepository extends AbstractRefsetRepository<UserReferenceset> {
	
	/**
	 * 
	 * @param referencedComponentIdList
	 * @param languageReferencesetIdList
	 * @param effectiveTime
	 * @return
	 */
	@Override
	@Query(value = "SELECT r.* " +
                 "FROM USER_REFERENCESET r " +
                 "INNER JOIN ( " +
                 "   SELECT REFSET_ID, " +
                 "          REFERENCED_COMPONENT_ID, " +
                 "          MAX(EFFECTIVE_TIME) as MAX_EFFECTIVE_TIME " +
                 "   FROM USER_REFERENCESET " +
                 "   WHERE REFERENCED_COMPONENT_ID IN (?1) " +
                 "   AND REFSET_ID IN (?2) " +
                 "   AND EFFECTIVE_TIME <= ?3 " +
                 "   GROUP BY REFSET_ID, REFERENCED_COMPONENT_ID " +
                 ") as groupRef " +
                 "ON r.REFSET_ID = groupRef.REFSET_ID " +
                 "AND r.REFERENCED_COMPONENT_ID = groupRef.REFERENCED_COMPONENT_ID " +
                 "AND r.EFFECTIVE_TIME = groupRef.MAX_EFFECTIVE_TIME", nativeQuery = true)
	List<UserReferenceset> findByReferencedComponentIdsAndRefsetIdsAndEffectiveTime(List<String> referencedComponentIds,
			List<String> languageReferencesetIds, String effectiveTime);

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.repository.AbstractRefsetRepository#findByRefsetIdsAndReferencedComponentIdAndEffectiveTime(java.util.List, java.lang.String, java.lang.String)
	 */
	@Override
	@Query(value = "SELECT * FROM USER_REFERENCESET WHERE REFSET_ID IN (?1) AND REFERENCED_COMPONENT_ID = ?2 AND EFFECTIVE_TIME = ?3", nativeQuery = true)
	List<UserReferenceset> findByRefsetIdsAndReferencedComponentIdAndEffectiveTime(List<String> refsetIds,
			String referencedComponentId, String effectiveTime);
}
