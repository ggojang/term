package co.infoclinic.term.loinc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.loinc.model.entity.LA;

/**
 * LA Repository
 */
public interface LARepository extends JpaRepository<LA, String> {
	
	static final String TBL = "loinc.LA ";
	static final String FIND_LIST_BY_CODE = "SELECT * FROM " + TBL + "WHERE ANSWER_LIST_ID = ?1 ORDER BY SEQUENCE_NUMBER";

	/**
	 * LOINC LA 조회
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
	List<LA> findListByCode(String code);
	
}