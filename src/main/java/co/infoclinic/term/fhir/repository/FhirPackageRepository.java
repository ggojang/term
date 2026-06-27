package co.infoclinic.term.fhir.repository;

import co.infoclinic.term.fhir.model.entity.FhirPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FhirPackageRepository extends JpaRepository<FhirPackage, String> {

    @Query(value = "SELECT id, name, version, description, installed_at FROM fhir.package ORDER BY installed_at DESC", nativeQuery = true)
    List<Object[]> findAllSummary();

    @Query(value = "SELECT resource_type, COUNT(*) FROM fhir.resource WHERE ig_id = :igId GROUP BY resource_type", nativeQuery = true)
    List<Object[]> countByResourceType(@org.springframework.data.repository.query.Param("igId") String igId);
}
