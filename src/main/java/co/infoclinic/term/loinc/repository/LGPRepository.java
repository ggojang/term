package co.infoclinic.term.loinc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.loinc.model.entity.LGP;

/**
 * LGP Repository
 */
public interface LGPRepository extends JpaRepository<LGP, String> {
	
	static final String TBL = "loinc.PARENT_LG ";
	static final String FIND_LGP_BY_LGID = "SELECT * FROM " + TBL + "WHERE PARENT_LG_ID = ?1";

	/**
	 * LOINC LGP 조회
	 * 
	 * <pre>
	 * SELECT * FROM loinc.LGP
	 * WHERE LGP_ID = ?1
	 * </pre>
	 * 
	 * @param LGP_ID
	 * @return
	 */
	@Query(value = FIND_LGP_BY_LGID, nativeQuery = true)
	LGP findByLGID(String code);
}