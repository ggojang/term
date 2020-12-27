package co.infoclinic.term.snomedct.model.converter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import co.infoclinic.term.common.utils.SNOMEDCTComponentTypeEnum;
import co.infoclinic.term.common.utils.SNOMEDCTUtils;
import co.infoclinic.term.common.model.dto.Value;
import co.infoclinic.term.snomedct.model.dto.LanguageRefsetDTO;
import co.infoclinic.term.snomedct.model.dto.RefsetMemberDTO;
import co.infoclinic.term.snomedct.model.dto.RefsetMemberViewDTO;
import co.infoclinic.term.snomedct.model.entity.AbstractReferenceset;
import co.infoclinic.term.snomedct.model.entity.Description;
import co.infoclinic.term.snomedct.model.entity.Referenceset;
import co.infoclinic.term.snomedct.model.entity.UserReferenceset;
import co.infoclinic.term.snomedct.repository.RefsetRepository;
import co.infoclinic.term.snomedct.service.DescriptionService;
import co.infoclinic.term.snomedct.service.RefsetMemberQueryService;
import co.infoclinic.term.snomedct.service.RefsetMemberService;

@Component
public final class RefsetMemberConverter {

  Logger log = LoggerFactory.getLogger(RefsetMemberConverter.class);
  
  private static final String DESCRIPTOR_SCTID = "900000000000456007";

  private static DescriptionService descSvc;

  private static RefsetRepository referencesetRepository;
  
  private static RefsetMemberQueryService mbrSvc;

  @Autowired
  private DescriptionService tDescSvc;

  @Autowired
  private RefsetRepository tRefsetRepository;
  
  @Autowired
  private RefsetMemberQueryService tMbrSvc;

  @PostConstruct
  public void init() {
    RefsetMemberConverter.descSvc = tDescSvc;
    RefsetMemberConverter.referencesetRepository = tRefsetRepository;
    RefsetMemberConverter.mbrSvc = tMbrSvc;
  }

