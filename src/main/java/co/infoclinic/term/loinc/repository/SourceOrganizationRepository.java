package co.infoclinic.term.loinc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.infoclinic.term.loinc.model.entity.SourceOrganization;

public interface SourceOrganizationRepository extends JpaRepository<SourceOrganization, Long> {

}
