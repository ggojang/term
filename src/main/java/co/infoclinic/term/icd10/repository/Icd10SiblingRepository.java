package co.infoclinic.term.icd10.repository;

import java.util.List;
import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.icd10.model.entity.Icd10Sibling;

public interface Icd10SiblingRepository extends JpaRepository<Icd10Sibling, String> {

	@Query(value = "SELECT * FROM icd10.ICD10_RUBRIC WHERE CODE IN ( ?1 )", nativeQuery = true)
	ArrayList<Icd10Sibling> findByCode(List<String> code);
}
