package co.infoclinic.term.snomedct.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import co.infoclinic.term.common.utils.SNOMEDCTUtils;
import co.infoclinic.term.snomedct.model.converter.RefsetMemberConverter;
import co.infoclinic.term.snomedct.model.dto.LanguageRefsetDTO;
import co.infoclinic.term.snomedct.model.dto.RefsetMemberDTO;
import co.infoclinic.term.snomedct.model.dto.RefsetMemberViewDTO;
import co.infoclinic.term.snomedct.model.entity.AbstractReferenceset;
import co.infoclinic.term.snomedct.model.entity.Referenceset;
import co.infoclinic.term.snomedct.model.entity.UserReferenceset;
import co.infoclinic.term.snomedct.repository.RefsetRepository;
import co.infoclinic.term.snomedct.repository.UserRefsetRepository;
import co.infoclinic.term.snomedct.service.RefsetMemberService;
import co.infoclinic.term.snomedct.service.TransitiveClosureService;

/**
 * The Refset Member Service
 */
@Service("SCTMbrSvc")
@Transactional
public class RefsetMemberServiceImpl implements RefsetMemberService {

	/** Logger */
	Logger log = LoggerFactory.getLogger(RefsetMemberServiceImpl.class);

	/** DI: Transitive Closure Service */
	@Autowired
	private TransitiveClosureService tcSvc;
	/** DI: Refset Repository */
	@Autowired
	private RefsetRepository referencesetRepository;
	/** DI: User Refset Repository */
	@Autowired
	private UserRefsetRepository userReferencesetRepository;

	
	// ------------------------------
	// Refset Member 추가, 수정, 삭제
	// ------------------------------
	
	@Override
	public RefsetMemberDTO createReferencesetMember(RefsetMemberDTO dto) {
		UserReferenceset entity = convertToUserEntity(dto, false);
		UserReferenceset responseEntity = saveUserEntity(entity);
		return convertToDTO(responseEntity);
	}

	@Override
	public RefsetMemberDTO updateReferencesetMember(RefsetMemberDTO dto) {
		UserReferenceset entity = convertToUserEntity(dto, false);
		UserReferenceset responseEntity = saveUserEntity(entity);
		return convertToDTO(responseEntity);
	}

	@Override
	public List<RefsetMemberDTO> createReferencesetMemberList(List<RefsetMemberDTO> dtos) {
		List<UserReferenceset> responseEntities = new ArrayList<UserReferenceset>();
		List<UserReferenceset> entities = convertToUserEntityList(dtos, false);

		for (UserReferenceset entity : MoreObjects.firstNonNull(entities, Collections.<UserReferenceset> emptyList())) {
			// save
			UserReferenceset responseEntity = saveUserEntity(entity);
			responseEntities.add(responseEntity);
		}

		// entities to dtos
		return convertToDTOList(responseEntities);
	}

	@Override
	public List<RefsetMemberDTO> updateReferencesetMemberList(List<RefsetMemberDTO> dtos) {
		List<UserReferenceset> responseEntities = new ArrayList<UserReferenceset>();
		List<UserReferenceset> entities = convertToUserEntityList(dtos, false);

		for (UserReferenceset entity : MoreObjects.firstNonNull(entities, Collections.<UserReferenceset> emptyList())) {
			// save
			UserReferenceset responseEntity = saveUserEntity(entity);
			responseEntities.add(responseEntity);
		}

		// entities to dtos
		return convertToDTOList(responseEntities);
	}

	@Override
	public boolean deleteReferencesetMember(Long id) {
		referencesetRepository.delete(id);
		return true;
	}

	@Override
	public boolean deleteReferencesetMember(RefsetMemberDTO dto) {
		UserReferenceset entity = convertToUserEntity(dto, true);
		return deleteReferencesetMember(entity.getId());
	}

	@Override
	public boolean deleteReferencesetMemberList(List<RefsetMemberDTO> dtos) {
		List<UserReferenceset> entities = convertToUserEntityList(dtos, true);
		userReferencesetRepository.deleteInBatch(entities);
		return true;
	}
	
	
	// ------------------------------
	// Language Refset 추가, 수정, 삭제
	// ------------------------------