  public static List<RefsetMemberViewDTO> toViewDTOList(Iterable<? extends AbstractReferenceset> entities,
      String refsetTypeId) {
    List<RefsetMemberViewDTO> dtos = new ArrayList<RefsetMemberViewDTO>();

    Map<Integer, Pair<String, String>> extraFields = Maps.newHashMap();

    List<String> fieldIds = null;
    List<? extends AbstractReferenceset> fields = null;
    try {
      fields = referencesetRepository.findByRefsetIdAndReferencedComponentId("900000000000456007",
          Iterables.get(entities, 0).getRefsetId());

      fieldIds = new ArrayList<String>();
      for (AbstractReferenceset field : fields) {
        fieldIds.add(field.getField1());
        fieldIds.add(field.getField2());
      }

      // TODO client로 부터 languageCode, effectiveTime 지정받도록 할 것
      String languageCode = "en";
      Date now = new Date();
      SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
      String effectiveTime = format.format(now);
      
      List<Description> descriptionsByFieldIds = descSvc
          .getDescriptionListByConceptIdsAndLanguageCodeAndEffectiveTime(fieldIds, languageCode, effectiveTime);
      Map<String, String> termMap = new HashMap<String, String>();
      for (Description d : descriptionsByFieldIds) {
        termMap.put(d.getConceptId(), d.getTerm());
      }
      for (AbstractReferenceset field : fields) {
        String attributeDescription = termMap.get(field.getField1());
        attributeDescription = attributeDescription.substring(0, attributeDescription.lastIndexOf("(") - 1);
        String attributeType = termMap.get(field.getField2());
        String attributeOrder = field.getField3();
        
        String cpnt = null;
        // TODO ComponentType의 자식을 가져와서 포함되어있는지 확인하는 로직으로 변경하
        if (attributeType.toLowerCase().indexOf("component") != -1) {
          cpnt = field.getField2();
        }
        extraFields.put(Integer.valueOf(attributeOrder),
            new ImmutablePair<String, String>(attributeDescription, cpnt));
      }
    } catch (Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    for (AbstractReferenceset r : entities) {
      dtos.add(toViewDTO(r, extraFields, refsetTypeId));
    }
    return dtos;
  }

  public static RefsetMemberViewDTO toViewDTO(AbstractReferenceset entity, String refsetTypeId) {
    Map<Integer, Pair<String, String>> extraFields = Maps.newHashMap();
    List<String> fieldIds = null;
    List<? extends AbstractReferenceset> fields = null;
    try {
      fields = referencesetRepository.findByRefsetIdAndReferencedComponentId("900000000000456007",
          entity.getRefsetId());
      fieldIds = new ArrayList<String>();
      for (AbstractReferenceset field : fields) {
        fieldIds.add(field.getField1());
        fieldIds.add(field.getField2());
      }
      // TODO client로 부터 languageCode, effectiveTime 지정받도록 할 것
      String languageCode = "en";
      Date now = new Date();
      SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
      String nowDate = format.format(now);
      List<Description> descriptionsByFieldIds =
          descSvc.getDescriptionListByConceptIdsAndLanguageCodeAndEffectiveTime(fieldIds, languageCode, nowDate);

      Map<String, String> termMap = new HashMap<String, String>();
      for (Description d : descriptionsByFieldIds) {
        termMap.put(d.getConceptId(), d.getTerm());
      }

      for (AbstractReferenceset field : fields) {
        String attributeDescription = termMap.get(field.getField1());
        attributeDescription = attributeDescription.substring(0, attributeDescription.lastIndexOf("(") - 1);
        String attributeType = termMap.get(field.getField2());
        String attributeOrder = field.getField3();
        
        String cpnt = null;
        // TODO ComponentType의 자식을 가져와서 포함되어있는지 확인하는 로직으로 변경하
        if (attributeType.toLowerCase().indexOf("component") != -1) {
          cpnt = field.getField2();
        }
        extraFields.put(Integer.valueOf(attributeOrder),
            new ImmutablePair<String, String>(attributeDescription, cpnt));
      }
    } catch (Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    return toViewDTO(entity, extraFields, refsetTypeId);
  }

  private static RefsetMemberViewDTO toViewDTO(AbstractReferenceset entity,
      Map<Integer, Pair<String, String>> fields, String refsetTypeId) {

    RefsetMemberViewDTO dto = new RefsetMemberViewDTO();
    List<String> conceptFieldIds = new ArrayList<String>();
    List<String> descriptionFieldIds = new ArrayList<String>();

    conceptFieldIds.add(entity.getModuleId());
    conceptFieldIds.add(entity.getRefsetId());
    conceptFieldIds.add(refsetTypeId);

    addToComponentIdByType(conceptFieldIds, descriptionFieldIds, entity.getReferencedComponentId());

    if (fields != null && fields.size() > 0) {
    	if (entity.getField1() != null) {
	      if (fields.get(1).getValue() != null) {
	        addToComponentIdByType(conceptFieldIds, descriptionFieldIds, entity.getField1());
	      }
	    }
	    if (entity.getField2() != null) {
	      if (fields.get(2).getValue() != null) {
	        addToComponentIdByType(conceptFieldIds, descriptionFieldIds, entity.getField2());
	      }
	    }
	    if (entity.getField3() != null) {
	      if (fields.get(3).getValue() != null) {
	        addToComponentIdByType(conceptFieldIds, descriptionFieldIds, entity.getField3());
	      }
	    }
	    if (entity.getField4() != null) {
	      if (fields.get(4).getValue() != null) {
	        addToComponentIdByType(conceptFieldIds, descriptionFieldIds, entity.getField4());
	      }
	    }
	    if (entity.getField5() != null) {
	      if (fields.get(5).getValue() != null) {
	        addToComponentIdByType(conceptFieldIds, descriptionFieldIds, entity.getField5());
	      }
	    }
	    if (entity.getField6() != null) {
	      if (fields.get(6).getValue() != null) {
	        addToComponentIdByType(conceptFieldIds, descriptionFieldIds, entity.getField6());
	      }
	    }
	    if (entity.getField7() != null) {
	      if (fields.get(7).getValue() != null) {
	        addToComponentIdByType(conceptFieldIds, descriptionFieldIds, entity.getField7());
	      }
	    }
    }
    

    // TODO client로 부터 languageCode, effectiveTime 지정받도록 할 것
    String languageCode = "en";
    Date now = new Date();
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    String effectiveTime = format.format(now);
    
    // ConceptType Id들을 모아서 Description을 호출
    List<Description> descriptionsByConceptFieldIds =
        descSvc.getDescriptionListByConceptIdsAndLanguageCodeAndEffectiveTime(conceptFieldIds, languageCode, effectiveTime);

    Map<String, String> termMap = new HashMap<String, String>();
    for (Description d : descriptionsByConceptFieldIds) {
      termMap.put(d.getConceptId(), d.getTerm());
    }

    // DescriptionType Id들을 모아서 Description을 호출
    List<Description> descriptionsByDescriptionFieldIds = null;
    if (!descriptionFieldIds.isEmpty()) {
      descriptionsByDescriptionFieldIds = descSvc.getDescriptionListByDescriptionIdsAndLanguageCodeAndEffectiveTime(descriptionFieldIds, languageCode, effectiveTime);

      for (Description d : descriptionsByDescriptionFieldIds) {
        termMap.put(d.getDescriptionId(), d.getTerm());
      }
    }

    // Module
    String module = termMap.get(entity.getModuleId());
    Value moduleValue = createValueObject(entity.getModuleId(), module);

    // Refset
    String refset = termMap.get(entity.getRefsetId());
    Value refsetValue = createValueObject(entity.getRefsetId(), refset);

    // ReferencedComponent
    String refsetComponent = termMap.get(entity.getReferencedComponentId());
    Value refsetComponentValue =
        createValueObject(entity.getReferencedComponentId(), refsetComponent);

    // Type
    String type = termMap.get(refsetTypeId);
    Value typeValue = createValueObject(refsetTypeId, type);

    dto.setId(entity.getId());
    dto.setUuid(entity.getReferencesetId());
    dto.setEffectiveTime(entity.getEffectiveTime());
    dto.setActive(entity.isActive());
    dto.setModule(moduleValue);
    dto.setRefset(refsetValue);
    dto.setReferencedComponent(refsetComponentValue);
    dto.setType(typeValue);

    Map<String, Value> extra = Maps.newLinkedHashMap();

    if (entity.getField1() != null) {
      String name = null;
      if (fields.get(1).getValue() != null) {
        name = termMap.get(entity.getField1());
      }
      extra.put(fields.get(1).getKey(), createValueObject(entity.getField1(), name));
    }
    if (entity.getField2() != null) {
      String name = null;
      if (fields.get(2).getValue() != null) {
        name = termMap.get(entity.getField2());
      }
      extra.put(fields.get(2).getKey(), createValueObject(entity.getField2(), name));
    }
    if (entity.getField3() != null) {
      String name = null;
      if (fields.get(3).getValue() != null) {
        name = termMap.get(entity.getField3());
      }
      extra.put(fields.get(3).getKey(), createValueObject(entity.getField3(), name));
    }
    if (entity.getField4() != null) {
      String name = null;
      if (fields.get(4).getValue() != null) {
        name = termMap.get(entity.getField4());
      }
      extra.put(fields.get(4).getKey(), createValueObject(entity.getField4(), name));
    }
    if (entity.getField5() != null) {
      String name = null;
      if (fields.get(5).getValue() != null) {
        name = termMap.get(entity.getField5());
      }
      extra.put(fields.get(5).getKey(), createValueObject(entity.getField5(), name));
    }
    if (entity.getField6() != null) {
      String name = null;
      if (fields.get(6).getValue() != null) {
        name = termMap.get(entity.getField6());
      }
      extra.put(fields.get(6).getKey(), createValueObject(entity.getField6(), name));
    }
    if (entity.getField7() != null) {
      String name = null;
      if (fields.get(7).getValue() != null) {
        name = termMap.get(entity.getField7());
      }
      extra.put(fields.get(7).getKey(), createValueObject(entity.getField7(), name));
    }

    if (!extra.isEmpty())
      dto.setExtra(extra);


    return dto;
  }
  
  
  public static Page<RefsetMemberViewDTO> toViewDTOPage(Pageable pageRequest,
      Page<AbstractReferenceset> source, String refsetTypeId) {
    List<RefsetMemberViewDTO> dtos = toViewDTOList(source.getContent(), refsetTypeId);
    return new PageImpl<>(dtos, pageRequest, source.getTotalElements());
  }
  
  
  public static Referenceset toEntity(LanguageRefsetDTO dto) {
    if (dto == null) {
      return new Referenceset();
    }
    
    Referenceset entity = new Referenceset();
    
    entity.setReferencesetId(dto.getReferencesetId());
    entity.setId(dto.getId());
    entity.setActive(dto.isActive());
    entity.setModuleId(dto.getModuleId());
    entity.setEffectiveTime(dto.getEffectiveTime());
    entity.setRefsetId(dto.getRefsetId());
    entity.setReferencedComponentId(dto.getReferencedComponentId());
    entity.setField1(dto.getAcceptabilityId());
    
    return entity;
  }
  
  
  public static List<Referenceset> toEntityList(List<LanguageRefsetDTO> dtos) {
    if (dtos == null || dtos.isEmpty()) {
      return new ArrayList<Referenceset>();
    }
    
    List<Referenceset> entities = new ArrayList<Referenceset>();
    LanguageRefsetDTO dto = null;
    int dtosSize = dtos.size();
    for (int i = 0; i < dtosSize; i++) {
      dto = dtos.get(i);
      entities.add(toEntity(dto));
    }
    
    return entities;
  }
  
  
  public static LanguageRefsetDTO toLangDTO(Referenceset entity) {
    if (entity == null) {
      return new LanguageRefsetDTO();
    }
    
    LanguageRefsetDTO dto = new LanguageRefsetDTO();
    dto.setReferencesetId(entity.getReferencesetId());
    dto.setId(entity.getId());
    dto.setActive(entity.isActive());
    dto.setModuleId(entity.getModuleId());
    dto.setEffectiveTime(entity.getEffectiveTime());
    dto.setRefsetId(entity.getRefsetId());
    dto.setReferencedComponentId(entity.getReferencedComponentId());
    dto.setAcceptabilityId(entity.getField1());
    
    return dto;
  }
  
  
  public static List<LanguageRefsetDTO> toLangDTOList(List<Referenceset> entities) {
    if (entities == null || entities.isEmpty()) {
      return new ArrayList<LanguageRefsetDTO>();
    }
    
    List<LanguageRefsetDTO> dtos = new ArrayList<LanguageRefsetDTO>();
    Referenceset entity = null;
    int entitiesSize = entities.size();
    for (int i = 0; i < entitiesSize; i++) {
      entity = entities.get(i);
      dtos.add(toLangDTO(entity));
    }
    
    return dtos;
  }
  
  
  /**
	 * 
	 * @param member
	 * @param refsetTypeId
	 * @param effectiveTime
	 * @return
	 */
	public static RefsetMemberViewDTO toViewDTO(Referenceset member, String refsetTypeId,
			String effectiveTime) {
		Map<Integer, Pair<String, String>> extraFields = new HashMap<Integer, Pair<String, String>>();
		List<String> fieldIds = null;
		List<Referenceset> fields = null;
		try {
			fields = mbrSvc.getMemberEntityList(DESCRIPTOR_SCTID, member.getRefsetId(), effectiveTime);
			fieldIds = new ArrayList<String>();
			for (Referenceset field : fields) {
				fieldIds.add(field.getField1());
				fieldIds.add(field.getField2());
			}

			String languageCode = "en";
			List<Description> descriptionsByFieldIds = descSvc
					.getDescriptionListByConceptIdsAndLanguageCodeAndEffectiveTime(fieldIds, languageCode,
							effectiveTime);

			Map<String, String> termMap = new HashMap<String, String>();
			for (Description d : descriptionsByFieldIds) {
				termMap.put(d.getConceptId(), d.getTerm());
			}

			for (Referenceset field : fields) {
				String attributeDescription = termMap.get(field.getField1());
				attributeDescription = attributeDescription.substring(0, attributeDescription.lastIndexOf("(") - 1);
				String attributeType = termMap.get(field.getField2());
				String attributeOrder = field.getField3();

				String cpnt = null;
				// TODO ComponentType의 자식을 가져와서 포함되어있는지 확인하는 로직으로 변경하
				if (attributeType.toLowerCase().indexOf("component") != -1) {
					cpnt = field.getField2();
				}
				extraFields.put(Integer.valueOf(attributeOrder),
						new ImmutablePair<String, String>(attributeDescription, cpnt));
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return toViewDTO(member, extraFields, refsetTypeId, effectiveTime);
	}

	/**
	 * 
	 * @param entity
	 * @param fields
	 * @param refsetTypeId
	 * @param effectiveTime
	 * @return
	 */
	private static RefsetMemberViewDTO toViewDTO(Referenceset member, Map<Integer, Pair<String, String>> fields,
			String refsetTypeId, String effectiveTime) {

		RefsetMemberViewDTO dto = new RefsetMemberViewDTO();
		List<String> conceptFieldIds = new ArrayList<String>();
		List<String> descriptionFieldIds = new ArrayList<String>();

		conceptFieldIds.add(member.getModuleId());
		conceptFieldIds.add(member.getRefsetId());
		conceptFieldIds.add(refsetTypeId);

		addToComponentIdByType(conceptFieldIds, descriptionFieldIds, member.getReferencedComponentId());

		if (fields != null && fields.size() > 0) {
			if (member.getField1() != null) {
				if (fields.get(1).getValue() != null) {
					addToComponentIdByType(conceptFieldIds, descriptionFieldIds, member.getField1());
				}
			}
			if (member.getField2() != null) {
				if (fields.get(2).getValue() != null) {
					addToComponentIdByType(conceptFieldIds, descriptionFieldIds, member.getField2());
				}
			}
			if (member.getField3() != null) {
				if (fields.get(3).getValue() != null) {
					addToComponentIdByType(conceptFieldIds, descriptionFieldIds, member.getField3());
				}
			}
			if (member.getField4() != null) {
				if (fields.get(4).getValue() != null) {
					addToComponentIdByType(conceptFieldIds, descriptionFieldIds, member.getField4());
				}
			}
			if (member.getField5() != null) {
				if (fields.get(5).getValue() != null) {
					addToComponentIdByType(conceptFieldIds, descriptionFieldIds, member.getField5());
				}
			}
			if (member.getField6() != null) {
				if (fields.get(6).getValue() != null) {
					addToComponentIdByType(conceptFieldIds, descriptionFieldIds, member.getField6());
				}
			}
			if (member.getField7() != null) {
				if (fields.get(7).getValue() != null) {
					addToComponentIdByType(conceptFieldIds, descriptionFieldIds, member.getField7());
				}
			}
		}

		// TODO client로 부터 languageCode, effectiveTime 지정받도록 할 것
		String languageCode = "en";

		// ConceptType Id들을 모아서 Description을 호출
		List<Description> descriptionsByConceptFieldIds = descSvc
				.getDescriptionListByConceptIdsAndLanguageCodeAndEffectiveTime(conceptFieldIds, languageCode,
						effectiveTime);

		Map<String, String> termMap = new HashMap<String, String>();
		for (Description d : descriptionsByConceptFieldIds) {
			termMap.put(d.getConceptId(), d.getTerm());
		}

		// DescriptionType Id들을 모아서 Description을 호출
		List<Description> descriptionsByDescriptionFieldIds = null;
		if (!descriptionFieldIds.isEmpty()) {
			descriptionsByDescriptionFieldIds = descSvc
					.getDescriptionListByDescriptionIdsAndLanguageCodeAndEffectiveTime(descriptionFieldIds,
							languageCode, effectiveTime);

			for (Description d : descriptionsByDescriptionFieldIds) {
				termMap.put(d.getDescriptionId(), d.getTerm());
			}
		}

		// Module
		String module = termMap.get(member.getModuleId());
		Value moduleValue = createValueObject(member.getModuleId(), module);

		// Refset
		String refset = termMap.get(member.getRefsetId());
		Value refsetValue = createValueObject(member.getRefsetId(), refset);

		// ReferencedComponent
		String refsetComponent = termMap.get(member.getReferencedComponentId());
		Value refsetComponentValue = createValueObject(member.getReferencedComponentId(), refsetComponent);

		// Type
		String type = termMap.get(refsetTypeId);
		Value typeValue = createValueObject(refsetTypeId, type);

		dto.setId(member.getId());
		dto.setUuid(member.getReferencesetId());
		dto.setEffectiveTime(member.getEffectiveTime());
		dto.setActive(member.isActive());
		dto.setModule(moduleValue);
		dto.setRefset(refsetValue);
		dto.setReferencedComponent(refsetComponentValue);
		dto.setType(typeValue);

		Map<String, Value> extra = new HashMap<String, Value>();

		if (member.getField1() != null) {
			String name = null;
			if (fields.get(1).getValue() != null) {
				name = termMap.get(member.getField1());
			}
			extra.put(fields.get(1).getKey(), createValueObject(member.getField1(), name));
		}
		if (member.getField2() != null) {
			String name = null;
			if (fields.get(2).getValue() != null) {
				name = termMap.get(member.getField2());
			}
			extra.put(fields.get(2).getKey(), createValueObject(member.getField2(), name));
		}
		if (member.getField3() != null) {
			String name = null;
			if (fields.get(3).getValue() != null) {
				name = termMap.get(member.getField3());
			}
			extra.put(fields.get(3).getKey(), createValueObject(member.getField3(), name));
		}
		if (member.getField4() != null) {
			String name = null;
			if (fields.get(4).getValue() != null) {
				name = termMap.get(member.getField4());
			}
			extra.put(fields.get(4).getKey(), createValueObject(member.getField4(), name));
		}
		if (member.getField5() != null) {
			String name = null;
			if (fields.get(5).getValue() != null) {
				name = termMap.get(member.getField5());
			}
			extra.put(fields.get(5).getKey(), createValueObject(member.getField5(), name));
		}
		if (member.getField6() != null) {
			String name = null;
			if (fields.get(6).getValue() != null) {
				name = termMap.get(member.getField6());
			}
			extra.put(fields.get(6).getKey(), createValueObject(member.getField6(), name));
		}
		if (member.getField7() != null) {
			String name = null;
			if (fields.get(7).getValue() != null) {
				name = termMap.get(member.getField7());
			}
			extra.put(fields.get(7).getKey(), createValueObject(member.getField7(), name));
		}

		if (!extra.isEmpty())
			dto.setExtra(extra);

		return dto;
	}
  
  
  public static RefsetMemberDTO toDTO(UserReferenceset entity) {
    if (entity == null) {
      return new RefsetMemberDTO();
    }
    
    RefsetMemberDTO dto = new RefsetMemberDTO();
    
    dto.setId(entity.getId()); // SEQ
    dto.setUuid(entity.getReferencesetId());
    dto.setActive(entity.isActive());
    dto.setEffectiveTime(entity.getEffectiveTime());
    //dto.setReferencesetId();
    dto.setModuleId(entity.getModuleId());
    dto.setRefsetId(entity.getRefsetId());
    dto.setReferencedComponentId(entity.getReferencedComponentId());
    
    // TODO Extra Field Get/Get
    // refsetId의 Descriptor 정보를 가져와서 Extra Field 값 존재여부를 확인후 셋팅한다.
    
    return dto;
  }
  
  public static List<RefsetMemberDTO> toDTOList(List<UserReferenceset> entities) {
    if (entities == null || entities.isEmpty()) {
      return new ArrayList<RefsetMemberDTO>();
    }
    
    List<RefsetMemberDTO> dtos = new ArrayList<RefsetMemberDTO>();
    UserReferenceset entity = null;
    int entitiesSize = entities.size();
    for (int i = 0; i < entitiesSize; i++) {
      entity = entities.get(i);
      dtos.add(toDTO(entity));
    }
    
    return dtos;
  }
  
  public static UserReferenceset toUserEntity(RefsetMemberDTO dto, boolean isIncludeId) {
    if (dto == null) {
      return new UserReferenceset();
    }
    
    UserReferenceset entity = new UserReferenceset();
    
    Long id = dto.getId();
    String uuid = dto.getUuid();
    boolean active = dto.isActive();
    String effectiveTime = dto.getEffectiveTime();
    String moduleId = dto.getModuleId();
    String refsetId = dto.getRefsetId();
    String referencedComponentId = dto.getReferencedComponentId();
    Map<String, String> extraMap = dto.getExtra();
    
    
    if (id != null && isIncludeId) {
      entity.setId(id);
    }
    
    if (uuid != null) {
      entity.setReferencesetId(uuid);
    }
    
    entity.setActive(active);
    
    if (effectiveTime != null) {
      entity.setEffectiveTime(effectiveTime);
    }
    
    if (moduleId != null) {
      entity.setModuleId(moduleId);
    }
    
    if (referencedComponentId != null) {
      entity.setReferencedComponentId(referencedComponentId);
    }
    
    // refsetId가 널이면 필드목록을 가져올 수 없다.
    if (refsetId != null) {
      entity.setRefsetId(refsetId);
    }
    
    // 추가필드가 있다면 필드목록을 가져와야함.
    if (refsetId != null && (extraMap != null && extraMap.size() > 0)) {
      List<RefsetMemberViewDTO> fieldList = null;
      RefsetMemberViewDTO field = null;
      Value extraDescriptionValue = null;
      Value extraOrderValue = null;
      String fieldAttributeDescriptionName = "Attribute description";
      String fieldAttributeOrderName = "Attribute order";
      //String fieldAttributeTypeName = "Attribute type";
      int fieldSize = 3;
      //int fieldExtraOrderIndex = 2;
      try {
        fieldList =
            mbrSvc.getMemberList(SNOMEDCTUtils.MetadataType.Descriptor, refsetId);
        int fieldListSize = fieldList.size();
        
        if (fieldList != null && (!fieldList.isEmpty() && extraMap.size() == (fieldListSize - 1))) {
          for (int i = 0; i < fieldListSize; i++) {
            field = fieldList.get(i);
            if (field != null && field.getExtra().size() == fieldSize) {
              extraOrderValue = field.getExtra().get(fieldAttributeOrderName);
              extraDescriptionValue = field.getExtra().get(fieldAttributeDescriptionName);
              if (extraOrderValue != null && extraDescriptionValue != null) {
                int order = Integer.valueOf(extraOrderValue.getId());
                String description = extraDescriptionValue.getName();
                description = description.contains("(") ? description.substring(0, description.lastIndexOf("(") - 1) : description;
                if (order > 0) {
                  String fieldId = extraMap.get(description);
                  if (order == 1) {
                    entity.setField1(fieldId);
                  } else if (order == 2) {
                    entity.setField2(fieldId);
                  } else if (order == 3) {
                    entity.setField3(fieldId);
                  } else if (order == 4) {
                    entity.setField4(fieldId);
                  } else if (order == 5) {
                    entity.setField5(fieldId);
                  } else if (order == 6) {
                    entity.setField6(fieldId);
                  } else if (order == 7) {
                    entity.setField7(fieldId);
                  }
                }
              }
            }
          }
        }
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    return entity;
  }
  
  public static List<UserReferenceset> toUserEntityList(List<RefsetMemberDTO> dtos, boolean isIncludeId) {
    if (dtos == null | dtos.isEmpty()) {
      return new ArrayList<UserReferenceset>();
    }
    
    List<UserReferenceset> entities = new ArrayList<UserReferenceset>();
    RefsetMemberDTO dto = null;
    int dtosSize = dtos.size();
    for (int i = 0; i < dtosSize; i++) {
      dto = dtos.get(i);
      entities.add(toUserEntity(dto, isIncludeId));
    }
    
    return entities;
  }
  
  

  private static void addToComponentIdByType(List<String> conceptFieldIds,
      List<String> descriptionFieldIds, String componentId) {
    boolean isConcept =
        SNOMEDCTComponentTypeEnum.getById(componentId).equals(SNOMEDCTComponentTypeEnum.CONCEPT);

    if (isConcept) {
      conceptFieldIds.add(componentId);
    } else {
      descriptionFieldIds.add(componentId);
    }
  }

  
  private static Value createValueObject(String id, String name) {
    return new Value(id, name);
  }

}
