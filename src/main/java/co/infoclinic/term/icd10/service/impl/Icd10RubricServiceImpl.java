package co.infoclinic.term.icd10.service.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.icd10.repository.Icd10ClassRepository;
import co.infoclinic.term.icd10.repository.Icd10RubricRepository;
import co.infoclinic.term.icd10.repository.Icd10ChildrenRepository;
import co.infoclinic.term.icd10.model.converter.Icd10RubricMapper;
import co.infoclinic.term.icd10.model.dto.Icd10RubricDTO;
import co.infoclinic.term.icd10.model.dto.Icd10SiblingsDTO;
import co.infoclinic.term.icd10.model.entity.Icd10Class;
import co.infoclinic.term.icd10.model.entity.Icd10Rubric;
import co.infoclinic.term.icd10.service.Icd10RubricService;


@Service
@Transactional
public class Icd10RubricServiceImpl implements Icd10RubricService {

  Logger log = LoggerFactory.getLogger(Icd10RubricServiceImpl.class);
  
  //@Autowired
  //private Icd10ClassService classService;
  
  @Autowired
  private Icd10RubricRepository rubricRepository;

  @Override
  public Icd10RubricDTO getRubricDTOByClassCode(String code) {
    Icd10RubricDTO dto = new Icd10RubricDTO();
    
//   Icd10Class icd10Class = null;
//   icd10Class = classService.getClassByClassCode(code);
    
//    if (icd10Class != null) {
//      List<Icd10Rubric> icd10RubricList = null;
//      icd10RubricList = icd10Class.getIcd10Rubric();
      List<Icd10Rubric> rubricList = rubricRepository.findByCode(code);
      
      dto = Icd10RubricMapper.mapClassesIntoDTOList(rubricList);
//    }
    
    return dto;
  }
  

}
