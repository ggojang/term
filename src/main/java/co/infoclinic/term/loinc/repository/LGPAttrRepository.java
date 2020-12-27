package co.infoclinic.term.loinc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.loinc.model.entity.LGPAttr;

/**
 * LGPAttr Repository
 */
public interface LGPAttrRepository extends JpaRepository<LGPAttr, String> {
	
	static final String TBL = "loinc.PARENT_LG_ATTRIBUTES ";
	static final String FIND_LGPAttr_BY_LGID = "SELECT * FROM " + TBL + "WHERE PARENT_LG_ID = ?1";

	/**
	 * LOINC LGPAttr 조회
	 * 
	 * <pre>
	 * SELECT * FROM loinc.LGPAttr
	 * WHERE LGPAttr_ID = ?1
	 * </pre>
	 * 
	 * @param LGPAttr_ID
	 * @return
	 */
	@Query(value = FIND_LGPAttr_BY_LGID, nativeQuery = true)
	LGPAttr findByLGID(String code);
}