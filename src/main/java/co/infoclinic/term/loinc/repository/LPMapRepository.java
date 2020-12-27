package co.infoclinic.term.loinc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.loinc.model.entity.LPMap;


/**
 * LPMap Repository
 */
public interface LPMapRepository extends JpaRepository<LPMap, String> {
	
	static final String TBL = "loinc.LP_MAP ";
	static final String FIND_LIST_BY_CODE = "SELECT * FROM " + TBL + "WHERE PART_NUMBER = ?1";

	/**
	 * LOINC LPMap 조회
	 * 
	 * <pre>
	 * SELECT * FROM loinc.LP_MAP
	 * WHERE partNumber = ?1
	 * </pre>
	 * 
	 * @param partNumber
	 * @return
	 */
	@Query(value = FIND_LIST_BY_CODE, nativeQuery = true)
	List<LPMap> findListByCode(String code);
}