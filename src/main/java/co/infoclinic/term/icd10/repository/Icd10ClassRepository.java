package co.infoclinic.term.icd10.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.icd10.model.entity.Icd10Class;


public interface Icd10ClassRepository extends JpaRepository<Icd10Class, String> {

	@Query(value = "SELECT * FROM icd10.ICD10_CLASS WHERE CODE = ?1", nativeQuery = true)
	Icd10Class findByCode(String code);

	@Query(value = "SELECT * FROM icd10.ICD10_CLASS WHERE CODE ILIKE ?1 || '%' ORDER BY length(code), code LIMIT ?3 OFFSET ?2", nativeQuery = true)
	List<Icd10Class> searchByCodePrefix(String q, int offset, int limit);

	@Query(value = "SELECT COUNT(*) FROM icd10.ICD10_CLASS WHERE CODE ILIKE ?1 || '%'", nativeQuery = true)
	long countByCodePrefix(String q);

	@Query(value = "SELECT * FROM icd10.ICD10_CLASS WHERE korean_label ILIKE '%' || ?1 || '%' ORDER BY length(code), code LIMIT ?3 OFFSET ?2", nativeQuery = true)
	List<Icd10Class> searchByKoreanLabel(String q, int offset, int limit);

	@Query(value = "SELECT COUNT(*) FROM icd10.ICD10_CLASS WHERE korean_label ILIKE '%' || ?1 || '%'", nativeQuery = true)
	long countByKoreanLabel(String q);

}