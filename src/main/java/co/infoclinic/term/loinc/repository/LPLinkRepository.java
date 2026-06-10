package co.infoclinic.term.loinc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.loinc.model.entity.LPLink;
import co.infoclinic.term.loinc.model.entity.Panel;

/**
 * LPLink Repository
 */
public interface LPLinkRepository extends JpaRepository<LPLink, String> {
	
	static final String TBL = "loinc.LP_LINK ";
	static final String FIND_LIST_BY_CODE = "SELECT * FROM " + TBL + "WHERE LOINC_NUMBER = ?1 AND PART_TYPE_NAME='COMPONENT' ORDER BY PART_NUMBER";
	static final String FIND_LIST_BY_PARTID = "SELECT * FROM " + TBL + "WHERE PART_NUMBER = ?1 AND PART_TYPE_NAME='COMPONENT' ORDER BY LOINC_NUMBER";

	//Sort by LINK_TYPE_NAME='SyntaxEnhancement'
	
	@Query(value = FIND_LIST_BY_CODE, nativeQuery = true)
	List<LPLink> findListByCode(String code);
	
	@Query(value = FIND_LIST_BY_PARTID, nativeQuery = true)
	List<LPLink> findListByPartId(String code);
	
}