package co.infoclinic.term.snomedct.service.impl;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.model.dto.SubsumptionTestDTO;
import co.infoclinic.term.common.utils.SNOMEDCTComponentTypeEnum;
import co.infoclinic.term.common.utils.SNOMEDCTUtils;
import co.infoclinic.term.snomedct.model.converter.ConceptConverter;
import co.infoclinic.term.snomedct.model.dto.ConceptViewDTO;
import co.infoclinic.term.snomedct.model.dto.ConceptTreeDTO;
import co.infoclinic.term.snomedct.model.dto.RelationshipViewDTO;
import co.infoclinic.term.snomedct.model.entity.Concept;
import co.infoclinic.term.snomedct.repository.ConceptRepository;
import co.infoclinic.term.snomedct.repository.LatestRefsetMemberRepository;
import co.infoclinic.term.snomedct.service.ConceptService;
import co.infoclinic.term.snomedct.service.RelationshipService;
import co.infoclinic.term.snomedct.service.TransitiveClosureService;

/**
 * SNOMED CT Concept Service
 */
@Service("SCTEntitySvc")
public class ConceptServiceImpl implements ConceptService {

	/** Logger */
	Logger log = LoggerFactory.getLogger(ConceptServiceImpl.class);

	/** DI: Concept repository */
	@Autowired
	private ConceptRepository conceptRepository;
	/** DI: Relationship service */
	@Autowired
	private RelationshipService relationshipService;
	/** DI: TransitiveClosure service */
	@Autowired
	private TransitiveClosureService tcSvc;

	@Autowired
	private LatestRefsetMemberRepository latestRefsetMemberRepo;
	

	// ---------------------------------------- 
	// Public mehtods
	// ----------------------------------------
	
