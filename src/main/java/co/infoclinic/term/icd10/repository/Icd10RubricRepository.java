package co.infoclinic.term.icd10.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.icd10.model.entity.Icd10Rubric;

public interface Icd10RubricRepository extends JpaRepository<Icd10Rubric, String> {

	@Query(value = "SELECT * FROM icd10.ICD10_RUBRIC WHERE CODE = ?1", nativeQuery = true)
	ArrayList<Icd10Rubric> findByCode(String code);

	@Query(value = "SELECT * FROM icd10.ICD10_RUBRIC WHERE kind = 'preferred' AND label ILIKE '%' || ?1 || '%' ORDER BY length(label) LIMIT ?3 OFFSET ?2", nativeQuery = true)
	List<Icd10Rubric> searchByLabel(String q, int offset, int limit);

	@Query(value = "SELECT COUNT(*) FROM icd10.ICD10_RUBRIC WHERE kind = 'preferred' AND label ILIKE '%' || ?1 || '%'", nativeQuery = true)
	long countByLabel(String q);

	// Searches Korean labels in ICD10_CLASS (returns Object[]: code, label, kind, korean_label)
	@Query(value = "SELECT r.code, r.label, r.kind, c.korean_label FROM icd10.ICD10_RUBRIC r JOIN icd10.ICD10_CLASS c ON r.code = c.code WHERE r.kind = 'preferred' AND c.korean_label ILIKE '%' || ?1 || '%' AND r.label NOT ILIKE '%' || ?1 || '%' ORDER BY length(r.label) LIMIT ?3 OFFSET ?2", nativeQuery = true)
	List<Object[]> searchByKoreanLabel(String q, int offset, int limit);

	@Query(value = "SELECT COUNT(*) FROM icd10.ICD10_RUBRIC r JOIN icd10.ICD10_CLASS c ON r.code = c.code WHERE r.kind = 'preferred' AND c.korean_label ILIKE '%' || ?1 || '%' AND r.label NOT ILIKE '%' || ?1 || '%'", nativeQuery = true)
	long countByKoreanLabel(String q);

	// Batch fetch Korean labels + isKcdExt by codes (Object[]: code, korean_label, is_kcd_ext)
	@Query(value = "SELECT code, korean_label, is_kcd_ext FROM icd10.ICD10_CLASS WHERE code IN ?1", nativeQuery = true)
	List<Object[]> findKoreanLabelsByCodes(List<String> codes);
}
