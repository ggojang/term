package co.infoclinic.term.icd10.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.infoclinic.term.icd10.repository.Icd10ClassRepository;
import co.infoclinic.term.icd10.model.entity.Icd10Class;
import co.infoclinic.term.icd10.model.dto.Icd10ClassDTO;
import co.infoclinic.term.icd10.model.converter.Icd10ClassToDTOConverter;
import co.infoclinic.term.icd10.service.Icd10ClassService;

@Service
@Transactional
public class Icd10ClassServiceImpl implements Icd10ClassService {
  
  Logger log = LoggerFactory.getLogger(Icd10ClassServiceImpl.class);
  
  @Autowired
  private Icd10ClassRepository classRepository;
  
  @Override
  public Icd10Class getClassByClassCode(String code) {
    return classRepository.findByCode(code);
  }
  
  @Override
  public Icd10ClassDTO getClassDTOByClassCode(String code) {
    Icd10Class entities = classRepository.findByCode(code);
    
    return classToDto(entities);
  }
  
  private Icd10ClassDTO classToDto(Icd10Class entities) {
    return new Icd10ClassToDTOConverter().fromClass(entities);
  }

  
}