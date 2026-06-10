package co.infoclinic.term.snomedct.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import co.infoclinic.term.common.model.dto.Value;
import co.infoclinic.term.common.utils.PropertiesUtil;
import co.infoclinic.term.common.utils.SNOMEDCTComponentTypeEnum;
import co.infoclinic.term.common.utils.SNOMEDCTUtils;
import co.infoclinic.term.snomedct.model.converter.RefsetMemberConverter;
import co.infoclinic.term.snomedct.model.dto.IdTermDTO;
import co.infoclinic.term.snomedct.model.dto.RefsetMemberQryDTO;
import co.infoclinic.term.snomedct.model.dto.RefsetMemberViewDTO;
import co.infoclinic.term.snomedct.model.entity.AbstractReferenceset;
import co.infoclinic.term.snomedct.model.entity.LatestRefsetMember;
import co.infoclinic.term.snomedct.model.entity.Referenceset;
import co.infoclinic.term.snomedct.model.entity.UserReferenceset;
import co.infoclinic.term.snomedct.repository.LatestRefsetMemberRepository;
import co.infoclinic.term.snomedct.repository.RefsetRepository;
import co.infoclinic.term.snomedct.repository.UserRefsetRepository;
import co.infoclinic.term.snomedct.service.ConceptService;
import co.infoclinic.term.snomedct.service.RefsetMemberQueryService;
import co.infoclinic.term.snomedct.service.RefsetService;
import co.infoclinic.term.snomedct.service.SearchService;
import co.infoclinic.term.snomedct.service.TransitiveClosureService;
import io.searchbox.core.SearchResult;

/**
 * 레퍼런스세트 멤버 조회관련 처리를 담당하는 서비스
 */
@Service(value = "RefsetMbrQrySvc")
public class RefsetMemberQueryServiceImpl implements RefsetMemberQueryService {

	/** Logger */
	Logger log = LoggerFactory.getLogger(RefsetMemberQueryServiceImpl.class);
	
	private static final String PREFIX_MEMBER_IDX = "refset-member";
	
	/** Concept service */
	@Autowired
	private ConceptService cnptSvc;

	/** DI: Search service */
	@Autowired
	private SearchService srchSvc;
	
	/** DI: Refset service */
	@Autowired
	private RefsetService refsetSvc;
	
	/** DI: Transitive Closure service */
	@Autowired
	private TransitiveClosureService tcSvc;
	
	/** DI: Latest Refset Member repository */
	@Autowired
	private LatestRefsetMemberRepository latestMbrRepo;
	
	@Autowired
	private RefsetRepository refsetRepo;
	
	/** DI: User Refset Repository */
	@Autowired
	private UserRefsetRepository userRefsetRepo;
	
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------

	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.RefsetMemberQueryService#getMemberList(java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public Page<RefsetMemberQryDTO> getMemberList(String refsetId, String effectiveTime, String q, int page, int size) {
		// 반환 객체
		Page<RefsetMemberQryDTO> mbrs = null;
		
		// 레퍼런스세트 아이디 목록 조회
		List<String> refsetIds = refsetSvc.getReferencesetIdList();
		
		// 레퍼런스세트 아이디 목록에 대상 아이디가 포함되어있다면
		if (refsetIds.contains(refsetId)) {
			// 페이지
			Pageable pageRequest = new PageRequest(page - 1, size);
			// 검색어
			String value = null;
			
			// 검색어를 입력받은 경우(*가 아닌 경우)
			if (!"*".equals(q)) {
				value = q;
			}
			
			// Term 조회 대상 언어
			String lang = "en";
			
			// 멤버 목록 조회
			mbrs = getMemberList(refsetId, effectiveTime, lang, value, pageRequest);
			
		}
		// 국제배포판 이외의 레퍼런스세트에 해당하는 경우
		else {
			if ("*".equals(q)) {
				// 멤버 전체 목록 호출
				mbrs = new PageImpl<RefsetMemberQryDTO>(getMemberListByFile(refsetId));
			} else {
				// 멤버 검색결과 목록 호출
				mbrs = new PageImpl<RefsetMemberQryDTO>(getSearchResultList(refsetId, q));
			}
		}
		return mbrs;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.RefsetMemberQueryService#getSpecificMemberList(java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public Page<RefsetMemberQryDTO> getSpecificMemberList(String refsetId, String refCpntId, String effectiveTime, int page, int size) {
		// 반환 객체
		Page<RefsetMemberQryDTO> mbrs = null;
		
