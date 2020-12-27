package co.infoclinic.term.loinc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.loinc.model.entity.LGTerm;

/**
 * LGTerm Repository
 */
public interface LGTermRepository extends JpaRepository<LGTerm, String> {
	
	static final String TBL = "loinc.LG_TERMS ";
	static final String FIND_LGTERM_BY_LGID = "SELECT * FROM " + TBL + "WHERE LG_ID = ?1";
	static final String FIND_LGTERM_BY_CODE = "SELECT * FROM " + TBL + "WHERE LOINC_NUMBER = ?1";

	@Query(value = FIND_LGTERM_BY_LGID, nativeQuery = true)
	List<LGTerm> findListByLGID(String code);
	
	@Query(value = FIND_LGTERM_BY_CODE, nativeQuery = true)
	List<LGTerm> findListByCode(String code);
}