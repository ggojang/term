package co.infoclinic.term.icd10.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.infoclinic.term.icd10.model.entity.Icd10Entity;
import co.infoclinic.term.icd10.model.entity.Icd10EntityId;
import co.infoclinic.term.icd10.model.entity.Icd10Property;


public interface EntityRepository extends JpaRepository<Icd10Property, Icd10EntityId> {

  @Query(value = "SELECT e FROM Icd10Entity e WHERE e.id.entity = :entity")
  Icd10Entity findByEntity(@Param("entity") String entity);

}