		// 레퍼런스세트 아이디 목록 조회
		List<String> refsetIds = refsetSvc.getReferencesetIdList();
		
		// 레퍼런스세트 아이디 목록에 대상 아이디가 포함되어있다면
		if (refsetIds.contains(refsetId)) {
			// 페이지, 201810 by Yu : page => page-1 
			Pageable pageRequest = new PageRequest(page-1, size);

			// Term 조회 대상 언어
			String lang = "en";
			
			// 멤버 목록 조회
			mbrs = getSpecificMemberList(refsetId, refCpntId, effectiveTime, lang, pageRequest);
		}
		
		return mbrs != null ? mbrs : new PageImpl<RefsetMemberQryDTO>(new ArrayList<RefsetMemberQryDTO>());
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.RefsetMemberQueryService#getDescriptorList(java.lang.String, java.lang.String)
	 */
	@Override
	public List<RefsetMemberQryDTO> getDescriptorList(String refsetId, String effectiveTime) {
		List<RefsetMemberQryDTO> dtos = null;
		List<LatestRefsetMember> list = latestMbrRepo.findByEditionAndVersionAndRefsetId("INT", effectiveTime, refsetId);
		
		dtos = toQryDTOList(list);
		
		return dtos;
	}
	

	@Override
	public List<RefsetMemberViewDTO> getMemberList(String referencedComponentId, String effectiveTime) {
		List<Referenceset> members = refsetRepo.findByReferencedComponentIdAndEffectiveTime(referencedComponentId, effectiveTime);
		return convertMemberDTOList(members, effectiveTime);
	}
	
	

