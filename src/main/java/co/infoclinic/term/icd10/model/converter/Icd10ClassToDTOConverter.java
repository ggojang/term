package co.infoclinic.term.icd10.model.converter;

//import org.modelmapper.ModelMapper;
//import org.modelmapper.PropertyMap;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.icd10.model.dto.Icd10ClassDTO;
import co.infoclinic.term.icd10.model.entity.Icd10Class;

@Component(value = "Icd10ClassCvt")
public final class Icd10ClassToDTOConverter {
	static final Logger log = LoggerFactory.getLogger(Icd10ClassToDTOConverter.class);

	public static List<Icd10ClassDTO> toDTOList(List<Icd10Class> entities) {
		List<Icd10ClassDTO> dtos = new ArrayList<Icd10ClassDTO>();
		
		int srcsSize = entities.size();
		for (int i = 0; i < srcsSize; i++) {
			dtos.add(toDTO(entities.get(i)));
		}
		
		return dtos;
	}
	
	public static Icd10ClassDTO toDTO(Icd10Class entity) {
		Icd10ClassDTO dto = new Icd10ClassDTO();
		
	    dto.setCode(entity.getCode());
	    dto.setVersion(entity.getVersion());
	    dto.setClassKind(entity.getClassKind());
	    dto.setUsageKind(entity.getUsageKind());
	    dto.setSuperClass(entity.getSuperClass());
	    dto.setLabel(entity.getLabel());
	    dto.setRef(entity.getRef());
	    dto.setChildrenCount(entity.getChildrenCount());
//	    dto.setDescendantCount(entity.getDescendantCount());
	    dto.setPath(entity.getPath());
	 		
		return dto;
	}

	public Icd10ClassDTO fromClass(Icd10Class entity) {
	    // Class가 검색이 안될경
		  if ( entity != null) { //
		    Icd10ClassDTO dto = toDTO(entity);
	        return dto; //
		  } else return null; //
	  		
	}
/*
public class Icd10ClassToDTOConverter {

  Logger log = LoggerFactory.getLogger(Icd10ClassToDTOConverter.class);
  
  private static ModelMapper modelMapper ; 
  
  
  public Icd10ClassDTO fromClass(Icd10Class source) {	
      // Entity가 검색이 안될경
	  if ( source != null) { //
	    Icd10ClassDTO dto = createModelMapper().map(source, Icd10ClassDTO.class);
        return dto; //
	  } else return null; //
  }
  
  private ModelMapper createModelMapper() {
    PropertyMap<Icd10Class, Icd10ClassDTO> map = new PropertyMap<Icd10Class, Icd10ClassDTO>() {   	
    	
      @Override
      protected void configure() {
        // TODO Auto-generated method stub
        map().setCode(source.getCode());
        map().setVersion(source.getVersion());
        map().setClassKind(source.getClassKind());
        map().setClassUsage(source.getClassUsage());
        map().setChildrenCount(source.getChildrenCount);
        map().setDescendantCount(source.getDescendantCount);
        map().setPath(source.getPath());
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
*/
}