	/// ---------------------------------------- 
	/// 조회
	/// ----------------------------------------

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.ConceptService#getConcept(java.lang.String, java.lang.String)
	 */
	@Override
	public ConceptViewDTO getConcept(String conceptId, String effectiveTime) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ConceptViewDTO();
		}
		
		ConceptViewDTO dto = conceptRepository.findByConceptIdAndEffectiveTime(conceptId, effectiveTime);

		return dto;
	}

	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.ConceptService#getConcept(java.lang.String, java.lang.String)
	 */
	@Override
	public ConceptTreeDTO getConcept2(String conceptId, String effectiveTime) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ConceptTreeDTO();
		}
		
		ConceptTreeDTO dto = conceptRepository.findByConceptIdAndEffectiveTime2(conceptId, effectiveTime);

		return dto;
	}
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.ConceptService#getConceptList(java.lang.String, java.lang.String)
	 */
	@Override
	public List<ConceptViewDTO> getConceptList(String conceptId, String effectiveTime) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ArrayList<ConceptViewDTO>();
		}

		List<Concept> concepts = conceptRepository.findConceptListByConceptIdAndEffectiveTime(conceptId, effectiveTime);
		return convertToViewDTOList(concepts, "en", effectiveTime);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.ConceptService#getConceptList(java.util.Map, java.lang.String)
	 */
	@Override
	public List<ConceptViewDTO> getConceptList(Map<String, Object> attrValMap, String effectiveTime) {
		List<Concept> entities = conceptRepository.findByAttrVals(attrValMap, effectiveTime);
		return convertToViewDTOList(entities, "en", effectiveTime);
	}
	

	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.ConceptService#getChildren(java.lang.String, java.lang.String)
	 */
	@Override
	public List<ConceptViewDTO> getChildren(String conceptId, String effectiveTime) {
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ArrayList<ConceptViewDTO>();
		}
		return conceptRepository.findChildrenByConceptIdAndEffectiveTime(conceptId, effectiveTime);
	}

	@Override
	public List<ConceptViewDTO> getChildrenOrSelf(String conceptId, String effectiveTime) {
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ArrayList<ConceptViewDTO>();
		}
		List<ConceptViewDTO> result = new ArrayList<>(conceptRepository.findChildrenByConceptIdAndEffectiveTime(conceptId, effectiveTime));
		ConceptViewDTO self = conceptRepository.findByConceptIdAndEffectiveTime(conceptId, effectiveTime);
		if (self != null) result.add(0, self);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.ConceptService#getParentList(java.lang.String, java.lang.String)
	 */
	@Override
	public List<ConceptViewDTO> getParentList(String conceptId, String effectiveTime) {
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ArrayList<ConceptViewDTO>();
		}
		return conceptRepository.findParentListByConceptIdAndEffectiveTime(conceptId, effectiveTime);
	}

	@Override
	public List<ConceptViewDTO> getParentListOrSelf(String conceptId, String effectiveTime) {
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ArrayList<ConceptViewDTO>();
		}
		List<ConceptViewDTO> result = new ArrayList<>(conceptRepository.findParentListByConceptIdAndEffectiveTime(conceptId, effectiveTime));
		ConceptViewDTO self = conceptRepository.findByConceptIdAndEffectiveTime(conceptId, effectiveTime);
		if (self != null) result.add(0, self);
		return result;
	}

	@Override
	public List<ConceptViewDTO> getMemberOfList(String refsetId, String effectiveTime) {
		List<String> memberIds = latestRefsetMemberRepo.findMemberIdsByRefsetIdAndEffectiveTime(refsetId, effectiveTime);
		if (memberIds == null || memberIds.isEmpty()) return new ArrayList<>();
		List<ConceptViewDTO> result = new ArrayList<>();
		for (String id : memberIds) {
			ConceptViewDTO dto = conceptRepository.findByConceptIdAndEffectiveTime(id, effectiveTime);
			if (dto != null) result.add(dto);
		}
		return result;
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.ConceptService#getAncestorList(java.lang.String, java.lang.String)
	 */
	@Override
	public Page<ConceptViewDTO> getAncestorList(String conceptId, String effectiveTime, int page, int size) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId) || page < 1 || size < 1) {
			return new PageImpl<ConceptViewDTO>(new ArrayList<ConceptViewDTO>());
		}
				
		return getAncestorList(conceptId, effectiveTime, page, size, false);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.ConceptService#getAncestorListOrSelf(java.lang.String, java.lang.String)
	 */
	@Override
	public Page<ConceptViewDTO> getAncestorListOrSelf(String conceptId, String effectiveTime, int page, int size) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId) || page < 1 || size < 1) {
			return new PageImpl<ConceptViewDTO>(new ArrayList<ConceptViewDTO>());
		}
				
		return getAncestorList(conceptId, effectiveTime, page, size, true);
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.ConceptService#getDescendantList(java.lang.String, java.lang.String)
	 */
	@Override
	public Page<ConceptViewDTO> getDescendantList(String conceptId, String effectiveTime, int page, int size) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId) || page < 1 || size < 1) {
			return new PageImpl<ConceptViewDTO>(new ArrayList<ConceptViewDTO>());
		}
				
		return getDescendantList(conceptId, effectiveTime, page, size, false);
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.ConceptService#getDescendantListOrSelf(java.lang.String, java.lang.String)
	 */
	@Override
	public Page<ConceptViewDTO> getDescendantListOrSelf(String conceptId, String effectiveTime, int page, int size) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId) || page < 1 || size < 1) {
			return new PageImpl<ConceptViewDTO>(new ArrayList<ConceptViewDTO>());
		}
				
		return getDescendantList(conceptId, effectiveTime, page, size, true);
	}
	
	@Override
	public List<ConceptViewDTO> getAllDescendantList(String conceptId, String effectiveTime) {
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) return new ArrayList<>();
		List<String> paths = getPathList(conceptId);
		if (CollectionUtils.isEmpty(paths)) return new ArrayList<>();
		return conceptRepository.findAllDescendantListByPathsAndEffectiveTime(paths, effectiveTime);
	}

	@Override
	public List<ConceptViewDTO> getAllDescendantListOrSelf(String conceptId, String effectiveTime) {
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) return new ArrayList<>();
		List<ConceptViewDTO> result = new ArrayList<>();
		ConceptViewDTO self = conceptRepository.findByConceptIdAndEffectiveTime(conceptId, effectiveTime);
		if (self != null) result.add(self);
		List<String> paths = getPathList(conceptId);
		if (!CollectionUtils.isEmpty(paths)) {
			result.addAll(conceptRepository.findAllDescendantListByPathsAndEffectiveTime(paths, effectiveTime));
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.ConceptService#getDescendantTree(java.lang.String, java.lang.String)
	 */
	@Override
	public ConceptTreeDTO getDescendantTree(String conceptId, String effectiveTime) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ConceptTreeDTO();
		}
				
		return getDescendantTree(conceptId, effectiveTime, true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.ConceptService#subsumptionTest(java.lang.String, java.lang.String)
	 */
	@Override
	public SubsumptionTestDTO subsumptionTest(String criteriaId, String conceptId) {
		SubsumptionTestDTO dto = new SubsumptionTestDTO();
		dto.setCriteriaId(criteriaId);
		dto.setConceptId(conceptId);
		
		if (SNOMEDCTComponentTypeEnum.isValidIdentifier(criteriaId) && SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			// 포함여부 조회
			boolean isSubsumes = tcSvc.isSubsumption(criteriaId, conceptId) > 0 ? true:false;			
			dto.setCriteriaId(criteriaId);
			dto.setConceptId(conceptId);
			dto.setResult(isSubsumes);
		}
		
		return dto;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.ConceptService#getPostExpr(java.lang.String, java.lang.String)
	 */
	@Override
	public String getPostExpr(String code, String effectiveTime) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(code)) {
			return "";
		}
		
		ConceptViewDTO cnpt = getConcept(code, effectiveTime);
		
		if (cnpt.getConceptId() == null || cnpt.getDefinitionStatus() == null || cnpt.getModule() == null) {
			return "";
		}
		
		String expr = "";
		String cnptExpr;
		String relsExpr;
		
		// Definition Status: EquivalentTo
		String equivalSymbol = "===";
		// Definition Status: SubtypeOf
		String subtypeSymbol = "<<<";
		String cDefStat = cnpt.getDefinitionStatus().getId();
		
		List<RelationshipViewDTO> rels = relationshipService.getRelationshipList(code, effectiveTime, false);
		//int relsSize = rels.size();
		
		
		// Entity의 DefinitionStatus가
		//   Fully Defined(900000000000073002)라면 EquivalentTo
		//   Primitive(900000000000074008)라면 SubtypeOf
		cnptExpr = SNOMEDCTUtils.DefinitionStatus.Defined.equals(cDefStat) ? equivalSymbol:subtypeSymbol;

		cnptExpr += " ";
		
		// Conditional
		//cnptExpr = " |" + cTerm + "|";
		
		String relType;
		String relTerm; // 20180727 
		boolean relAtv = false;
		boolean firstPrnt = true;
		int group = 0;
		ConceptViewDTO relDest;
		Map<Integer, List<String>> relGrpAttrsMap = new TreeMap<Integer, List<String>>();
		List<String> attrs;
		for (RelationshipViewDTO rel : rels) {
			relAtv = rel.isActive();
			relType = rel.getType().getConceptId();
			relTerm = rel.getType().getTerm(); // 20180727
			relDest = rel.getDestination();
			
			// active
			if (relAtv) {
				// isA
				if (SNOMEDCTUtils.PrimaryId.IsA.equals(relType)) {
					if (!firstPrnt) {
						cnptExpr += " + ";
					}
					
					// format: conceptId |fsn|
					cnptExpr += relDest.getConceptId() + " |" + relDest.getTerm() + "|";
					firstPrnt = false;
				}
				// other
				else {
					group = Integer.parseInt(rel.getRelationshipGroup(), 10);
					if (!relGrpAttrsMap.containsKey(group)) {
						attrs = new ArrayList<String>();
						relGrpAttrsMap.put(group, attrs);
					} else {
						attrs = relGrpAttrsMap.get(group);
					}
					
					//20180727
					attrs.add(relType + " |" + relTerm + "| = " + relDest.getConceptId() + " | " + relDest.getTerm()  + "|");	
					//attrs.add(relType + " |" + relDest.getTerm() + "| = " + relDest.getConceptId() + " | " + relDest.getTerm()  + "|");		
				}
			}
		}
		
		// init relationship expr
		relsExpr = "";
		
		List<String> groupAttrs;
		int attrsSize = 0;
		Iterator<Integer> groupItr = relGrpAttrsMap.keySet().iterator();
		while(groupItr.hasNext()) {
			group = groupItr.next();
			groupAttrs = relGrpAttrsMap.get(group);
			
			if (group > 0) { relsExpr += "{"; }
			
			attrsSize = groupAttrs.size();
			for (int i = 0; i < attrsSize; i++) {
				relsExpr += groupAttrs.get(i);
				if (i != attrsSize-1) { relsExpr += ", "; }
			}
			
			if (group > 0) { relsExpr += "}"; }
			
			relsExpr += " ";
		}
		
		expr = cnptExpr + (!StringUtils.isEmpty(relsExpr) ? (" : " + relsExpr):"");
		
		return expr;
	}


	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------

	/// ---------------------------------------- 
	/// 공통
	/// ----------------------------------------
	
	/**
	 * Concept Entity 목록을 ViewDTO 목록으로 변환
	 * 
	 * @param entities
	 * @param languageCode
	 * @param effectiveTime
	 * @return
	 */
	private List<ConceptViewDTO> convertToViewDTOList(List<Concept> entities, String languageCode,
			String effectiveTime) {
		return ConceptConverter.toViewDTOList(entities, languageCode, effectiveTime);
	}
	

	/// ---------------------------------------- 
	/// 조회
	/// ----------------------------------------
	
	/**
	 * Entity ID의 경로 목록 조회
	 * 
	 * @param conceptId
	 * @return
	 */
	private List<String> getPathList(String conceptId) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ArrayList<String>();
		}
				
		List<String> paths;
		
		if (!SNOMEDCTUtils.PrimaryId.SnomedCTConcept.equals(conceptId)) {
			paths = tcSvc.getPathListByConceptId(conceptId);
		} else {
			paths = new ArrayList<String>();
			paths.add(conceptId);
		}
		
		return paths;
	}
	
	
	/**
	 * Entity ID의 부모 경로 록록 조회
	 * 
	 * @param conceptId
	 * @return
	 */
	private List<String> getParentPathList(String conceptId) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ArrayList<String>();
		}
				
		List<String> paths;
		
		if (!SNOMEDCTUtils.PrimaryId.SnomedCTConcept.equals(conceptId)) {
			paths = tcSvc.getParentPathListByConceptId(conceptId);
		} else {
			paths = new ArrayList<String>();
		}
		
		return paths;
	}
	
	
	/**
	 * 경로 목록으로 부터 중복을 제거한 ID 목록 반환
	 * 
	 * @param paths
	 * @return
	 */
	private List<String> getIdList(List<String> paths) {
		if (CollectionUtils.isEmpty(paths)) {
			return new ArrayList<String>();
		}
		
		Set<String> idSet = new HashSet<String>();
		
		String path;
		List<String> ids;
		int pathsSize = paths.size();
		for (int i = 0; i < pathsSize; i++) {
			// i번째 경로
			path = paths.get(i);
			
			// ~ 및 중복 제거한 ConceptId목록 만들기
			// 경로에 구분자(~)가 포함되어 있는 경우, 구분자로 분리(아이디 목록); 구분자가 없는 경우, 분리 하지 않음(단일 아이디)
			ids = path.contains("~") ? new ArrayList<>(asList(path.split("~"))) : new ArrayList<>(asList(path));
			
			idSet.addAll(ids);
		}
		
		return new ArrayList<String>(idSet);
	}
	
	
	/**
	 * Entity ID의 Entity(조건부) 및 하위 목록 조회
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @param page
	 * @param size
	 * @param inclSelf
	 * @return
	 */
	private Page<ConceptViewDTO> getDescendantList(String conceptId, String effectiveTime, int page, int size, boolean inclSelf) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new PageImpl<ConceptViewDTO>(new ArrayList<ConceptViewDTO>());
		}
			
		List<ConceptViewDTO> dtos;
		// 자신의 경로 목록 조회
		List<String> paths = getPathList(conceptId);
				
		if (CollectionUtils.isEmpty(paths)) {
			return new PageImpl<ConceptViewDTO>(new ArrayList<ConceptViewDTO>());
		}
		
		int totalSize = tcSvc.getDescendantCount(conceptId);
		int offset = 0;
		int limit = 0;
		if (inclSelf) {
			totalSize += 1;
			limit = page == 1 ? (size - 1):size;
			offset = page == 1 ? 0:(((page - 1) * limit) - 1);
		} else {
			limit = size;
			offset = (page - 1) * limit;
		}
		
		// 하위 목록 조회; inclSelf값이 true이면 자신의 Entity 포함
		dtos = conceptRepository.findDescendantListByPathsAndEffectiveTime(paths, effectiveTime, offset, limit);
		if (inclSelf && page == 1) {
			List<ConceptViewDTO> inclDtos = new ArrayList<ConceptViewDTO>();
			inclDtos.add(getConcept(conceptId, effectiveTime));
			if (!CollectionUtils.isEmpty(dtos)) {
				inclDtos.addAll(dtos);
			}
			dtos = inclDtos;
		}
		
		
		return new PageImpl<ConceptViewDTO>(dtos, new PageRequest(page - 1, size), totalSize);
	}
	
	/**
	 * Entity ID의 Entity(조건부) 및 하위 목록 조회하여 트리구조 리
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @param page
	 * @param size
	 * @param inclSelf
	 * @return
	 */
	private ConceptTreeDTO getDescendantTree(String conceptId, String effectiveTime, boolean tmp) {
		// SCTID 규칙을 따르지 않는 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId)) {
			return new ConceptTreeDTO();
		}
			
		ConceptTreeDTO dtos  =  new ConceptTreeDTO();
	
		dtos = getConcept2(conceptId, effectiveTime);
		
		dtos.setChildren(recursive(conceptId, effectiveTime));
		
		return dtos;
	}
	
	private List<ConceptTreeDTO> recursive(String conceptId, String effectiveTime) {
		
		List<ConceptTreeDTO> tmps = new ArrayList<ConceptTreeDTO>();
		tmps = conceptRepository.findChildrenByConceptIdAndEffectiveTime2(conceptId, effectiveTime);
		
		int index=0;
		for (ConceptTreeDTO tmp : tmps) {
			ConceptTreeDTO inclDtos = getConcept2(tmp.getConceptId(), effectiveTime);
			
			tmps.get(index).setConceptId(inclDtos.getConceptId());
			tmps.get(index).setTerm(inclDtos.getTerm());
			tmps.get(index).setSemanticTag(inclDtos.getSemanticTag());
			tmps.get(index).setModule(inclDtos.getModule());
			tmps.get(index).setDefinitionStatus(inclDtos.getDefinitionStatus());
			tmps.get(index).setChildrenCount(inclDtos.getChildrenCount());
			tmps.get(index).setDescendantCount(inclDtos.getDescendantCount());
			
			if (inclDtos.getConceptId() != "") {
				tmps.get(index).setChildren(recursive(inclDtos.getConceptId(), effectiveTime ));	
			}
			index++;
		}
		//log.info("tmp : " + tmps.toString());
		return tmps;
	}
	
	/**
	 * Entity ID의 Entity(조건부) 및 상위 목록 조회
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @param page
	 * @param size
	 * @param inclSelf
	 * @return
	 */
	private Page<ConceptViewDTO> getAncestorList(String conceptId, String effectiveTime, int page, int size, boolean inclSelf) {
		// SCTID 규칙을 따르지 않거나 루트인 경우 반환
		if (!SNOMEDCTComponentTypeEnum.isValidIdentifier(conceptId) || SNOMEDCTUtils.PrimaryId.SnomedCTConcept.equals(conceptId)) {
			return new PageImpl<ConceptViewDTO>(new ArrayList<ConceptViewDTO>());
		}

		List<ConceptViewDTO> dtos;
		
		// 부모의 경로 목록 조회
		List<String> paths = getParentPathList(conceptId);
		List<String> ids = getIdList(paths);
		
		if (CollectionUtils.isEmpty(paths) || CollectionUtils.isEmpty(ids)) {
			return new PageImpl<ConceptViewDTO>(new ArrayList<ConceptViewDTO>());
		}
		
		int totalSize = ids.size();
		if (inclSelf) {
			totalSize += 1;
		}
		int offset = (page - 1) * size;
		int limit = size;
		
		// 상위 목록 조회; inclSelf값이 true이면 자신의 Entity 포함
		if (inclSelf) {
			dtos = conceptRepository.findAncestorListOrSelfByConceptIdAndIdsAndEffectiveTime(conceptId, ids, effectiveTime, offset, limit);
		} else {
			dtos = conceptRepository.findAncestorListByIdsAndEffectiveTime(ids, effectiveTime, offset, limit);
		}
		
		return new PageImpl<ConceptViewDTO>(dtos, new PageRequest(page - 1, size), totalSize);
	}

}
