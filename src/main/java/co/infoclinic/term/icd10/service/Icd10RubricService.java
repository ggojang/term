package co.infoclinic.term.icd10.service;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import co.infoclinic.term.icd10.model.dto.Icd10RubricDTO;

public interface Icd10RubricService {

  Icd10RubricDTO getRubricDTOByClassCode(String code);
  
}
