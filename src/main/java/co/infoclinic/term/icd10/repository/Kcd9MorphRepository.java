package co.infoclinic.term.icd10.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.icd10.model.entity.Kcd9Morph;

public interface Kcd9MorphRepository extends JpaRepository<Kcd9Morph, String> {

    @Query(value = "SELECT * FROM icd10.KCD9_MORPH WHERE code ILIKE '%' || ?1 || '%' OR korean_label ILIKE '%' || ?1 || '%' OR english_label ILIKE '%' || ?1 || '%' ORDER BY length(code) LIMIT ?3 OFFSET ?2", nativeQuery = true)
    List<Kcd9Morph> search(String q, int offset, int limit);

    @Query(value = "SELECT COUNT(*) FROM icd10.KCD9_MORPH WHERE code ILIKE '%' || ?1 || '%' OR korean_label ILIKE '%' || ?1 || '%' OR english_label ILIKE '%' || ?1 || '%'", nativeQuery = true)
    long searchCount(String q);
}
