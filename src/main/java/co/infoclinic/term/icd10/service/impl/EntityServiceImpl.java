package co.infoclinic.term.icd10.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.infoclinic.term.icd10.model.converter.Icd10EntityToDTOConverter;
import co.infoclinic.term.icd10.model.dto.EntityDTO;
import co.infoclinic.term.icd10.model.entity.Icd10Entity;
import co.infoclinic.term.icd10.repository.EntityRepository;
import co.infoclinic.term.icd10.service.EntityService;

@Service
@Transactional
public class EntityServiceImpl implements EntityService {
  
  Logger log = LoggerFactory.getLogger(EntityServiceImpl.class);
  
  @Autowired
  private EntityRepository entityRepository;
  
  @Override
  public Icd10Entity getEntityByEntityCode(String entityCode) {
    return entityRepository.findByEntity(entityCode);
  }

  @Override
  public EntityDTO getEntityDTOByEntityCode(String entityCode) {
    Icd10Entity entity = entityRepository.findByEntity(entityCode);
    
    return entityToDto(entity);
  }
  
  private EntityDTO entityToDto(Icd10Entity entity) {
    return new Icd10EntityToDTOConverter().fromEntity(entity);//modelMapper.map(entity, EntityDTO.class);
  }

  
}
