package co.infoclinic.term.icd10.model.converter;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

//import co.infoclinic.term.icd10.model.entity.Icd10Class;
import co.infoclinic.term.icd10.model.entity.Icd10Rubric;
import co.infoclinic.term.icd10.model.dto.Icd10RubricDTO;
import co.infoclinic.term.icd10.model.dto.Icd10RubricKindDTO;

@Component
public class Icd10RubricMapper {
  
  static Logger log = LoggerFactory.getLogger(Icd10RubricMapper.class);
  
  //private final static String TYPE_MORTALITY = "mortality";
  //private final static String TYPE_MORBIDITY = "morbidity";
  //private final static String TYPE_LABEL = "label";
  
  //private final static String KIND_PREFERRED = "preferred";
  //private final static String KIND_INCLUSION = "inclusion";
  //private final static String KIND_EXCLUSION = "exclusion";
  //private final static String KIND_CODING_HINT = "coding-hint";
  //private final static String KIND_NOTE = "note";
  //private final static String KIND_TEXT = "text";
  
  //private final static String TYPE_LIST = "list";
  //private final static String TYPE_ITEM = "item";
  //private final static String TYPE_PARA = "para";
//  private final static String TYPE_LIST = "listitem";
  
  //private final static String REF = "ref";
  
  public static Icd10RubricDTO mapClassesIntoDTOList(List<Icd10Rubric> entities) {
    if (entities == null || ((Collection<?>) entities).size() == 0) {
      return null;
    }
    
    Icd10RubricDTO dto = new Icd10RubricDTO();
//    Icd10RubricKindDTO kindDto = new Icd10RubricKindDTO();
//    Icd10RubricTypeDTO typeDto = new Icd10RubricTypeDTO();   
    
//    List<String> kindList = new ArrayList<String>();
//    List<String> labelList = new ArrayList<String>();
    
    // set codeSystem and version
    String codeSystem = "ICD10";
//    String version = "2016";
    String version = entities.get(0).getVersion();
//    String code = entities.get(0).getCode();
    dto.setCodeSystem(codeSystem);
    dto.setVersion(version);
        
    //Icd10Class icd10Class = null;
    Icd10Rubric icd10Rubric = null;
//    List<String> motalityList = new ArrayList<String>();
//    List<String> mobidityList = new ArrayList<String>();
//    List<Icd10RubricKindDTO> inclusionList = new ArrayList<Icd10RubricKindDTO>();
//    List<Icd10RubricKindDTO> exclusionList = new ArrayList<Icd10RubricKindDTO>();
//    List<Icd10RubricKindDTO> noteList = new ArrayList<Icd10RubricKindDTO>();
    List<Icd10RubricKindDTO> kindList = new ArrayList<Icd10RubricKindDTO>();

    int classListSize = entities.size();
    
    for (int i = 0; i < classListSize; i++) {
      icd10Rubric = entities.get(i);
      String code = icd10Rubric.getCode();
//      String usageKind = null;
//      if ( code == icd10Class.getCode()) {
//    	  usageKind = icd10Class.getUsageKind();
//      }  
      //String id = icd10Rubric.getId();
      String kind = icd10Rubric.getKind();
      String modCode = icd10Rubric.getModifierCode();
      String usage = icd10Rubric.getUsageKind();
      String lang = icd10Rubric.getLang();
      String ftype = icd10Rubric.getFragmentType();
      String paraType = icd10Rubric.getParaType();      
      String label = icd10Rubric.getLabel();
      String ref = icd10Rubric.getRef();
      
//      if (type.equals(TYPE_MORTALITY)) {
//        motalityList.add(property);
//      } else if (type.equals(TYPE_MORBIDITY)) {
//        mobidityList.add(property);
//      } else 

	  dto.setCode(code);
	  if (usage != null) { 
		  dto.setUsageKind(usage);
	  }
//	  dto.setKind(kind);
//	  dto.setFragmentType(ftype);
//	  dto.setLang(lang);
//	  dto.setLabel(label);
//	  if (ref != null) {
//		  dto.setRef(ref);
//	  }
	  if (ftype == null) ftype="";
	  if (ref == null) ref="";
	  if (label == null) label="";
	  
//      if (kind.equals(KIND_PREFERRED) || kind.equals(KIND_CODING_HINT) || kind.equals(KIND_NOTE) || kind.equals(KIND_TEXT)) {
      if (kind != null) {
	  	  kindList.add(Icd10RubricKindSetter(kind, modCode, ftype, paraType, lang, label, ref));
//    	  dto.setKind(kindList);
//    	  dto.setUsageKind(usage);
//    	  labelList.add(label);
//    	  dto.setLabel(labelList);
//    	  if (ref != null) {
//    		  dto.setRef(ref);
//    	  }
//      } else if (kind.equals(KIND_CODING_HINT)) {
//    	  dto.setKind(kind);
//    	  dto.setLabel(label);
//    	  if (ref != null) {
//    		  dto.setRef(ref);
//    	  }
//      } else if (kind.equals(KIND_INCLUSION)) {
//    	  if (ftype.equals(TYPE_LIST) || ftype.equals(TYPE_ITEM) || "") {
//    		  inclusionList.add(Icd10RubricKindSetter(ftype, label, ref));
//    	  }
//    	  if (ref.equals(REF)) {
//    		  dto.setRef(ref);
//    	  }
//      } else if (kind.equals(KIND_EXCLUSION)) {
//    	  if (ftype.equals(TYPE_LIST) || ftype.equals(TYPE_ITEM) || "":) {
//    		  exclusionList.add(Icd10RubricKindSetter(ftype, label, ref)); 
//    	  }
//    	  if (ref.equlas(REF)) {
//    		  dto.setRef(ref);
//    	  }
//      } else if (kind.equals(KIND_NOTE) ) {
//    	  	noteList.add(Icd10RubricKindSetter(ftype, label, ref));	
      } else {
    	  log.error("Undefined Kind : " + kind);
      }
//    }
    
//    if (!motalityList.isEmpty()) {
//      dto.setMotalities(motalityList);
//    }
//    if (!mobidityList.isEmpty()) {
//      dto.setMobidities(mobidityList);
//    }
  
/*    if (!inclusionList.isEmpty()) {
    	dto.setInclusions(inclusionList);
      }
      if (!exclusionList.isEmpty()) {
    	dto.setExclusions(exclusionList);
      }
*/
      if (!kindList.isEmpty()) {
    	dto.setKinds(kindList);  
      }
    }
      return dto;
  }

