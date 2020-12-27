package co.infoclinic.term.loinc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.loinc.model.entity.LinguisticVariant;

public interface LinguisticVariantRepository extends JpaRepository<LinguisticVariant, Long> {

	static final String TBL = "loinc.LINGUISTIC_VARIANT ";
	static final String FIND_LIST_BY_CODE = "SELECT * FROM " + TBL + "WHERE CODE = ?1";

	@Query(value = FIND_LIST_BY_CODE, nativeQuery = true)
	List<LinguisticVariant> findListByCode(String code);
	
}
