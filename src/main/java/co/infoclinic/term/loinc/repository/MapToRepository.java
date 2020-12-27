package co.infoclinic.term.loinc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.infoclinic.term.loinc.model.entity.MapTo;

public interface MapToRepository extends JpaRepository<MapTo, Long> {

}
