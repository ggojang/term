package co.infoclinic.term.icd10.service;

import co.infoclinic.term.icd10.model.dto.EntityDTO;
import co.infoclinic.term.icd10.model.entity.Icd10Entity;

public interface EntityService {

  Icd10Entity getEntityByEntityCode(String entityCode);
 
  EntityDTO getEntityDTOByEntityCode(String entityCode);

}
