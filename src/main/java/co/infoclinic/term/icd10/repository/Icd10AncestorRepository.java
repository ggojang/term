package co.infoclinic.term.icd10.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.infoclinic.term.icd10.model.entity.Icd10Ancestor;

public interface Icd10AncestorRepository extends JpaRepository<Icd10Ancestor, String> {

	@Query(value = "SELECT * FROM icd10.ICD10_CLASS WHERE CODE IN ( :codes ) ORDER BY PATH", nativeQuery = true)

	List<Icd10Ancestor> findAncestorByCodes(@Param("codes") List<String> codes);

}