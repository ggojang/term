package co.infoclinic.term.snomedct.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.infoclinic.term.snomedct.api.QryApi;
import co.infoclinic.term.snomedct.model.dto.DescriptionDTO;
import co.infoclinic.term.snomedct.service.DescriptionService;
import co.infoclinic.term.snomedct.service.SchemeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Description API를 제공하는 컨트롤러
 */
@Api(value = "Description", description = "Description", tags = QryApi.API_TAGS_DESCRIPTION)
@RestController(value = "SCTDescCtrl")
public class DescriptionController {

	/** Logger */
	Logger log = LoggerFactory.getLogger(DescriptionController.class);
	
	/** DI: Description service */
	@Autowired
	private DescriptionService descriptionService;

	/** DI: Scheme Service */
	@Autowired
	private SchemeService schemeSvc;
	
	
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------

	/// ----------------------------------------
	/// 조회
	/// ----------------------------------------

	/**
	 * Code로 Description 조회
	 * <p>
	 * ConceptId인 경우에는 Description 목록을 반환
	 * DescriptionId인 경우에는 해당 타입의 Description을 반환
	 * </p>
	 * 
	 * @param version
	 * @param code
	 * @param langGroup
	 * @param typeId (fsn:900000000000003001, prf:900000000000548007,syn:900000000000013009, def:900000000000550004)
	 * @return
	 */
	@ApiOperation(value = "Get Description List")
	@RequestMapping(value = QryApi.API_DESCRIPTION_LIST_BY_CODE, method = RequestMethod.GET)
	public List<DescriptionDTO> getDescriptionByCode(
			@PathVariable(value = QryApi.PARAM_CD) String code,
			@RequestParam(value = QryApi.PARAM_LANGGR, required = false, defaultValue = "1") int langGroup,
			@RequestParam(value = QryApi.PARAM_TYPEID, required = false, defaultValue = "") String typeId,
			@ApiParam(value = QryApi.PARAM_VER_CMNT) @RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {
		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return new ArrayList<DescriptionDTO>();
		}
		
		List<DescriptionDTO> descs;
		  if ("".equals(typeId)) {
			  descs = descriptionService.getDescriptionList(code, schemeSvc.getEffectiveTime(ver), langGroup == 1 ? true:false);
		  } else {
			  descs = descriptionService.getDescriptionList(code, schemeSvc.getEffectiveTime(ver), typeId);
		  }
		  
		  return descs;
	}
	
	
	
	
	/// ----------------------------------------
	/// 생성, 수정, 삭제
	/// ----------------------------------------

	/**
	 * Description을 추가하는 메소드 (LanguageRefset 포함)
	 * 
	 * @param dto
	 * @return
	 * @throws Exception
	 */
	//@RequestMapping(value = "/snomedct/description/item", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ApiOperation(value = "Create Description")
	@RequestMapping(value = QryApi.API_DESCRIPTION_LIST, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public DescriptionDTO createDescription(@RequestBody DescriptionDTO dto) throws Exception {
		return descriptionService.createDescription(dto);
	}

	
	/**
	 * Description을 수정하는 메소드 (LanguageRefset 포함)
	 * 
	 * @param dto
	 * @return
	 * @throws Exception
	 */
	//@RequestMapping(value = "/snomedct/description/item/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
	@ApiOperation(value = "Update Description")
	@RequestMapping(value = QryApi.API_DESCRIPTION_LIST_BY_CODE, method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
	public DescriptionDTO updateDescription(@PathVariable Long code, @RequestBody DescriptionDTO dto) throws Exception {
		return descriptionService.updateDescription(dto);
	}

	
	/**
	 * Description을 제거하는 메소드 (LanguageRefset 포함)
	 * 
	 * @param d
	 * @return
	 * @throws Exception
	 */
	//@RequestMapping(value = "/snomedct/description/item/{id}", method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
	@ApiOperation(value = "Delete Description")
	@RequestMapping(value = QryApi.API_DESCRIPTION_LIST_BY_CODE, method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
	public Boolean deleteDescription(@PathVariable Long code, @RequestBody DescriptionDTO d) throws Exception {
		return descriptionService.deleteDescription(code);
	}
}
