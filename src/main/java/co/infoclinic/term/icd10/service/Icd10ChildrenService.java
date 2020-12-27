package co.infoclinic.term.icd10.service;

import java.util.List;

import co.infoclinic.term.icd10.model.dto.Icd10ChildrenDTO;
import co.infoclinic.term.icd10.model.entity.Icd10Children;

public interface Icd10ChildrenService {

  List<Icd10Children> getChildrenByClassCode(String code);
 
  List<Icd10ChildrenDTO> getChildrenDTOByClassCode(String code);

}