package co.infoclinic.term.icd10.model.converter;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.infoclinic.term.icd10.model.dto.EntityDTO;
import co.infoclinic.term.icd10.model.entity.Icd10Entity;

public class Icd10EntityToDTOConverter {

  Logger log = LoggerFactory.getLogger(Icd10EntityToDTOConverter.class);
  
  private static ModelMapper modelMapper ; 
  
  public EntityDTO fromEntity(Icd10Entity source) {	
      // Entity가 검색이 안될경
	  if ( source != null) { //
	    EntityDTO dto = createModelMapper().map(source, EntityDTO.class);
        return dto; //
	  } else return null; //
  }
  
  private ModelMapper createModelMapper() {
    PropertyMap<Icd10Entity, EntityDTO> map = new PropertyMap<Icd10Entity, EntityDTO>() {   	
    	
      @Override
      protected void configure() {
        // TODO Auto-generated method stub
        map().setId(source.getId().getId());
        map().setEntity(source.getId().getEntity());
        map().setParent(source.getParent());
        map().setType(source.getType());
        map().setName(source.getName());
      }
    };
  
    getModelMapper().addMappings(map);
    
    return modelMapper;
  }
  
  private ModelMapper getModelMapper() {
// 2017.11.4 disable    
//	  if (modelMapper == null) {
      modelMapper = new ModelMapper();
//    }
    return modelMapper;
   }

}
