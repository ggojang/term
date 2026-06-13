package co.infoclinic.term.icd10.model.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.icd10.model.entity.Icd10Class;
import co.infoclinic.term.icd10.model.entity.Icd10Ancestor;
import co.infoclinic.term.icd10.model.dto.Icd10AncestorDTO;
import co.infoclinic.term.icd10.model.dto.Icd10RubricKindDTO;

@Component(value = "Icd10AncestorCvt")
public final class Icd10AncestorToDTOConverter {
  
	static Logger log = LoggerFactory.getLogger(Icd10AncestorToDTOConverter.class);

	public static List<Icd10AncestorDTO> toDTOList(List<Icd10Ancestor> Icd10Ancestors) {
		List<Icd10AncestorDTO> dtos = new ArrayList<Icd10AncestorDTO>();
		
		int srcsSize = Icd10Ancestors.size();
		for (int i = 0; i < srcsSize; i++) {
			dtos.add(toDTO(Icd10Ancestors.get(i)));
		}
		
		return dtos;
	}
  
	public static Icd10AncestorDTO toDTO(Icd10Ancestor entity) {
		Icd10AncestorDTO dto = new Icd10AncestorDTO();
		//List<Icd10RubricKindDTO> dtoKinds = new ArrayList<Icd10RubricKindDTO>();

		//dto.setCodeSystem("ICD10");
	    dto.setCode(entity.getCode());
	    dto.setVersion(entity.getVersion());
	    dto.setClassKind(entity.getClassKind());
		//String classKind = entity.getClassKind();
	    //String kind = entity.getKind();
	    dto.setUsageKind(entity.getUsageKind());
	    dto.setSuperClass(entity.getSuperClass());
	    //String usage = entity.getUsageKind();
	    //String superClass = entity.getSuperClass();
	    //String lang = entity.getLang();
	    //String ftype = entity.getFragmentType();
	    //String paraType = entity.getParaType();      
	    //String label = entity.getLabel();
	    //String ref = entity.getRef();
	    dto.setLabel(entity.getLabel());
	    dto.setRef(entity.getRef());
	    dto.setChildrenCount(entity.getChildrenCount());
	    dto.setDescendantCount(entity.getDescendantCount());
	    dto.setPath(entity.getPath());
	    dto.setKoreanLabel(entity.getKoreanLabel());
	    dto.setIsKcdExt(entity.getIsKcdExt());
	    //int childrenCount = entity.getChildrenCount();
	    //int descendantCount = entity.getDescendantCount();
	    //String path = entity.getPath();
	    

	    /*
	    if (usage != null ) {
		    dto.setUsageKind(usage);	    	
	    }

		if (ftype == null) ftype="";
		if (ref == null) ref="";
		if (label == null) label="";


	    if (kind != null) {
  		  dtoKinds.add(Icd10RubricKindSetter(kind, id, ftype, paraType, lang, label, ref));
    	} else {
    		log.error("Undefined Kind : " + kind);
    	}
	    
	    if (!dtoKinds.isEmpty()) {
    		dto.setKinds(dtoKinds);
    	}
	    */
		return dto;
	}

  public List<Icd10AncestorDTO> fromAncestor(List<Icd10Ancestor> Icd10Ancestors) {
	    // Class가 검색이 안될경
		  if ( Icd10Ancestors != null) { //
		    List<Icd10AncestorDTO> dto = toDTOList(Icd10Ancestors);
	        return dto; //
		  } else return null; //
	  		
	}
  /*
  private static Icd10RubricKindDTO Icd10RubricKindSetter(String kind, String id, String ftype, String paraType, String lang, String label, String ref) {
  		Icd10RubricKindDTO dtoKind = new Icd10RubricKindDTO();
	    
  		dtoKind.setKind(kind);
  		dtoKind.setId(id);
  		dtoKind.setFtype(ftype);
  		dtoKind.setParaType(paraType);
  		dtoKind.setLang(lang);
  		dtoKind.setLabel(label);
  		dtoKind.setRef(ref);
	    
	    return dtoKind;
  }  
  */
} 
