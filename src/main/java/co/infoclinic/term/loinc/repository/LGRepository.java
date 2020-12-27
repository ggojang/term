package co.infoclinic.term.loinc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.loinc.model.entity.LG;

/**
 * LG Repository
 */
public interface LGRepository extends JpaRepository<LG, String> {
	
	static final String TBL = "loinc.LG ";
	static final String FIND_LG_BY_LGID = "SELECT * FROM " + TBL + "WHERE LG_ID = ?1";

	/**
	 * LOINC LG 조회
	 * 
	 * <pre>
	 * SELECT * FROM loinc.LG
	 * WHERE LG_ID = ?1
	 * </pre>
	 * 
	 * @param LG_ID
	 * @return
	 */
	@Query(value = FIND_LG_BY_LGID, nativeQuery = true)
	List<LG> findListByLGID(String code);
}