	@Override
	public LanguageRefsetDTO createLanguageReferencesetMember(LanguageRefsetDTO dto) {
		Referenceset entity = convertToEntity(dto);
		Referenceset responseEntity = saveEntity(entity);
		return convertToLangDTO(responseEntity);
	}

	@Override
	public List<LanguageRefsetDTO> createLanguageReferencesetMemberList(List<LanguageRefsetDTO> dtos,
			String referencedComponentId) {
		List<Referenceset> responseEntities = Lists.newArrayList();
		List<Referenceset> entities = convertToEntityList(dtos);

		for (Referenceset entity : MoreObjects.firstNonNull(entities, Collections.<Referenceset> emptyList())) {
			entity.setReferencedComponentId(referencedComponentId);
			// save
			Referenceset responseEntity = saveEntity(entity);
			responseEntities.add(responseEntity);
		}
		// entity to dto
		return convertToLangDTOList(responseEntities);
	}

	@Override
	public List<LanguageRefsetDTO> updateLanguageReferencesetMemberList(List<LanguageRefsetDTO> dtos) {
		List<Referenceset> responseEntities = Lists.newArrayList();
		List<Referenceset> entities = convertToEntityList(dtos);

		for (Referenceset entity : MoreObjects.firstNonNull(entities, Collections.<Referenceset> emptyList())) {
			// save
			Referenceset responseEntity = saveEntity(entity);
			responseEntities.add(responseEntity);
		}
		// entity to dto
		return convertToLangDTOList(responseEntities);
	}

	
	@Override
	public Boolean deleteLanguageReferencesetMember(Long id, LanguageRefsetDTO dto) {
		referencesetRepository.delete(id);
		return true;
	}
	
	
	
	// ------------------------------
	// Refset Member 조회
	// ------------------------------
	

	

	@Override
	public List<RefsetMemberViewDTO> getHistoricalReferencesetMemberList(String componentId,
			String effectiveTime) {
		List<String> refsetIdList = new ArrayList<>(Arrays.asList("900000000000530003", // ALTERNATIVE
				"900000000000525002", // MOVED FROM
				"900000000000524003", // MOVED TO
				"900000000000523009", // POSSIBLY EQUIVALENT TO
				"900000000000531004", // REFERES TO
				"900000000000526001", // REPLACED BY
				"900000000000527005", // SAME AS
				"900000000000529008", // SIMILAR TO
				"900000000000528000" // WAS A
		));

		List<Referenceset> referencesetList = referencesetRepository
				.findByRefsetIdsAndReferencedComponentIdAndEffectiveTime(refsetIdList, componentId, effectiveTime);
		return convertToViewDTOList(referencesetList);
	}

	@Override
	public List<RefsetMemberViewDTO> getConceptInactivationReferencesetMemberList(String conceptId,
			String effectiveTime) {
		// 900000000000489007: Concept inactivation indicator attribute value references set
		List<String> refsetIdList = new ArrayList<>(Arrays.asList("900000000000489007"));

		List<Referenceset> referencesetList = referencesetRepository
				.findByRefsetIdsAndReferencedComponentIdAndEffectiveTime(refsetIdList, conceptId, effectiveTime);
		return convertToViewDTOList(referencesetList);
	}

