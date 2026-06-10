package co.infoclinic.term.icd10.repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.icd10.model.entity.Icd10Rubric;

public interface Icd10RubricRepository extends JpaRepository<Icd10Rubric, String> {

	@Query(value = "SELECT * FROM icd10.ICD10_RUBRIC WHERE CODE = ?1", nativeQuery = true)
	ArrayList<Icd10Rubric> findByCode(String code);
}
