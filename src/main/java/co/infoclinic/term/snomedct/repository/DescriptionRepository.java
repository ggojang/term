package co.infoclinic.term.snomedct.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.infoclinic.term.snomedct.model.entity.Description;
import co.infoclinic.term.snomedct.repository.custom.DescriptionRepositoryCustom;

/**
 * Description Repository
 */
public interface DescriptionRepository extends JpaRepository<Description, Long>, DescriptionRepositoryCustom {

	/**
	 * 
	 * @return
	 */
	@Query(value = "SELECT MAX(d.id) " + "FROM Description d")
	Long findMaxId();

	
	/**
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	@Query(value = "SELECT * " + 
			"FROM DESCRIPTION d " + 
			"WHERE d.CONCEPT_ID=?1 " + 
			"AND d.EFFECTIVE_TIME <= ?2 " +
			"ORDER BY d.DESCRIPTION_ID, d.EFFECTIVE_TIME DESC", nativeQuery = true)
	List<Description> findByConceptIdAndEffectiveTime(@Param("conceptId") String conceptId,
			@Param("effectiveTime") String effectiveTime);

	
	/**
	 * 
	 * @param term
	 * @return
	 */
	@Query(value = "SELECT DISTINCT(d.descriptionId) " + 
			"FROM Description d " + 
			"WHERE d.term LIKE CONCAT('%',?1,'%') " + 
			"AND d.active=1")
	Set<String> findDescriptionIdByTerm(String term);

	
	/**
	 * 
	 * @param term
	 * @return
	 */
	@Query(value = "SELECT DISTINCT(d.conceptId) " + 
			"FROM Description d " + 
			"WHERE d.term LIKE CONCAT('%',?1,'%') " +
			"AND d.active=1")
	Set<String> findConceptIdByTerm(String term);
	
	
	/**
	 * 특정 ConceptId와 EffectiveTime(적용일자)내의 가장 최신의 Description의 리스트를 반환하는 메소드
	 * 
	 * @param effectiveTime(적용일자)
	 * @param conceptId(SNOEMDCT
	 *            Component의 Concept 아이디)
	 * @param active(활성화
	 *            여부)
	 * @return Description의 리스트
	 */
	@Query(value = "SELECT d.* " +
	                 "FROM DESCRIPTION d " +
	                 "INNER JOIN (" +
	                 "   SELECT DESCRIPTION_ID, " +
	                 "          MAX(EFFECTIVE_TIME) as MAX_EFFECTIVE_TIME " +
	                 "   FROM DESCRIPTION " +
	                 "   WHERE CONCEPT_ID = ?2 " +
	                 "   AND EFFECTIVE_TIME <= ?1 " +
	                 "   GROUP BY DESCRIPTION_ID " +
	                 ") as groupDes " +
	                 "ON d.DESCRIPTION_ID = groupDes.DESCRIPTION_ID " +
	                 "AND d.EFFECTIVE_TIME = groupDes.MAX_EFFECTIVE_TIME " +
	                 "AND d.ACTIVE = ?3", nativeQuery = true)
	List<Description> findDescriptionListByEffectiveTimeAndConceptIdAndActive(String effectiveTime, String conceptId,
			int active);
 
	/**
	 * 특정 descriptionId와 EffectiveTime(적용일자)내의 가장 최신의 Description의 리스트를 반환하는 메소드
	 * 
	 * @param effectiveTime(적용일자)
	 * @param descriptionId(SNOEMDCT
	 *            Component의 Description 아이디)
	 * @param active(활성화
	 *            여부)
	 * @return Description의 리스트
	 */
	@Query(value = "SELECT d.* " +
	                 "FROM DESCRIPTION d " +
	                 "INNER JOIN (" +
	                 "   SELECT DESCRIPTION_ID, " +
	                 "          MAX(EFFECTIVE_TIME) as MAX_EFFECTIVE_TIME " +
	                 "   FROM DESCRIPTION " +
	                 "   WHERE DESCRIPTION_ID = ?2 " +
	                 "   AND EFFECTIVE_TIME <= ?1 " +
	                 ") as groupDes " +
	                 "ON d.DESCRIPTION_ID = groupDes.DESCRIPTION_ID " +
	                 "AND d.EFFECTIVE_TIME = groupDes.MAX_EFFECTIVE_TIME " +
	                 "AND d.ACTIVE = ?3", nativeQuery = true)
	List<Description> findDescriptionListByEffectiveTimeAndDescriptionIdAndActive(String effectiveTime,
			String descriptionId, int active);

