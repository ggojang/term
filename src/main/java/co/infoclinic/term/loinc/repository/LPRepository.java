package co.infoclinic.term.loinc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.loinc.model.entity.LP;
import co.infoclinic.term.loinc.repository.custom.LPRepositoryCustom;

/**
 * LP Repository
 */
public interface LPRepository extends JpaRepository<LP, String>, LPRepositoryCustom {
	
	static final String TBL = "loinc.LP ";
	static final String FIND_LP_BY_CODE = "SELECT * FROM " + TBL + "WHERE PART_NUMBER = ?1";

	/**
	 * LOINC LP 조회
	 * 
	 * <pre>
	 * SELECT * FROM loinc.LP
	 * WHERE LP = ?1
	 * </pre>
	 * 
	 * @param LP
	 * @return
	 */
	@Query(value = FIND_LP_BY_CODE, nativeQuery = true)
	LP findByCode(String code);
}