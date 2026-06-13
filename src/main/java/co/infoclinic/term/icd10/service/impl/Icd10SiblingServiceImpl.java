package co.infoclinic.term.icd10.service.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.icd10.repository.Icd10ClassRepository;
import co.infoclinic.term.icd10.repository.Icd10RubricRepository;
import co.infoclinic.term.icd10.repository.Icd10ChildrenRepository;
import co.infoclinic.term.icd10.repository.Icd10SiblingRepository;
import co.infoclinic.term.icd10.model.entity.Icd10Class;
import co.infoclinic.term.icd10.model.converter.Icd10SiblingMapper;
import co.infoclinic.term.icd10.model.dto.Icd10RubricDTO;
import co.infoclinic.term.icd10.model.dto.Icd10SiblingsDTO;
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
  private Icd10RubricRepository rubricRepository;

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

	  // Batch fetch Korean label + isKcdExt from ICD10_CLASS
	  Map<String, String> koreanMap = new HashMap<String, String>();
	  Map<String, Boolean> extMap = new HashMap<String, Boolean>();
	  List<Object[]> korRows = rubricRepository.findKoreanLabelsByCodes(codes);
	  for (Object[] row : korRows) {
		  if (row[0] == null) continue;
		  String cd = row[0].toString();
		  if (row[1] != null) koreanMap.put(cd, row[1].toString());
		  if (row[2] != null) extMap.put(cd, (Boolean) row[2]);
	  }

	  // If no rubric entries (e.g. KCD-9 extended codes), build from ICD10_CLASS directly
	  if (dto.isEmpty()) {
		  ArrayList<Icd10RubricDTO> fallback = new ArrayList<Icd10RubricDTO>();
		  for (Icd10Children sibling : chs) {
			  Icd10RubricDTO d = new Icd10RubricDTO();
			  d.setCode(sibling.getCode());
			  d.setLabel(sibling.getLabel());
			  d.setKoreanLabel(sibling.getKoreanLabel());
		  d.setIsKcdExt(sibling.getIsKcdExt());
			  d.setKinds(new ArrayList<co.infoclinic.term.icd10.model.dto.Icd10RubricKindDTO>());
			  fallback.add(d);
		  }
		  Icd10SiblingsDTO result = new Icd10SiblingsDTO();
		  result.setSiblings(fallback);
		  return result;
	  }

	  return Icd10SiblingMapper.mapSiblingIntoDTOMap(dto, koreanMap, extMap);
  }
  
}