	/**
	 * 
	 * @param conceptIds
	 * @param languageCode
	 * @param effectiveTime
	 * @return
	 */
	@Query(value = "SELECT d.* " +
	        "FROM DESCRIPTION d " +
	        "INNER JOIN (" +
	        "   SELECT DESCRIPTION_ID, " +
	        "          MAX(EFFECTIVE_TIME) as MAX_EFFECTIVE_TIME " +
	        "   FROM DESCRIPTION " +
	        "   WHERE CONCEPT_ID IN (?1) " +
	        "     AND LANGUAGE_CODE = ?2 " +
	        "     AND TYPE_ID = '900000000000003001' " + // fully specified name
	        "     AND EFFECTIVE_TIME <= ?3 " + 
	        "     GROUP BY DESCRIPTION_ID " +
	        ") as groupDes " +
	        "ON d.DESCRIPTION_ID = groupDes.DESCRIPTION_ID " +
	        "AND d.EFFECTIVE_TIME = groupDes.MAX_EFFECTIVE_TIME " +
	        "AND d.ACTIVE = 1", nativeQuery = true)
	List<Description> findByConceptIdsAndLanguageCodeAndTypeIdAndEffectiveTime(List<String> conceptIds,
			String languageCode, String effectiveTime);

	
	/**
	 * 
	 * @param descriptionIds
	 * @param languageCode
	 * @param effectiveTime
	 * @return
	 */
	@Query(value = "SELECT d.* " +
	        "FROM DESCRIPTION d " +
	        "INNER JOIN (" +
	        "   SELECT DESCRIPTION_ID, " +
	        "          MAX(EFFECTIVE_TIME) as MAX_EFFECTIVE_TIME " +
	        "   FROM DESCRIPTION " +
	        "   WHERE DESCRIPTION_ID IN (?1) " +
	        "     AND LANGUAGE_CODE = ?2 " +
	        "     AND TYPE_ID = '900000000000003001' " + // fully specified name
	        "     AND EFFECTIVE_TIME <= ?3 " + 
	        "     GROUP BY DESCRIPTION_ID " +
	        ") as groupDes " +
	        "ON d.DESCRIPTION_ID = groupDes.DESCRIPTION_ID " +
	        "AND d.EFFECTIVE_TIME = groupDes.MAX_EFFECTIVE_TIME " +
	        "AND d.ACTIVE = 1", nativeQuery = true)
	List<Description> findByDescriptionIdsAndLanguageCodeAndTypeIdAndEffectiveTime(List<String> descriptionIds,
			String languageCode, String effectiveTime);

	
	/**
	 * 특정 Concept에 등록된 Description 중 특정 유형(fsn, prf, syn, def 중 하나)의
	 * Description만 반환하는 메소드
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @param typeId
	 * @return
	 */
	@Query(value = "SELECT D.*  " +
			  "FROM DESCRIPTION_TP AS D " +
			  "INNER JOIN ( " +
			  "	SELECT DESCRIPTION_ID, MAX(EFFECTIVE_TIME) AS MAX_EFFECTIVE_TIME " +
			  "    FROM DESCRIPTION_TP " +
			  "    WHERE CONCEPT_ID = ?1 " +
			  "    AND TYPE_ID = ?3 " +
			  "    AND EFFECTIVE_TIME <= ?2 " +
			  "	GROUP BY DESCRIPTION_ID, LANGUAGE_CODE " +
			  ") AS MD " +
			  "ON D.DESCRIPTION_ID = MD.DESCRIPTION_ID " +
			  "AND D.EFFECTIVE_TIME = MD.MAX_EFFECTIVE_TIME " +
			  "WHERE D.ACTIVE = 1",
			  nativeQuery = true)
	List<Description> findByConceptIdAndEffectiveTimeAndTypeId(String conceptId, String effectiveTime, String typeId);

	
	/**
	 * componentId의 term을 반환
	 * 
	 * @param conceptIds
	 * @param descriptionIds
	 * @param effectiveTime
	 * @return
	 */
	@Query(value = " SELECT D.* " +
					" FROM DESCRIPTION_TP AS D   " +
					" INNER JOIN (   " +
					" 	SELECT DESCRIPTION_ID, MAX(EFFECTIVE_TIME) AS MAX_EFFECTIVE_TIME   " +
					"     FROM DESCRIPTION_TP   " +
					"     WHERE CONCEPT_ID IN (?1) " +
					"     AND TYPE_ID = '900000000000003001' " +
					"     AND LANGUAGE_CODE = 'en' " +
					"     AND EFFECTIVE_TIME <= ?3 " +
					" 	GROUP BY CONCEPT_ID   " +
					" ) AS MD   " +
					" ON D.DESCRIPTION_ID = MD.DESCRIPTION_ID   " +
					" AND D.EFFECTIVE_TIME = MD.MAX_EFFECTIVE_TIME   " +
					" WHERE D.ACTIVE = 1 " +
					" UNION ALL " +
					" SELECT D.* " +
					" FROM DESCRIPTION_TP AS D   " +
					" INNER JOIN (   " +
					" 	SELECT DESCRIPTION_ID, MAX(EFFECTIVE_TIME) AS MAX_EFFECTIVE_TIME   " +
					"     FROM DESCRIPTION_TP   " +
					"     WHERE DESCRIPTION_ID IN (?2) " +
					"     AND LANGUAGE_CODE = 'en' " +
					"     AND EFFECTIVE_TIME <= ?3 " +
					" 	GROUP BY DESCRIPTION_ID   " +
					" ) AS MD   " +
					" ON D.DESCRIPTION_ID = MD.DESCRIPTION_ID   " +
					" AND D.EFFECTIVE_TIME = MD.MAX_EFFECTIVE_TIME   " +
					" WHERE D.ACTIVE = 1 ",
					nativeQuery = true)
	List<Description> findByConceptIdsAndDescriptionIdsAndEffectiveTime(List<String> conceptIds,
			List<String> descriptionIds, String effectiveTime);
}
