package co.infoclinic.term.loinc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.loinc.model.entity.Hierarchy;
import co.infoclinic.term.loinc.repository.custom.HierarchyRepositoryCustom;

/**
 * Hierarchy Repository
 */
public interface HierarchyRepository extends JpaRepository<Hierarchy, Long> , HierarchyRepositoryCustom  {
	
	
	// ----------------------------------------
	// Children
	// ----------------------------------------

	/**
	 * 자식 목록 검색
	 * 
	 * @param code
	 * @return
	 */
	@Query(value=
			 "SELECT * " +
		     "FROM loinc.HIERARCHY " +
		     "WHERE PARENT = ?1 " +
		     "ORDER BY SEQUENCE, NAME", nativeQuery=true)
	List<Hierarchy> findChildrenByCode(String code);
	
	
	/**
	 * 자식 목록 검색
	 * 
	 * @param code
	 * @param path
	 * @return
	 */
	@Query(value=
			 "SELECT * " +
			 "FROM loinc.HIERARCHY " +
			 "WHERE PARENT = ?1 " +
		     "AND PATH = ?2 " +
		     "ORDER BY SEQUENCE, NAME", nativeQuery=true)
	List<Hierarchy> findChildrenByCodeAndPath(String code, String path);
	
	/**
	 * 특정 언어(Linguistic Variant)의 자식 목록 검색
	 * @param code
	 * @param path
	 * @param lang
	 * @param country
	 * @return
	 */
	@Query(value=
			 "SELECT " +
		     "  h.SEQ, " +
			 "  h.CODE, " +
		     "  CASE WHEN l.CODE IS NULL " +
			 "    THEN h.NAME " +
		     "    ELSE CONCAT( " +
			 "      l.COMPONENT, ':', " +
		     "      l.PROPERTY, ':', " +
			 "      l.TIME_ASPECT, ':', " +
		     "      l.SYSTEM, ':', " +
			 "      l.SCALE_TYPE, " +
		     "      IF(LENGTH(l.METHOD_TYPE), CONCAT(':', l.METHOD_TYPE), '') " +
			 "    ) " +
		     "  END AS NAME, " +
		     "  CASE WHEN l.CODE IS NULL " +
			 "    THEN h.PREFERRED_NAME " +
		     "    ELSE CONCAT( " +
			 "      l.COMPONENT, ':', " +
		     "      l.PROPERTY, ':', " +
			 "      l.TIME_ASPECT, ':', " +
		     "      l.SYSTEM, ':', " +
			 "      l.SCALE_TYPE, " +
		     "      IF(LENGTH(l.METHOD_TYPE), CONCAT(':', l.METHOD_TYPE), '') " +
			 "    ) " +
		     "  END AS PREFERRED_NAME, " +
			 "  h.TYPE, " +
		     "  h.PATH, " +
		     "  h.CHILDREN_COUNT, " +
		     "  h.DESCENDANT_COUNT, " +
		     "  h.SEQUENCE, " +
		     "  h.PARENT " +
		     "FROM loinc.HIERARCHY AS h " +
		     "LEFT JOIN loinc.LINGUISTIC_VARIANT AS l " +
		     "ON l.CODE = h.CODE " +
		     "AND l.ISO_LANG = ?2 " +
		     "AND l.ISO_COUNTRY = ?3 " +
		     "WHERE PARENT = ?1 " +
		     "ORDER BY SEQUENCE, NAME", nativeQuery=true)
	List<Hierarchy> findChildrenByCodeAndLangAndCountry(String code, String lang, String country);
	
	/**
	 * 특정 언어(Linguistic Variant)의 자식 목록 검색
	 * @param code
	 * @param path
	 * @param lang
	 * @param country
	 * @return
	 */
	@Query(value=
			 "SELECT " +
		     "  h.SEQ, " +
			 "  h.CODE, " +
		     "  CASE WHEN l.CODE IS NULL " +
			 "    THEN h.NAME " +
		     "    ELSE CONCAT( " +
			 "      l.COMPONENT, ':', " +
		     "      l.PROPERTY, ':', " +
			 "      l.TIME_ASPECT, ':', " +
		     "      l.SYSTEM, ':', " +
			 "      l.SCALE_TYPE, " +
		     "      IF(LENGTH(l.METHOD_TYPE), CONCAT(':', l.METHOD_TYPE), '') " +
			 "    ) " +
		     "  END AS NAME, " +
		     "  CASE WHEN l.CODE IS NULL " +
			 "    THEN h.PREFERRED_NAME " +
		     "    ELSE CONCAT( " +
			 "      l.COMPONENT, ':', " +
		     "      l.PROPERTY, ':', " +
			 "      l.TIME_ASPECT, ':', " +
		     "      l.SYSTEM, ':', " +
			 "      l.SCALE_TYPE, " +
		     "      IF(LENGTH(l.METHOD_TYPE), CONCAT(':', l.METHOD_TYPE), '') " +
			 "    ) " +
		     "  END AS PREFERRED_NAME, " +
			 "  h.TYPE, " +
		     "  h.PATH, " +
		     "  h.CHILDREN_COUNT, " +
		     "  h.DESCENDANT_COUNT, " +
		     "  h.SEQUENCE, " +
		     "  h.PARENT " +
		     "FROM loinc.HIERARCHY AS h " +
		     "LEFT JOIN loinc.LINGUISTIC_VARIANT AS l " +
		     "ON l.CODE = h.CODE " +
		     "AND l.ISO_LANG = ?3 " +
		     "AND l.ISO_COUNTRY = ?4 " +
		     "WHERE PARENT = ?1 " +
		     "AND PATH = ?2 " +
		     "ORDER BY SEQUENCE, NAME", nativeQuery=true)
	List<Hierarchy> findChildrenByCodeAndPathAndLangAndCountry(String code, String path, String lang, String country);
	
	
	// ----------------------------------------
	// Parents
	// ----------------------------------------
	
