package co.infoclinic.term.snomedct.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.infoclinic.term.snomedct.api.QryApi;
import co.infoclinic.term.snomedct.model.dto.LanguageRefsetDTO;
import co.infoclinic.term.snomedct.model.dto.RefsetMemberCmdDTO;
import co.infoclinic.term.snomedct.model.dto.RefsetMemberDTO;
import co.infoclinic.term.snomedct.model.dto.RefsetMemberQryDTO;
import co.infoclinic.term.snomedct.model.dto.RefsetMemberViewDTO;
import co.infoclinic.term.snomedct.service.RefsetMemberCommandService;
import co.infoclinic.term.snomedct.service.RefsetMemberQueryService;
import co.infoclinic.term.snomedct.service.RefsetMemberService;
import co.infoclinic.term.snomedct.service.SchemeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Referenceset Member API를 제공하는 컨트롤러
 */
@Api(value = "Referenceset Member", description = "Referenceset Member", tags = QryApi.API_TAGS_MEMBER)
@RestController(value = "SCTRefsetMbrCtrl")
public class RefsetMemberController {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(RefsetMemberController.class);

	/** DI: Referenceset Member service */
	@Autowired
	private RefsetMemberService mbrSvc;
	
	/** DI: Referencset Member query service */
	@Autowired
	private RefsetMemberQueryService mbrQrySvc;
	
	/** DI: Referenceset Member command service */
	@Autowired
	private RefsetMemberCommandService mbrCmdSvc;
	
