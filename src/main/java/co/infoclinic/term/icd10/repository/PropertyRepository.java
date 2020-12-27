package co.infoclinic.term.icd10.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.infoclinic.term.icd10.model.entity.Icd10Property;


public interface PropertyRepository extends JpaRepository<Icd10Property, Integer> {

}
