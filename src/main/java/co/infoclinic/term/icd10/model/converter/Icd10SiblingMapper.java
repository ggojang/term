package co.infoclinic.term.icd10.model.converter;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.icd10.model.entity.Icd10Class;
import co.infoclinic.term.icd10.model.entity.Icd10Rubric;
import co.infoclinic.term.icd10.model.dto.Icd10RubricDTO;
import co.infoclinic.term.icd10.model.dto.Icd10RubricKindDTO;
import co.infoclinic.term.icd10.model.entity.Icd10Sibling;
import co.infoclinic.term.icd10.model.dto.Icd10SiblingsDTO;

@Component
public class Icd10SiblingMapper {
	
	  static Logger log = LoggerFactory.getLogger(Icd10SiblingMapper.class);

  
  public static Icd10SiblingsDTO mapSiblingIntoDTOMap(ArrayList<Icd10Sibling> entities) {
	    if (entities == null || ((Collection<?>) entities).size() == 0) {
	      return null;
	    }
	    
	  	Icd10RubricDTO dto = new Icd10RubricDTO();
	  	ArrayList<Icd10RubricDTO> dtos = new ArrayList<Icd10RubricDTO>();

	  	Icd10SiblingsDTO dtoss = new Icd10SiblingsDTO();
	    Icd10Sibling icd10Sibling = new Icd10Sibling();
	    
	    Icd10RubricKindDTO k = new Icd10RubricKindDTO();
	    List<Icd10RubricKindDTO> kindList = new ArrayList<Icd10RubricKindDTO>();

	    String codeSystem = "ICD10";
	    String version = entities.get(0).getVersion();
	    
	    for (int i = 0; i < entities.size(); i++) {
	    	//for (int j=0; j < entities.get(i).size(); j++) { 
	  			if ( (i != 0) && !entities.get(i-1).getCode().equals(entities.get(i).getCode()) ) {
	  				//log.info(i-1 +" : " + entities.get(i-1).getCode() + " " + i + " : " + entities.get(i).getCode());
	  				dto.setKinds(kindList);  
	  				kindList = new ArrayList<Icd10RubricKindDTO>();	
	  		    	dtos.add(dto);
	  		    	dto = new Icd10RubricDTO();
	  			}
	    		
		    	dto.setCodeSystem(codeSystem);
			    dto.setVersion(version);

		  	    dto.setCode(entities.get(i).getCode());
		  	    
		  		if (!entities.get(i).getUsageKind().isEmpty()) { 
		  			  dto.setUsageKind(entities.get(i).getUsageKind());
		  		}

		  		
		  	    if (!entities.get(i).getKind().isEmpty()) {
		  	    	
		  	    	k.setKind(entities.get(i).getKind());
		  	    	k.setModCode(entities.get(i).getModifierCode());	
		  	    	k.setId(entities.get(i).getId());
		  	    	k.setFtype(entities.get(i).getFragmentType());
		  	    	k.setParaType(entities.get(i).getParaType());
		  	    	k.setLang(entities.get(i).getLang());
		  	    	k.setLabel(entities.get(i).getLabel());
		  	    	k.setRef(entities.get(i).getRef());
		  	    	
		  	    	//log.info("k : " + k);
		  	    	kindList.add(k);
		  	    	k = new Icd10RubricKindDTO();
		  	    	
		  	    }
		  	    
	    	//}
	    }
		dto.setKinds(kindList);  
	    dtos.add(dto);
	    
	    dtoss.setSiblings(dtos);
	    
	    /*
	    Set<String> key = new LinkedHashSet<String>();
	    key = entities.keySet();
	    
	    //for(int i=0; i < key.length(); i++) {
	    for (String k : key) {
			dto.setCode(k);
			dto.setSiblings(entities.get(k)); 
			dtoS.add(dto);  
		}
	    */
	    
	    return dtoss; 
  } 
}