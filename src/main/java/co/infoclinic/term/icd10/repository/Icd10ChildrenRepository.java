package co.infoclinic.term.icd10.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.infoclinic.term.icd10.model.entity.Icd10Children;

public interface Icd10ChildrenRepository extends JpaRepository<Icd10Children, String> {

	@Query(value = "SELECT * FROM icd10.ICD10_CLASS WHERE SUPER_CLASS = ?1 ORDER BY SEQ", nativeQuery = true)
	/*
	@Query(value = "SELECT a.*, r.LABEL FROM icd10.ICD10_RUBRIC as r "
			+ "JOIN (SELECT * FROM icd10.ICD10_CLASS as c WHERE c.SUPER_CLASS = ?1) a "
			+ "ON r.CODE = a.CODE && r.KIND = \"preferred\" ", nativeQuery = true)
	*/
	List<Icd10Children> findChildrenByCode(String code);

}