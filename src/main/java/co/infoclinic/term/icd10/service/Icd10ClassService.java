package co.infoclinic.term.icd10.service;

import co.infoclinic.term.icd10.model.dto.Icd10ClassDTO;
import co.infoclinic.term.icd10.model.entity.Icd10Class;

public interface Icd10ClassService {

  Icd10Class getClassByClassCode(String code);
 
  Icd10ClassDTO getClassDTOByClassCode(String code);

}
