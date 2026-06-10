package co.infoclinic.term.icd10.model.converter;

//import org.modelmapper.ModelMapper;
//import org.modelmapper.PropertyMap;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.icd10.model.dto.Icd10ChildrenDTO;
import co.infoclinic.term.icd10.model.entity.Icd10Children;

@Component(value = "Icd10ChildrenCvt")
public final class Icd10ChildrenToDTOConverter {
	static final Logger log = LoggerFactory.getLogger(Icd10ChildrenToDTOConverter.class);

	public static List<Icd10ChildrenDTO> toDTOList(List<Icd10Children> Icd10ChildrenList) {
		List<Icd10ChildrenDTO> dtos = new ArrayList<Icd10ChildrenDTO>();
		
		int srcsSize = Icd10ChildrenList.size();
		for (int i = 0; i < srcsSize; i++) {
			dtos.add(toDTO(Icd10ChildrenList.get(i)));
		}
		
		return dtos;
	}
	
	public static Icd10ChildrenDTO toDTO(Icd10Children entity) {
		Icd10ChildrenDTO dto = new Icd10ChildrenDTO();
		
	    dto.setCode(entity.getCode());
	    dto.setVersion(entity.getVersion());
	    dto.setClassKind(entity.getClassKind());
	    dto.setUsageKind(entity.getUsageKind());
	    dto.setSuperClass(entity.getSuperClass());
	    dto.setChildrenCount(entity.getChildrenCount());
	    dto.setDescendantCount(entity.getDescendantCount());
	    dto.setPath(entity.getPath());
	    dto.setLabel(entity.getLabel());
	    dto.setRef(entity.getRef());
	 		
		return dto;
	}

	public List<Icd10ChildrenDTO> fromChildren(List<Icd10Children> Icd10ChildrenList) {
	    // Class가 검색이 안될경
		  if ( Icd10ChildrenList != null) { //
		    List<Icd10ChildrenDTO> dto = toDTOList(Icd10ChildrenList);
	        return dto; //
		  } else return null; //
	  		
	}

}
