package co.infoclinic.term.loinc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.loinc.model.entity.LGAttr;

/**
 * LGAttr Repository
 */
public interface LGAttrRepository extends JpaRepository<LGAttr, String> {
	
	static final String TBL = "loinc.LG_ATTRIBUTES ";
	static final String FIND_LGATTR_BY_LGID = "SELECT * FROM " + TBL + "WHERE LG_ID = ?1";

	/**
	 * LOINC LGAttr 조회
	 * 
	 * <pre>
	 * SELECT * FROM loinc.LGAttr
	 * WHERE LG_ID = ?1
	 * </pre>
	 * 
	 * @param LG_ID
	 * @return
	 */
	@Query(value = FIND_LGATTR_BY_LGID, nativeQuery = true)
	List<LGAttr> findListByLGID(String code);
}