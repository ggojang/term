package co.infoclinic.term.icd10.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.infoclinic.term.icd10.repository.Icd10ChildrenRepository;
import co.infoclinic.term.icd10.model.entity.Icd10Children;
import co.infoclinic.term.icd10.model.dto.Icd10ChildrenDTO;
import co.infoclinic.term.icd10.model.converter.Icd10ChildrenToDTOConverter;
import co.infoclinic.term.icd10.service.Icd10ChildrenService;

@Service
@Transactional
public class Icd10ChildrenServiceImpl implements Icd10ChildrenService {
  
  Logger log = LoggerFactory.getLogger(Icd10ChildrenServiceImpl.class);
  
  @Autowired
  private Icd10ChildrenRepository childrenRepository;
  
  @Override
  public List<Icd10Children> getChildrenByClassCode(String code) {
    return childrenRepository.findChildrenByCode(code);
  }

  @Override
  public List<Icd10ChildrenDTO> getChildrenDTOByClassCode(String code) {
    List<Icd10Children> Icd10ChildrenList = childrenRepository.findChildrenByCode(code);
    
    return childrenToDto(Icd10ChildrenList);
    
  }
  
  private List<Icd10ChildrenDTO> childrenToDto(List<Icd10Children> Icd10ChildrenList) {
    return new Icd10ChildrenToDTOConverter().fromChildren(Icd10ChildrenList);
  }
  
}