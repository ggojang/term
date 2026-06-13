package co.infoclinic.term.icd10.controller;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/*
import co.infoclinic.term.icd10.model.dto.EntityDTO;
import co.infoclinic.term.icd10.model.dto.PropertyDTO;
import co.infoclinic.term.icd10.service.EntityService;
import co.infoclinic.term.icd10.service.PropertyService;
*/
import co.infoclinic.term.icd10.model.dto.Icd10ClassDTO;
import co.infoclinic.term.icd10.model.entity.Icd10Rubric;
import co.infoclinic.term.icd10.model.dto.Icd10RubricDTO;
import co.infoclinic.term.icd10.model.dto.Icd10ChildrenDTO;
import co.infoclinic.term.icd10.model.dto.Icd10AncestorDTO;
import co.infoclinic.term.icd10.model.dto.Icd10SiblingsDTO;
import co.infoclinic.term.icd10.service.Icd10ClassService;
import co.infoclinic.term.icd10.service.Icd10RubricService;
import co.infoclinic.term.icd10.service.Icd10ChildrenService;
import co.infoclinic.term.icd10.service.Icd10AncestorService;
import co.infoclinic.term.icd10.service.Icd10SiblingService;
import co.infoclinic.term.icd10.api.QryApi;
import co.infoclinic.term.icd10.model.entity.Kcd9Morph;
import co.infoclinic.term.icd10.repository.Kcd9MorphRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Icd10 Rest Controller
 * 
 * @author dongwon
 *
 */
@Api(value = "Entity", description = "Entity", tags = "III-1. ICD10")
@RestController
// entity|property/ICD10/code 형태를 위해 disable
// @RequestMapping("/ICD10")
public class Icd10RestController {

  Logger log = LoggerFactory.getLogger(Icd10RestController.class);
/*  
  @Autowired
  private EntityService entitySerivce;
  
  @Autowired
  private PropertyService propertyService;
*/ 
  @Autowired
  private Icd10ClassService classService;
  
  @Autowired
  private Icd10RubricService rubricService;
 
  @Autowired
  private Icd10ChildrenService childrenService;
  
  @Autowired
  private Icd10AncestorService ancestorService;
  
  @Autowired
  private Icd10SiblingService siblingService;

  @Autowired
  private Kcd9MorphRepository morphRepository;
  
  
  /**
   * Get Entity By EntityCode
   * 
   * @param entityCode(e.g. B34.0)
   * @return
   
  @ApiOperation(value = "Get Entity")
  // @RequestMapping(value = "/{entityCode:.+}", method = RequestMethod.GET)
  @RequestMapping(value = "/entity/ICD10/{entityCode:.+}", method = RequestMethod.GET)
  public EntityDTO getEntityByEntityCode(@PathVariable String entityCode) {
    return entitySerivce.getEntityDTOByEntityCode(entityCode);
  }
  
  @ApiOperation(value = "Get Property")
  //@RequestMapping(value = "/{entityCode:.+}/property", method = RequestMethod.GET)
  @RequestMapping(value = "/property/ICD10/{entityCode:.+}", method = RequestMethod.GET)
  public PropertyDTO getPropertyByEntityCode(@PathVariable String entityCode) {
    return propertyService.getByEntityCode(entityCode);
  }
*/
  
  @ApiOperation(value = "Get Entity")
  @RequestMapping(value = "/entity/ICD10/{code:.+}", method = RequestMethod.GET)
  public Icd10ClassDTO getClassByClassCode(@PathVariable String code) {
	  return classService.getClassDTOByClassCode(code);
  }
    
  @ApiOperation(value = "Get Rubric")
  @RequestMapping(value = "/rubric/ICD10/{code:.+}", method = RequestMethod.GET)
  public Icd10RubricDTO getRubricByClassCode(@PathVariable String code) {
	  return rubricService.getRubricDTOByClassCode(code);
  }
  
  @ApiOperation(value = "Get Sibling")
  @RequestMapping(value = "/sibling/ICD10/{code:.+}", method = RequestMethod.GET)
  public Icd10SiblingsDTO getSiblingsByClassCode(@PathVariable String code) {
	  return siblingService.getSiblingsDTOByClassCode(code);
  }

  @ApiOperation(value = "Get Ancestor")
  @RequestMapping(value = "/ancestor/ICD10/{code:.+}", method = RequestMethod.GET)
  public List<Icd10AncestorDTO> getAncestorByClassCode(@PathVariable String code) {
	  return ancestorService.getAncestorDTOByClassCode(code);
  }

  @ApiOperation(value = "Get Children")
  @RequestMapping(value = "/children/ICD10/{code:.+}", method = RequestMethod.GET)
  public List<Icd10ChildrenDTO> getChildrenByClassCode(@PathVariable String code) {
	  return childrenService.getChildrenDTOByClassCode(code);
  }

  // ── KCD-9 Neoplasm Morphology ──────────────────────────────────────────────

  @ApiOperation(value = "List all KCD9 Morphology")
  @RequestMapping(value = "/kcd9/morph/all", method = RequestMethod.GET)
  public List<Kcd9Morph> getAllMorph() {
      return morphRepository.findAll();
  }

  @ApiOperation(value = "Search KCD9 Morphology")
  @RequestMapping(value = "/kcd9/morph/search", method = RequestMethod.GET)
  public Page<Kcd9Morph> searchMorph(
          @RequestParam String q,
          @RequestParam(defaultValue = "1") int page,
          @RequestParam(defaultValue = "50") int size) {
      int offset = (page - 1) * size;
      List<Kcd9Morph> content = morphRepository.search(q, offset, size);
      long total = morphRepository.searchCount(q);
      return new PageImpl<>(content, new PageRequest(page - 1, size), total);
  }

}