  private static Icd10RubricKindDTO Icd10RubricKindSetter(String kind, String modCode, String ftype, String paraType, String lang, String label, String ref) {
  		//Icd10RubricKindDTO dtoKind = new Icd10RubricKindDTO();
  		Icd10RubricKindDTO dtoKind = new Icd10RubricKindDTO();
	    
	    //if (ftype != null) {
	    //	ftypeList.add(Icd10RubricTypeSetter(ftype, label, ref));
	    //}
  		dtoKind.setKind(kind);
  		dtoKind.setModCode(modCode);
  		dtoKind.setFtype(ftype);
  		dtoKind.setParaType(paraType);
  		dtoKind.setLang(lang);
  		dtoKind.setLabel(label);
  		dtoKind.setRef(ref);
	   
//	    dtoKind.setLabel(label);
	    		
//	    if (ref != null) {
//	    	dtoKind.setRef(ref);
//	    }
	    	
//	    if (!ftypeList.isEmpty()) {
//	    	dtoKind.setFtypes(ftypeList);
//	    }
	    
	    return dtoKind;
  }
/*
  	private static Icd10RubricTypeDTO Icd10RubricTypeSetter(String ftype, String label, String ref) {

  		Icd10RubricTypeDTO dtoType = new Icd10RubricTypeDTO();
 	    
  		dtoType.setFtype(ftype);
  		dtoType.setLabel(label);
	    		
  		if (ref != null) {
  			dtoType.setRef(ref);
  		}
  		return dtoType;
 	}
*/
  
} 
