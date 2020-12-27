package co.infoclinic.term.loinc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.loinc.model.entity.Loinc;
import co.infoclinic.term.loinc.repository.custom.LoincRepositoryCustom;

/**
 * Loinc Repository
 */
public interface LoincRepository extends JpaRepository<Loinc, String>, LoincRepositoryCustom {
	
	static final String TBL = "loinc.LOINC";
	static final String FIND_ENTITY_BY_CD = "SELECT * FROM " + TBL + " WHERE CODE = ?1";

	/**
	 * LOINC Entity 조회
	 * 
	 * <pre>
	 * SELECT * FROM loinc.LOINC
	 * WHERE CODE = ?1
	 * </pre>
	 * 
	 * @param code
	 * @return
	 */
	@Query(value = FIND_ENTITY_BY_CD, nativeQuery = true)
	Loinc findByCode(String code);

}
