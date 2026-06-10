package co.infoclinic.term.snomedct.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.infoclinic.term.snomedct.model.entity.SCTID;

/**
 * SNOMED CT ID Repository
 */
public interface SCTIDRepository extends JpaRepository<SCTID, Long> {

}