	@Override
	public List<RefsetMemberViewDTO> getMemberList(String refsetId, String referencedComponentId, String effectiveTime) {
		// 1. referencesetId의 ancestorPath를 찾는다.
		// 2. 900000000000455006 다음의 id를 찾는다.
		String matchId = null;
		List<String> ancestorPathList = tcSvc.getParentPathListByConceptId(refsetId);
		if (ancestorPathList.size() == 1) {
			String ancestorPath = ancestorPathList.get(0);
			int idx = ancestorPath.indexOf(SNOMEDCTUtils.MetadataType.Referenceset);
			if (idx > 0) {
				int lastSpace = ancestorPath.indexOf("~", idx);
				if (lastSpace == -1) {
					matchId = refsetId;
				} else {
					String trimPath = ancestorPath.substring(lastSpace + 1); // ~제거위함
					if (trimPath.contains("~"))
						matchId = trimPath.substring(0, trimPath.indexOf("~"));
					else
						matchId = trimPath;
				}
			}
		}
		List<AbstractReferenceset> results = new ArrayList<AbstractReferenceset>();
		List<Referenceset> rList = null;
		List<UserReferenceset> urList = null;
		if (matchId != null) {
			rList = refsetRepo.findByRefsetIdAndReferencedComponentId(refsetId, referencedComponentId);

			urList = userRefsetRepo.findByRefsetIdAndReferencedComponentId(refsetId, referencedComponentId);

			if (rList != null) {
				results.addAll(rList);
			}
			if (urList != null) {
				results.addAll(urList);
			}

		}
		if (results.isEmpty()) {
			return Lists.newArrayList();
		}

		return convertToViewDTOList(results, matchId);
	}
	

	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.RefsetMemberQueryService#getMemberEntityList(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<Referenceset> getMemberEntityList(String refsetId, String referencedComponentId, String effectiveTime) {
		return refsetRepo.findByRefsetIdAndReferencedComponentIdAndEffectiveTime(refsetId, referencedComponentId, effectiveTime);
	}

	// ------------------------------
	// Private methods
	// ------------------------------
	
	
	/**
	 * 파일로부터 멤버 목록을 반환하는 메소드
	 * @return member list
	 */
	private List<RefsetMemberQryDTO> getMemberListByFile(String refsetId) {
		List<RefsetMemberQryDTO> mbrs = new ArrayList<RefsetMemberQryDTO>();
		RefsetMemberQryDTO mbr = null;
		
		FileReader fr = null;
		BufferedReader br = null;
		//Gson gson = null;
		String line = null;
		String path = getRefsetFileLocation(refsetId);
		
		// FIXME: 유효한 경로인지 확인 ? 진행:에러
		
		File dirf = new File(path.substring(0, path.lastIndexOf("/")));
		if (!dirf.exists()) {
			dirf.mkdir();
		}
		
		// 파일이 존재하지 않을 경우 생성
		File f = new File(path);
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			fr = new FileReader(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		br = new BufferedReader(fr);
		try {
			while ((line = br.readLine()) != null) {
				String[] l = line.split("\t");
				
				mbr = new RefsetMemberQryDTO();
				mbr.setRefset(new Value(refsetId, null));
				mbr.setReferencedComponent(new Value(l[0], l[1]));
				mbrs.add(mbr);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mbrs;
	}
	
	
	/**
	 * 레퍼런스세트 아이디로부터 리소스(파일)의 위치를 반환하는 메소드
	 * 
	 * @param refsetId
	 * @return
	 */
	private String getRefsetFileLocation(String refsetId) {
		// FIXME: implements... 
		// bodysite_relative_location
		
		PropertiesUtil prop = null;
		try {
			prop = new PropertiesUtil();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String refsetPath = prop.getPropValue("sct.refset.dir").toString();
		
		String location = refsetPath + "/contents/simple/" + refsetId + "/SCT-REFSET-SIMPLE-MEMBER-ACTIVE-" + refsetId + ".tsv";
		
		return location;
	}
	
	
	/**
	 * 레퍼런스세트 멤버 내 검색 결과를 반환하는 메소드
	 * 
	 * @param refsetId
	 * @param q
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private List<RefsetMemberQryDTO> getSearchResultList(String refsetId, String q) {
		List<RefsetMemberQryDTO> mbrs = new ArrayList<RefsetMemberQryDTO>();
		
		// set index
		String idx = PREFIX_MEMBER_IDX + "-simple-" + refsetId;
		
		// FIXME: change strategy of index name (name to id)
		SearchResult srchRslt = srchSvc.getSearchResult(idx, getMemberQuery(q));
		
		// path: /hits/hits[]/_source
		//List<Hit<ESRefsetMember, Void>> mbrs = srchRslt.getHits(ESRefsetMember.class);
		// or
		// 결과가 없는 경우, 초기화된 리스트를 반환.
		List<IdTermDTO> dtos = srchRslt.getSourceAsObjectList(IdTermDTO.class);
		
		RefsetMemberQryDTO mbr;
		for (IdTermDTO dto : dtos) {
			mbr = new RefsetMemberQryDTO();
			mbr.setRefset(new Value(refsetId, null));
			mbr.setReferencedComponent(new Value(dto.getId(), dto.getTerm()));
			mbrs.add(mbr);
		}
		
		return mbrs;
	}
	
	/**
	 * 멤버를 찾는 쿼리를 반환하는 메소드
	 * 
	 * @param q
	 * @return
	 */
	private String getMemberQuery(String q) {
		// fetch size
		int size = 100;
		// FIXME: numeric & digit 6~18 ? true
		boolean isNumeric = StringUtils.isNumeric(q);
		boolean isSctId = false;
		
		// 숫자타입 이면서 6~18자리라면 id 아니면 term
		if (isNumeric) {
			int num = (int)(Math.log10(Integer.parseInt(q))+1);
			if (num > 5 && num < 19) {
				isSctId = true;
			}
		}
	
		// query: id가 q && 활성상태인 && 삭제되지않은
		String query = "{\n" +
				"	\"query\" : { \n" +
			    "     \"bool\" : { \n" +
			    "       \"filter\" : [ \n";
		
		if (isSctId) {
			query += "         {\n" +
				     "          \"term\" : { \n" +
				     "            \"id\" : \"" + q + "\" \n" +
					 "            }\n" +
				     "         },\n";
		} else {
			query += "         {\n" +
				     "          \"match\" : { \n" +
				     "            \"term\" : \"" + q + "\" \n" +
					 "            }\n" +
				     "         },\n";
		}
			    
	   query += "         {\n" +
			    "          \"term\" : { \n" +
			    "            \"act\" : \"1\" \n" +
			    "            }\n" +
			    "         },\n" +
			    "         {\n" +
			    "          \"term\" : { \n" +
			    "            \"del\" : \"0\" \n" +
			    "            }\n" +
			    "         }\n" +
			    "       ]\n" +
			    "     }\n" +
				"	}, \n" +
				"   \"size\":" + size + "\n" +
				"}";
		
		return query;
	}
	
	
	/**
	 * 레퍼런스세트 멤버 조회
	 * 
	 * <pre>
	 * 1. 조회 대상 레퍼런스세트의 경로 목록 조회 (레퍼런스세트의 경우에는 경로가 1개임)
	 * 2.
	 * </pre>
	 * 
	 * @param refsetId
	 * @param effectiveTime
	 * @param languageCode
	 * @param value
	 * @param pageRequest
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Page<RefsetMemberQryDTO> getMemberList(String refsetId, String effectiveTime,
			String languageCode, String value, Pageable pageRequest) {

		// 반환 객체
		//Page<AbstractReferenceset> results = null;
		Page<RefsetMemberQryDTO> results = null;
		
		// 참조세트 분류 아이디
		String categoryId = null;
		
		// 루트~부모경로 목록(경로간 구분은 '~')
		List<String> paths = tcSvc.getParentPathListByConceptId(refsetId);
		
		// 루트~부모경로 목록의 크기
		int pathsSize = paths.size();
		
		if (pathsSize == 1) { // 루트~부모 경로가 있을 경우(참조세트 경우 부모경로는 하나)
			// 루트~부모경로
			String path = paths.get(0);
			
			// 루트~부모경로내에 900000000000455006 |Reference set (foundation metadata concept)| 컨셉 위치 확인
			int refsetCnptIdx = path.indexOf(SNOMEDCTUtils.MetadataType.Referenceset);
			// 위치가 존재하는 경우
			if (refsetCnptIdx != -1) {
				// 900000000000455006 |Reference set (foundation metadata concept)| 하위 컨셉 확인
				int subtypeChkIdx = path.indexOf("~", refsetCnptIdx);
				if (subtypeChkIdx == -1) { // 하위 컨셉이 없는 경우, 파라메터인 refsetId가 categoryId임.
					categoryId = refsetId;
				} else { // 하위 컨셉이 있는 경우
					String remainderPath = path.substring(subtypeChkIdx + 1); // ~제거위함
					if (remainderPath.contains("~")) {
						categoryId = remainderPath.substring(0, remainderPath.indexOf("~"));
					} else {
						categoryId = remainderPath;
					}
				}
			}
		} else if (pathsSize == 0) { // TODO 루트~부모 경로가 없을 경우(존재하지 않는 아이디이거나 개발 중
										// refsetId가 concept에 등록되어있지않아 발생하는
										// 경우임.)
			// Simpletype
			//refsetTypeId = "446609009";
		}

		if (categoryId != null) { // 레퍼런스세트 분류가 있는 경우
			List<LatestRefsetMember> list = null;
			int totalCount = 0;
			//AbstractRefsetRepository absRefsetRepo = (AbstractRefsetRepository) refsetRepo;
			// *(전체)가 아닌 검색어를 입력받은 경우
			if (value != null) {
				//pageRequest = new PageRequest(0, pageRequest.getPageSize());
				//results = absRefsetRepo.findBySearchTerm(refsetId, value, pageRequest);
				String word = "";
				boolean isNumeric = StringUtils.isNumericSpace(value);
				if (isNumeric) {
					 if (SNOMEDCTComponentTypeEnum.CONCEPT.equals(SNOMEDCTComponentTypeEnum.getById(value)) || SNOMEDCTComponentTypeEnum.DESCRIPTION.equals(SNOMEDCTComponentTypeEnum.getById(value))) {
					     
					 } else {
					    isNumeric = false;
					 }
				}
				
				if (!isNumeric) {
					List<String> queryPieces = Arrays.asList(value.split(" "));
					int queryPiecesSize = queryPieces != null ? queryPieces.size():0;
					for (int i = 0; i < queryPiecesSize; i++) {
						if (i == 0) {
							word = "+" + queryPieces.get(i);
						} else {
							word += " " + queryPieces.get(i);
						}
					}
				}
				
				
				list = latestMbrRepo.findByEditionAndVersionAndRefsetIdAndTermAndOffsetAndLimit("INT", effectiveTime, refsetId, word, pageRequest.getOffset(), pageRequest.getPageSize());
				totalCount = latestMbrRepo.findCountByEditionAndVersionAndRefsetIdAndTerm("INT", effectiveTime, refsetId, word);
			} else {
				//results = absRefsetRepo.findByRefsetId(refsetId, pageRequest);
				list = latestMbrRepo.findByEditionAndVersionAndRefsetIdAndOffsetAndLimit("INT", effectiveTime, refsetId, pageRequest.getOffset(), pageRequest.getPageSize());
				totalCount = latestMbrRepo.findCountByEditionAndVersionAndRefsetId("INT", effectiveTime, refsetId);
			}
			
			results = toQryDTOPage(list, pageRequest, totalCount);
			
		}

		return results != null ? results : new PageImpl(new ArrayList<RefsetMemberQryDTO>());
	}
	
	
	/**
	 * 
	 * @param refsetId
	 * @param refCpntId
	 * @param effectiveTime
	 * @param lang
	 * @param pageRequest
	 * @return
	 */
	private Page<RefsetMemberQryDTO> getSpecificMemberList(String refsetId, String refCpntId, String effectiveTime,
			String lang, Pageable pageRequest) {
		
		// 반환 객체
		Page<RefsetMemberQryDTO> results = null;
		
		List<LatestRefsetMember> list = latestMbrRepo.findByEditionAndVersionAndRefsetIdAndRefCpntIdAndOffsetAndLimit("INT", effectiveTime, refsetId, refCpntId, pageRequest.getOffset(), pageRequest.getPageSize());
		int totalCount = latestMbrRepo.findCountByEditionAndVersionAndRefsetIdAndRefCpntId("INT", effectiveTime, refsetId, refCpntId);

		results = toQryDTOPage(list, pageRequest, totalCount);
		
		return results;
	}
	
	
	/**
	 * Entity List to DTO List
	 * 
	 * @param list
	 * @return
	 */
	private List<RefsetMemberQryDTO> toQryDTOList(List<LatestRefsetMember> list) {
		List<RefsetMemberQryDTO> dtos = new ArrayList<RefsetMemberQryDTO>();
		RefsetMemberQryDTO dto = null;
		List<Value> fields = null;
		LatestRefsetMember mbr = null;
		int mbrsSize = list.size();
		for (int i = 0; i < mbrsSize; i++) {
			mbr = list.get(i);
			
			dto = new RefsetMemberQryDTO();
			
			// set uuid
			dto.setUuid(mbr.getId().getUuid());
			dto.setEffectiveTime(mbr.getEffectiveTime());
			dto.setModule(new Value(mbr.getModuleId(), mbr.getModuleName()));
			dto.setRefset(new Value(mbr.getRefsetId(), mbr.getRefsetName()));
			dto.setReferencedComponent(new Value(mbr.getReferencedComponentId(), mbr.getReferencedComponentName()));
			dto.setReferencedComponentActive(mbr.isReferencedComponentActive());
			
			fields = new ArrayList<Value>();
			dto.setFields(fields);
			
			String field1Id = mbr.getField1Id();
			String field1Value = mbr.getField1Value();
			
			String field2Id = mbr.getField2Id();
			String field2Value = mbr.getField2Value();
			
			String field3Id = mbr.getField3Id();
			String field3Value = mbr.getField3Value();
			
			String field4Id = mbr.getField4Id();
			String field4Value = mbr.getField4Value();
			
			String field5Id = mbr.getField5Id();
			String field5Value = mbr.getField5Value();
			
			String field6Id = mbr.getField6Id();
			String field6Value = mbr.getField6Value();
			
			String field7Id = mbr.getField7Id();
			String field7Value = mbr.getField7Value();
			
			if (field1Id != null || field1Value != null) {
				fields.add(new Value(field1Id, field1Value));
			}
			
			if (field2Id != null || field2Value != null) {
				fields.add(new Value(field2Id, field2Value));
			}
			
			if (field3Id != null || field3Value != null) {
				fields.add(new Value(field3Id, field3Value));
			}
			
			if (field4Id != null || field4Value != null) {
				fields.add(new Value(field4Id, field4Value));
			}
			
			if (field5Id != null || field5Value != null) {
				fields.add(new Value(field5Id, field5Value));
			}
			
			if (field6Id != null || field6Value != null) {
				fields.add(new Value(field6Id, field6Value));
			}
			
			if (field7Id != null || field7Value != null) {
				fields.add(new Value(field7Id, field7Value));
			}
			
			dtos.add(dto);
		}
		
		return dtos;
	}
	
	
	/**
	 * 
	 * @param members
	 * @param pageRequest
	 * @param categoryId
	 * @return
	 */
	private Page<RefsetMemberQryDTO> toQryDTOPage(List<LatestRefsetMember> members, Pageable pageRequest, int totalCount) {
		return new PageImpl<RefsetMemberQryDTO>(toQryDTOList(members), pageRequest, totalCount);
	}
	
	
	/**
	 * 
	 * @param members
	 * @param effectiveTime
	 * @return
	 */
	private List<RefsetMemberViewDTO> convertMemberDTOList(List<Referenceset> members, String effectiveTime) {
		List<RefsetMemberViewDTO> dtos = new ArrayList<RefsetMemberViewDTO>();
		List<String> ancestorPaths = null;
		Referenceset member = null;
		int membersLen = members.size();
		for (int i = 0; i < membersLen; i++) {
			member = members.get(i);
			String referencesetId = member.getRefsetId();
			ancestorPaths = tcSvc.getParentPathListByConceptId(referencesetId);
			String refsetTypeId = null;
			if (ancestorPaths.size() == 1) {
				String ancestorPath = ancestorPaths.get(0);
				int idx = ancestorPath.indexOf(SNOMEDCTUtils.MetadataType.Referenceset);
				if (idx > 0) {
					int lastSpace = ancestorPath.indexOf("~", idx);
					if (lastSpace == -1) {
						refsetTypeId = referencesetId;
					} else {
						String trimPath = ancestorPath.substring(lastSpace + 1); // ~제거위함
						if (trimPath.contains("~")) {
							refsetTypeId = trimPath.substring(0, trimPath.indexOf("~"));
						} else {
							refsetTypeId = trimPath;
						}
					}
				}
			}
			if (refsetTypeId != null) {
				dtos.add(RefsetMemberConverter.toViewDTO(member, refsetTypeId, effectiveTime));
			}
		}
		return dtos;
	}


	private List<RefsetMemberViewDTO> convertToViewDTOList(List<AbstractReferenceset> entities,
			String refsetTypeId) {
		return RefsetMemberConverter.toViewDTOList(entities, refsetTypeId);
	}


}