	@Override
	public List<RefsetMemberViewDTO> getDescriptionInactivationReferencesetMemberList(String descriptionId,
			String effectiveTime) {
		// 900000000000490003: Description inactivation indicator attribute value references set
		List<String> refsetIdList = new ArrayList<>(Arrays.asList("900000000000490003"));

		List<Referenceset> referencesetList = referencesetRepository
				.findByRefsetIdsAndReferencedComponentIdAndEffectiveTime(refsetIdList, descriptionId, effectiveTime);
		return convertToViewDTOList(referencesetList);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// ------------------------------
	// Private methods
	// ------------------------------
	
	private UserReferenceset saveUserEntity(UserReferenceset entity) {
		// TODO 이미 해당 refset에 member가 들어있다면 어떻게 처리해줄지 고민해서 반영하기
		return userReferencesetRepository.save(entity);
	}

	private Referenceset saveEntity(Referenceset entity) {
		// TODO 이미 해당 refset에 member가 들어있다면 어떻게 처리해줄지 고민해서 반영하기
		return referencesetRepository.save(entity);
	}

	private LanguageRefsetDTO convertToLangDTO(Referenceset entity) {
		return RefsetMemberConverter.toLangDTO(entity);
	}

	private Referenceset convertToEntity(LanguageRefsetDTO dto) {
		return RefsetMemberConverter.toEntity(dto);
	}

	private List<Referenceset> convertToEntityList(List<LanguageRefsetDTO> dtos) {
		return RefsetMemberConverter.toEntityList(dtos);
	}

	private List<LanguageRefsetDTO> convertToLangDTOList(List<Referenceset> entities) {
		return RefsetMemberConverter.toLangDTOList(entities);
	}

	//////////////////////

	private List<RefsetMemberViewDTO> convertToViewDTOList(List<AbstractReferenceset> entities,
			String refsetTypeId) {
		return RefsetMemberConverter.toViewDTOList(entities, refsetTypeId);
	}

	private List<RefsetMemberViewDTO> convertToViewDTOList(List<? extends AbstractReferenceset> entities) {
		if (entities == null || entities.isEmpty()) {
			return null;
		}

		List<RefsetMemberViewDTO> dtoList = new ArrayList<RefsetMemberViewDTO>();

		Map<String, List<AbstractReferenceset>> map = new HashMap<String, List<AbstractReferenceset>>();
		for (AbstractReferenceset result : entities) {
			String refsetId = result.getRefsetId();
			if (!map.containsKey(refsetId)) {
				List<AbstractReferenceset> list = new ArrayList<AbstractReferenceset>();
				list.add(result);

				map.put(refsetId, list);
			} else {
				map.get(refsetId).add(result);
			}
		}

		List<AbstractReferenceset> refsets = null;
		List<String> ancestorPathList = null;
		Iterator<String> itr = map.keySet().iterator();
		while (itr.hasNext()) {
			refsets = map.get(itr.next());
			String matchId = null;
			ancestorPathList = tcSvc.getParentPathListByConceptId(refsets.get(0).getRefsetId());
			if (ancestorPathList.size() == 1) {
				String ancestorPath = ancestorPathList.get(0);
				int idx = ancestorPath.indexOf(SNOMEDCTUtils.MetadataType.Referenceset);
				if (idx > 0) {
					int lastSpace = ancestorPath.indexOf("~", idx);
					if (lastSpace == -1)
						matchId = refsets.get(0).getRefsetId();
					else {
						String trimPath = ancestorPath.substring(lastSpace + 1); // ~제거위함
						if (trimPath.contains("~")) {
							matchId = trimPath.substring(0, trimPath.indexOf("~"));
						} else {
							matchId = trimPath;
						}
					}
				}
			}
			if (matchId != null) {
				dtoList.addAll(convertToViewDTOList(refsets, matchId));
			}
		}
		return dtoList;
	}


	//////////////////////For UserReferenceset Entity
	
	private RefsetMemberDTO convertToDTO(UserReferenceset entity) {
		return RefsetMemberConverter.toDTO(entity);
	}
	
	private List<RefsetMemberDTO> convertToDTOList(List<UserReferenceset> entities) {
		return RefsetMemberConverter.toDTOList(entities);
	}
	
	private UserReferenceset convertToUserEntity(RefsetMemberDTO dto, boolean isIncludeId) {
		return RefsetMemberConverter.toUserEntity(dto, isIncludeId);
	}
	
	private List<UserReferenceset> convertToUserEntityList(List<RefsetMemberDTO> dtos, boolean isIncludeId) {
		return RefsetMemberConverter.toUserEntityList(dtos, isIncludeId);
	}
	
}