	/**
	 * 부모 목록 검색
	 * 
	 * @param code
	 * @return
	 */
	@Query(value="SELECT HR.* " +
				 "FROM ( " +
				 "  SELECT PARENT AS CODE " +
				 "  FROM loinc.HIERARCHY " +
				 "  WHERE CODE = ?1 " +
				 "  GROUP BY PARENT " +
				 ") AS PRNT " +
				 "INNER JOIN loinc.HIERARCHY AS HR " +
				 "ON HR.CODE = PRNT.CODE " +
				 "ORDER BY SEQUENCE, NAME", nativeQuery=true)
	List<Hierarchy> findParentListByCode(String code);

	
	
	
	
	// ----------------------------------------
	// Descendants
	// ----------------------------------------
	
	/**
	 * 코드 하위 목록
	 * 
	 * @param code
	 * @return
	 */
	@Query(value="SELECT h.* " +
			 "FROM loinc.HIERARCHY AS h " +
			 "INNER JOIN ( " +
			 "SELECT CONCAT(PATH, '~', ?1, '%') AS PATH " +
			 "  FROM loinc.HIERARCHY " +
			 "  WHERE CODE = ?1" +
			 ") AS t " +
			 "ON h.PATH LIKE t.PATH ", nativeQuery=true)
	List<Hierarchy> findDescendantListByCode(String code);
	
	
	/**
	 * 코드 자신 및 모든 하위 목록
	 * 
	 * @param code
	 * @return
	 */
	@Query(value="SELECT h.* " +
			 "FROM loinc.HIERARCHY AS h " +
			 "INNER JOIN ( " +
			 "SELECT CONCAT(PATH, '~', ?1, '%') AS PATH " +
			 "  FROM loinc.HIERARCHY " +
			 "  WHERE CODE = ?1" +
			 ") AS t " +
			 "ON h.PATH LIKE t.PATH " +
			 "UNION ALL " +
			 "SELECT * " +
			 "FROM loinc.HIERARCHY " +
			 "WHERE CODE = ?1", nativeQuery=true)
	List<Hierarchy> findDescendantOrSelfListByCode(String code);
	
	
	/**
	 * 코드와 특정 경로에 존재하는 모든 하위 목록
	 * 
	 * @param code
	 * @param path
	 * @return
	 */
	@Query(value="SELECT h.* " +
			 "FROM loinc.HIERARCHY AS h " +
			 "INNER JOIN ( " +
			 "SELECT CONCAT(PATH, '~', ?1, '%') AS PATH " +
			 "  FROM loinc.HIERARCHY " +
			 "  WHERE CODE = ?1" +
			 "  AND PATH = ?2 " +
			 ") AS t " +
			 "ON h.PATH LIKE t.PATH ", nativeQuery=true)
	List<Hierarchy> findDescendantListByCodeAndPath(String code, String path);
	
	
	/**
	 * 코드와 특정 경로에 존재하는 자신 및 모든 하위 목록
	 * 
	 * @param code
	 * @param path
	 * @return
	 */
	@Query(value="SELECT h.* " +
			 "FROM loinc.HIERARCHY AS h " +
			 "INNER JOIN ( " +
			 "SELECT CONCAT(PATH, '~', ?1, '%') AS PATH " +
			 "  FROM loinc.HIERARCHY " +
			 "  WHERE CODE = ?1" +
			 "  AND PATH = ?2 " +
			 ") AS t " +
			 "ON h.PATH LIKE t.PATH " +
			 "UNION ALL " +
			 "SELECT * " +
			 "FROM loinc.HIERARCHY " +
			 "WHERE CODE = ?1 " +
			 "AND PATH = ?2", nativeQuery=true)
	List<Hierarchy> findDescendantOrSelfListByCodeAndPath(String code, String path);
	
	
	// ----------------------------------------
	// Others
	// ----------------------------------------
	
	/**
	 * 코드의 계층 목록 검색하는 쿼리
	 * 
	 * @param code
	 * @return
	 */
	@Query(value = "SELECT * " +
				   "FROM loinc.HIERARCHY " +
				   "WHERE CODE = ?1 ", nativeQuery = true)
	List<Hierarchy> findByCode(String code);


	/**
	 * Entity Id간 포함여부 조회
	 * 
	 * 계층구조에서 Concept Id가 Criteria Id내에 포함되어 있는지 확인 
	 * 
	 * @param criteriaCode
	 * @param code
	 * @return
	 */
	@Query(value = "SELECT COUNT(CODE) " +
				   "FROM loinc.HIERARCHY " + 
				   "WHERE CODE = ?2 " + 
				   "AND PATH LIKE CONCAT ('%', ?1, '%')", nativeQuery = true)
	int findCountByCriteriaCodeAndCode(String criteriaCode, String code);
}