	/** DI: Scheme Service */
	@Autowired
	private SchemeService schemeSvc;
	
	
	
	
	/**
	 * 
	 * @param refsetId
	 * @param refCpntId
	 * @param version
	 * @param q 쿼리; MemberId|Part of Description|SNOMED CT Constraint Expr
	 * @param page
	 * @param size
	 * @param view 반환되는 객체 구조; list tree
	 * @return
	 */
	@ApiOperation(value = "Get Referenceset Member List")
	@RequestMapping(value = "/members/SNOMEDCT/{refsetid}", method = RequestMethod.GET)
	public Page<RefsetMemberQryDTO> getMemberList(
			@PathVariable(value = QryApi.PARAM_REFSET_ID) String refsetId,
			@RequestParam(value = QryApi.PARAM_REFCPNT_ID, required = false) String refCpntId,
			@RequestParam(value = QryApi.PARAM_VER, required = false) String ver,
			@RequestParam(value = QryApi.PARAM_Q, required = false, defaultValue = "*") String q,
			@RequestParam(value = QryApi.PARAM_PAGE, required = false, defaultValue = "1") int page,
			@RequestParam(value = QryApi.PARAM_SIZE, required = false, defaultValue = "500") int size,
			@RequestParam(value = "view", required = false, defaultValue = "list") String view) {
		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return new PageImpl<RefsetMemberQryDTO>(new ArrayList<RefsetMemberQryDTO>());
		}
		System.out.print("version :" + schemeSvc.getEffectiveTime(schemeSvc.getLatestVersion()));
		return refCpntId == null ?
				mbrQrySvc.getMemberList(refsetId, schemeSvc.getEffectiveTime(schemeSvc.getLatestVersion()), q, page, size): // 20200702 by Yu
				mbrQrySvc.getSpecificMemberList(refsetId, refCpntId, schemeSvc.getEffectiveTime(schemeSvc.getLatestVersion()), page, size); // 20200702 by Yu
	}
	

	/**
	 * 
	 * @param refsetId
	 * @param version
	 * @return
	 */
	@ApiOperation(value = "Get Referenceset Descriptor List")
	@RequestMapping(value = "/descriptors/SNOMEDCT/{refsetid}", method = RequestMethod.GET)
	public List<RefsetMemberQryDTO> getDescriptorList(
			@PathVariable(value = QryApi.PARAM_REFSET_ID) String refsetId,
			@RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {
		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return new ArrayList<RefsetMemberQryDTO>();
		}
		return mbrQrySvc.getDescriptorList(refsetId, schemeSvc.getEffectiveTime(ver));
	}
	
	
	/**
	 * 
	 * @param refCpntId
	 * @param ver
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "Get Refset Member List")
	@RequestMapping(value = QryApi.API_GET_REFSET_MBR_LIST, method = RequestMethod.GET)
	public List<RefsetMemberViewDTO> getReferencesetMemberList(
			@RequestParam(value = QryApi.PARAM_REFCPNT_ID, required = true) String refCpntId,
			@RequestParam(value = QryApi.PARAM_VER, required = false) String ver) throws Exception {
		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return new ArrayList<RefsetMemberViewDTO>();
		}
		return mbrQrySvc.getMemberList(refCpntId, schemeSvc.getEffectiveTime(ver));
	}
	
	
	
	/// ----------------------------------------
	/// Referenceset Member 추가, 수정, 삭제
	/// ----------------------------------------
	
	/**
	 * 레퍼런스세트 멤버를 추가하는 메소드
	 * 
	 * @param dto
	 * @return
	 */
	@RequestMapping(value = "/snomedct/refset/{refsetId}/members", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public void addRefsetMemberList(
			@PathVariable String refsetId,
			@RequestBody RefsetMemberCmdDTO dto) {
		
		mbrCmdSvc.addMemberList(refsetId, dto);
	}
	
	/**
	 * 레퍼런스세트 멤버를 추가하는 메소드
	 * 
	 * @param dto
	 * @return
	 */
	@RequestMapping(value = "/snomedct/refset/member", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RefsetMemberDTO createReferencesetMember(@RequestBody RefsetMemberDTO dto) {
		return mbrSvc.createReferencesetMember(dto);
	}

	/**
	 * 레퍼런스세트 멤버들을 추가하는 메소드
	 * 
	 * @param referencesetMemberDTOList
	 * @return
	 */
	@RequestMapping(value = "/snomedct/refset/members", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public List<RefsetMemberDTO> createReferencesetMemberList(
			@RequestBody List<RefsetMemberDTO> referencesetMemberDTOList) {
		return mbrSvc.createReferencesetMemberList(referencesetMemberDTOList);
	}

	
	@RequestMapping(value = "/snomedct/refset/member/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
	public RefsetMemberDTO updateReferencesetMember(@RequestBody RefsetMemberDTO referencesetMemberDTO) {
		return mbrSvc.updateReferencesetMember(referencesetMemberDTO);
	}

	
	@RequestMapping(value = "/snomedct/refset/members", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
	public List<RefsetMemberDTO> updateReferencesetMemberList(
			@RequestBody List<RefsetMemberDTO> referencesetMemberDTOList) {
		return mbrSvc.updateReferencesetMemberList(referencesetMemberDTOList);
	}

	
	@RequestMapping(value = "/snomedct/refset/member/{id}", method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
	public boolean deleteReferencesetMember(@PathVariable Long id, @RequestBody RefsetMemberDTO dto) {
		return mbrSvc.deleteReferencesetMember(dto);
	}

	
	@RequestMapping(value = "/snomedct/refset/members", method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
	public boolean deleteReferencesetMemberList(@RequestBody List<RefsetMemberDTO> referencesetMemberDTOList) {
		return mbrSvc.deleteReferencesetMemberList(referencesetMemberDTOList);
	}
	
	
	
	
	
	
	
	/// ----------------------------------------
	/// Language Referenceset Member 추가, 삭제
	/// ----------------------------------------

	@RequestMapping(value = "/snomedct/refset/members/language/item", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public LanguageRefsetDTO createLanguageRefset(@RequestBody LanguageRefsetDTO dto) throws Exception {
		return mbrSvc.createLanguageReferencesetMember(dto);
	}

	@RequestMapping(value = "/snomedct/refset/members/item/{id}", method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
	public Boolean deleteLanguageRefset(@RequestBody LanguageRefsetDTO dto) throws Exception {
		return mbrSvc.deleteLanguageReferencesetMember(dto.getId(), dto);
	}
}
