package co.infoclinic.term.icd10.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.icd10.model.converter.PropertyMapper;
import co.infoclinic.term.icd10.model.dto.PropertyDTO;
import co.infoclinic.term.icd10.model.entity.Icd10Attribute;
import co.infoclinic.term.icd10.model.entity.Icd10Entity;
import co.infoclinic.term.icd10.model.entity.Icd10Property;
import co.infoclinic.term.icd10.service.EntityService;
import co.infoclinic.term.icd10.service.PropertyService;


@Service
@Transactional
public class PropertyServiceImpl implements PropertyService {

  Logger log = LoggerFactory.getLogger(PropertyServiceImpl.class);
  
  @Autowired
  private EntityService entityService;
  
  @Override
  public PropertyDTO getByEntityCode(String entityCode) {
    PropertyDTO dto = new PropertyDTO();
    
    Icd10Entity icd10Entity = null;
    icd10Entity = entityService.getEntityByEntityCode(entityCode);
    
    if (icd10Entity != null) {
      List<Icd10Property> icd10PropertyList = null;
      icd10PropertyList = icd10Entity.getIcd10Properties();
      
      Icd10Attribute icd10Attribute = icd10Entity.getIcd10Attribute();
      dto = PropertyMapper.mapEntitiesIntoDTOList(icd10PropertyList, icd10Attribute);
    }
    
    return dto;
  }
}
