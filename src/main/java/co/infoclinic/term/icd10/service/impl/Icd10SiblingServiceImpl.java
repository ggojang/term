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
import co.infoclinic.term.icd10.repository.Icd10SiblingRepository;
import co.infoclinic.term.icd10.model.converter.Icd10SiblingMapper;
import co.infoclinic.term.icd10.model.dto.Icd10SiblingsDTO;
import co.infoclinic.term.icd10.model.entity.Icd10Class;
import co.infoclinic.term.icd10.model.entity.Icd10Children;
import co.infoclinic.term.icd10.model.entity.Icd10Sibling;
//import co.infoclinic.term.icd10.service.Icd10ClassService;
import co.infoclinic.term.icd10.service.Icd10ChildrenService;
import co.infoclinic.term.icd10.service.Icd10SiblingService;


@Service
@Transactional
public class Icd10SiblingServiceImpl implements Icd10SiblingService {

  Logger log = LoggerFactory.getLogger(Icd10SiblingServiceImpl.class);
  
  //@Autowired
  //private Icd10ClassService classService;
  
  @Autowired
  private Icd10SiblingRepository siblingRepository;

  @Autowired
  private Icd10ClassRepository classRepository;
  
  @Autowired
  private Icd10ChildrenRepository childrenRepository;
  
  @Override
  public Icd10SiblingsDTO getSiblingsDTOByClassCode(String code) {
	  //ArrayList<ArrayList<Icd10Sibling>> ss = new ArrayList<ArrayList<Icd10Sibling>>();
	  ArrayList<Icd10Sibling> dto = new ArrayList<Icd10Sibling>();
	  
	  
	  Icd10Class c = new Icd10Class();
	  c = classRepository.findByCode(code);
	  
	  Icd10Children ch = new Icd10Children();
	  List<Icd10Children> chs = childrenRepository.findChildrenByCode(c.getSuperClass()); 
	  
	  List<String> codes = new ArrayList<String>();
	  for (int i=0; i < chs.size(); i++) {
		  codes.add(chs.get(i).getCode());		  
	  }
	  
	  //log.info("codes : + codes");
	  //List<Icd10Sibling> r = new ArrayList<Icd10Sibling>();
	  /*
	  for(int i=0; i < chs.size(); i++) {
		  dto = siblingRepository.findByCode(chs.get(i).getCode());
		  log.info("for dto from Rubric : " + ss);
		  ss.add(dto);  
	  }
	  */
	  dto = siblingRepository.findByCode(codes);
	  //ss.add(dto);
	  //log.info("ss : " + ss);
	  
	  return Icd10SiblingMapper.mapSiblingIntoDTOMap(dto);
  }
  
}
