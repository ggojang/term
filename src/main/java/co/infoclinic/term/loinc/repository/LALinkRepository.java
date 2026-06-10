package co.infoclinic.term.loinc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.loinc.model.entity.LALink;

/**
 * LALink Repository
 */
public interface LALinkRepository extends JpaRepository<LALink, String> {
	
	static final String TBL = "loinc.LA_LINK ";
	static final String FIND_LIST_BY_CODE = "SELECT * FROM " + TBL + "WHERE LOINC_NUMBER = ?1 AND ANSWER_LIST_LINK_TYPE=\"PREFERRED\" ";

	/**
	 * LOINC LALink 조회
	 * 
	 * <pre>
	 * SELECT * FROM loinc.LP_LINK
	 * WHERE partNumber = ?1 and partTypeName="COMPONENT"
	 * ORDER BY loincNumber
	 * </pre>
	 * 
	 * @param partNumber
	 * @return
	 */
	@Query(value = FIND_LIST_BY_CODE, nativeQuery = true)
	List<LALink> findListByCode(String code);
	
}