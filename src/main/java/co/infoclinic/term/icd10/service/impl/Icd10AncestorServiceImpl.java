package co.infoclinic.term.icd10.service.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.infoclinic.term.icd10.repository.Icd10AncestorRepository;
import co.infoclinic.term.icd10.model.converter.Icd10AncestorToDTOConverter;
import co.infoclinic.term.icd10.model.dto.Icd10AncestorDTO;
import co.infoclinic.term.icd10.model.entity.Icd10Ancestor;
import co.infoclinic.term.icd10.service.Icd10AncestorService;
import co.infoclinic.term.icd10.model.entity.Icd10Class;
import co.infoclinic.term.icd10.service.Icd10ClassService;

@Service
@Transactional
public class Icd10AncestorServiceImpl implements Icd10AncestorService {
  
  Logger log = LoggerFactory.getLogger(Icd10AncestorServiceImpl.class);
  
  @Autowired
  private Icd10AncestorRepository ancestorRepository;
  
  @Autowired
  private Icd10ClassService classService;
  
  @Override
  public List<Icd10Ancestor> getAncestorByClassCode(String code) {
	  
	  List<String> codes = new ArrayList<String>();

	  Icd10Class icd10Class = new Icd10Class();
	  
	  icd10Class = classService.getClassByClassCode(code); 
		  
	  Pattern pattern = Pattern.compile( "(?<=~)([\\w-]+)" );
	  Matcher matcher = pattern.matcher( icd10Class.getPath() );

	  while (matcher.find()) {
		  codes.add(matcher.group());
	  }

	  log.info("codes : " + codes);
		
	  /*
	  if (codes.isEmpty()) {
		  codes.add(code);
	  } else if (codes.size() == 1) {
		  codes.add(codes.get(0));
		  codes.add(code);
	  } 
	  */
	  return ancestorRepository.findAncestorByCodes(codes);
  }
  
  @Override
  public List<Icd10AncestorDTO> getAncestorDTOByClassCode(String code) {
	  
	  List<String> codes = new ArrayList<String>();

	  Icd10Class icd10Class = new Icd10Class();
	  
	  icd10Class = classService.getClassByClassCode(code); 
		  
	  Pattern pattern = Pattern.compile( "(?<=~)([\\w-]+)" );
	  Matcher matcher = pattern.matcher( icd10Class.getPath() );

	  while (matcher.find()) {
		  codes.add(matcher.group());
	  }

	  log.info("(DTO) codes : " + codes);
	  /*	  
	  if (codes.isEmpty()) {
		  codes.add(code);
	  } else if (codes.size() == 1) {
		  codes.add(codes.get(0));
		  codes.add(code);
	  }
	  */ 
	  return ancestorToDto(ancestorRepository.findAncestorByCodes(codes));
	 
  }
  
  private List<Icd10AncestorDTO> ancestorToDto(List<Icd10Ancestor> Icd10Ancestors) {
    return new Icd10AncestorToDTOConverter().fromAncestor(Icd10Ancestors);
  }
  
  
}