package co.infoclinic.term.icd10.model.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.icd10.model.dto.PropertyDTO;
import co.infoclinic.term.icd10.model.dto.PropertyValueDTO;
import co.infoclinic.term.icd10.model.entity.Icd10Attribute;
import co.infoclinic.term.icd10.model.entity.Icd10Property;

@Component
public class PropertyMapper {
  
  static Logger log = LoggerFactory.getLogger(PropertyMapper.class);
  
  private final static String TYPE_MORTALITY = "mortality";
  private final static String TYPE_MORBIDITY = "morbidity";
  private final static String TYPE_LABEL = "label";
  
  private final static String NAME_PREFERRED = "preferred";
  private final static String NAME_INCLUSION = "inclusion";
  private final static String NAME_EXCLUSION = "exclusion";
  

  public static PropertyDTO mapEntitiesIntoDTOList(List<Icd10Property> entities, Icd10Attribute attribute) {
    if (entities == null || ((Collection<?>) entities).size() == 0) {
      return null;
    }
    
    PropertyDTO dto = new PropertyDTO();
    
    // set codeSystem and version
    String codeSystemAndVersion = entities.get(0).getId();
    String code = entities.get(0).getEntity();
    int underscoreIndex = codeSystemAndVersion.indexOf("_");
    String codeSystem = codeSystemAndVersion.substring(0, underscoreIndex);
    String version = codeSystemAndVersion.substring(underscoreIndex + 1, codeSystemAndVersion.length());
    dto.setCodeSystem(codeSystem);
    dto.setVersion(version);
    dto.setCode(code);
    
    // set attribute
    if (attribute != null) {
      dto.setAttribute(propertyValueSetter(attribute.getAttribute(), attribute.getValue()));
    }
    
    Icd10Property icd10Property = null;
    List<String> motalityList = new ArrayList<String>();
    List<String> mobidityList = new ArrayList<String>();
    List<PropertyValueDTO> inclusionList = new ArrayList<PropertyValueDTO>();
    List<PropertyValueDTO> exclusionList = new ArrayList<PropertyValueDTO>();
    int entityListSize = entities.size();
    for (int i = 0; i < entityListSize; i++) {
      icd10Property = entities.get(i);
      String property = icd10Property.getProperty();
      String type = icd10Property.getType();
      String value = icd10Property.getValue();
      
      if (type.equals(TYPE_MORTALITY)) {
        motalityList.add(property);
      } else if (type.equals(TYPE_MORBIDITY)) {
        mobidityList.add(property);
      } else if (type.equals(TYPE_LABEL)) {
        String name = icd10Property.getName();
        if (name.equals(NAME_PREFERRED)) {
          dto.setPreferredName(value);
        } else if (name.equals(NAME_INCLUSION)) {
          inclusionList.add(propertyValueSetter(property, value));
        } else if (name.equals(NAME_EXCLUSION)) {
          exclusionList.add(propertyValueSetter(property, value));
        } else {
          log.error("Undefined NAME : " + name);
        }
      } else {
        log.error("Undefined TYPE : " + type);
      }
    }
    
    if (!motalityList.isEmpty()) {
      dto.setMotalities(motalityList);
    }
    if (!mobidityList.isEmpty()) {
      dto.setMobidities(mobidityList);
    }
    if (!inclusionList.isEmpty()) {
      dto.setInclusions(inclusionList);
    }
    if (!exclusionList.isEmpty()) {
      dto.setExclusions(exclusionList);
    }
    
    return dto;
  }
  
  private static PropertyValueDTO propertyValueSetter(String property, String value) {
    PropertyValueDTO dto = new PropertyValueDTO();
    
    if (property != null) {
      dto.setProperty(property);
    }

    if (value != null) {
      dto.setValue(value);
    }
    
    return dto;
  }
}
