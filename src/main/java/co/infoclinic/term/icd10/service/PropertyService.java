package co.infoclinic.term.icd10.service;

import co.infoclinic.term.icd10.model.dto.PropertyDTO;

public interface PropertyService {

  PropertyDTO getByEntityCode(String entityCode);
}
