package co.infoclinic.term.icd10.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.infoclinic.term.icd10.model.entity.Icd10Class;
//import co.infoclinic.term.icd10.model.entity.Icd10Rubric;


public interface Icd10ClassRepository extends JpaRepository<Icd10Class, String> {

	@Query(value = "SELECT * FROM icd10.ICD10_CLASS WHERE CODE = ?1", nativeQuery = true)
	Icd10Class findByCode(String code);

}