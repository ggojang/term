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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.infoclinic.term.common.model.dto.SubsumptionTestDTO;
import co.infoclinic.term.snomedct.api.QryApi;
import co.infoclinic.term.snomedct.model.dto.ConceptViewDTO;
import co.infoclinic.term.snomedct.model.dto.ConceptTreeDTO;
import co.infoclinic.term.snomedct.model.entity.Scheme;
import co.infoclinic.term.snomedct.service.ConceptService;
import co.infoclinic.term.snomedct.service.SchemeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Concept API를 제공하는 컨트롤러
 */
@Api(value = "Entity", description = "Entity", tags = QryApi.API_TAGS_ENTITY)
@RestController(value = "SCTEntityCtrl")
public class EntityController {

	/** Logger */
	Logger log = LoggerFactory.getLogger(EntityController.class);
	
	/** Default Page Number */
	private static final String DEFAULT_PAGE = "1";
	
	/** Default Size Number */
	private static final String DEFAULT_SIZE = "20";

	/** DI: Concept Service. */
	@Autowired
	private ConceptService conceptService;
	
	/** DI: Scheme Service */
	@Autowired
	private SchemeService schemeSvc;

	
	
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------

	/**
	 * 용어체계의 현재 버전을 반환
	 * 
	 * @param version
	 * @return
	 */
	@ApiOperation(value = "Get Version")
	@RequestMapping(value = QryApi.API_GET_VERSION, method = RequestMethod.GET)
	public List<Scheme> getSchemeList() {
		return schemeSvc.getSchemeList();
	}
	
	
	/**
	 * Entity 조회
	 * 
	 * @param version
	 * @param code
	 * @return
	 */
	@ApiOperation(value = "Get Entity")
	@RequestMapping(value = QryApi.API_GET_ENTITY_BY_CODE, method = RequestMethod.GET)
	public ConceptViewDTO getConceptByCode(
			@ApiParam(value = QryApi.PARAM_CD_CMNT) @PathVariable(value = QryApi.PARAM_CD) String code,
			@ApiParam(value = QryApi.PARAM_VER_CMNT) @RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {
		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return new ConceptViewDTO();
		}
		
		return conceptService.getConcept(code, schemeSvc.getTcEffectiveTime(ver));
	}

	
	/**
	 * Entity의 자식 목록 조회
	 * 
	 * @param version
	 * @param code
	 * @return
	 */
	@ApiOperation(value = "Get Children")
	@RequestMapping(value = QryApi.API_GET_CHILDREN_BY_CODE, method = RequestMethod.GET)
	public List<ConceptViewDTO> getChildrenByCode(
			@ApiParam(value = QryApi.PARAM_CD_CMNT) @PathVariable(value = QryApi.PARAM_CD) String code,
			@ApiParam(value = QryApi.PARAM_VER_CMNT) @RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {
		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return new ArrayList<ConceptViewDTO>();
		}
		
		return conceptService.getChildren(code, schemeSvc.getTcEffectiveTime(ver));
	}
	
	
	/**
	 * Entity의 하위 목록 조회
	 * 
	 * @param version
	 * @param code
	 * @param offset
	 * @param limit
	 * @return
	 */
	@ApiOperation(value = "Get Descendant List")
	@RequestMapping(value = QryApi.API_GET_DESCENDANTS_BY_CODE, method = RequestMethod.GET)
	public Page<ConceptViewDTO> getDescendantsByCode(
			@ApiParam(value = QryApi.PARAM_CD_CMNT) @PathVariable(value = QryApi.PARAM_CD) String code,
			@RequestParam(value = QryApi.PARAM_PAGE, required = false, defaultValue = DEFAULT_PAGE) int page,
			@RequestParam(value = QryApi.PARAM_SIZE, required = false, defaultValue = DEFAULT_SIZE) int size,
			@ApiParam(value = QryApi.PARAM_VER_CMNT) @RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {
		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return new PageImpl<ConceptViewDTO>(new ArrayList<ConceptViewDTO>());
		}
		
		return conceptService.getDescendantList(code, schemeSvc.getTcEffectiveTime(ver), page, size);
	}
	
	
	/**
	 * Entity 및 하위 목록 조회
	 * 
	 * @param version
	 * @param code
	 * @param offset
	 * @param limit
	 * @return
	 */
	@ApiOperation(value = "Get Descendant List Or Self")
	@RequestMapping(value = QryApi.API_GET_DESCENDANTS_OR_SELF_BY_CODE, method = RequestMethod.GET)
	public Page<ConceptViewDTO> getDescendantsOrSelfByCode(
			@ApiParam(value = QryApi.PARAM_CD_CMNT) @PathVariable(value = QryApi.PARAM_CD) String code,
			@RequestParam(value = QryApi.PARAM_PAGE, required = false, defaultValue = DEFAULT_PAGE) int page,
			@RequestParam(value = QryApi.PARAM_SIZE, required = false, defaultValue = DEFAULT_SIZE) int size,
			@ApiParam(value = QryApi.PARAM_VER_CMNT) @RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {
		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return new PageImpl<ConceptViewDTO>(new ArrayList<ConceptViewDTO>());
		}
		
		return conceptService.getDescendantListOrSelf(code, schemeSvc.getTcEffectiveTime(ver), page, size);
	}
	 
	/**
	 * Entity의 하위 목록 조회하여 트리구조 리턴 
	 * 
	 * @param version
	 * @param code
	 * @param offset
	 * @param limit
	 * @return
	 */
	@ApiOperation(value = "Get Descendant Tree")
	@RequestMapping(value = QryApi.API_GET_DESCENDANTS_TREE_BY_CODE, method = RequestMethod.GET)
	public ConceptTreeDTO getDescendantsTreeByCode(
			@ApiParam(value = QryApi.PARAM_CD_CMNT) @PathVariable(value = QryApi.PARAM_CD) String code,
			@ApiParam(value = QryApi.PARAM_VER_CMNT) @RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {
		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return new ConceptTreeDTO();
		}
		
		return conceptService.getDescendantTree(code, schemeSvc.getTcEffectiveTime(ver));
	}
	
	/**
	 * Entity의 부모 목록 조회
	 * 
	 * @param version
	 * @param code
	 * @return
	 */
	@ApiOperation(value = "Get Parent List")
	@RequestMapping(value = QryApi.API_GET_PARENTS_BY_CODE, method = RequestMethod.GET)
	public List<ConceptViewDTO> getParentsByCode(
			@ApiParam(value = QryApi.PARAM_CD_CMNT) @PathVariable(value = QryApi.PARAM_CD) String code,
			@ApiParam(value = QryApi.PARAM_VER_CMNT) @RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {
		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return new ArrayList<ConceptViewDTO>();
		}
		
		return conceptService.getParentList(code, schemeSvc.getTcEffectiveTime(ver));
	}
	
	
	/**
	 * Entity의 상위 목록 조회
	 * 
	 * @param version
	 * @param code
	 * @param offset
	 * @param limit
	 * @return
	 */
	@ApiOperation(value = "Get Ancestor List")
	@RequestMapping(value = QryApi.API_GET_ANCESTORS_BY_CODE, method = RequestMethod.GET)
	public Page<ConceptViewDTO> getAncestorsByCode(
			@ApiParam(value = QryApi.PARAM_CD_CMNT) @PathVariable(value = QryApi.PARAM_CD) String code,
			@RequestParam(value = QryApi.PARAM_PAGE, required = false, defaultValue = DEFAULT_PAGE) int page,
			@RequestParam(value = QryApi.PARAM_SIZE, required = false, defaultValue = DEFAULT_SIZE) int size,
			@ApiParam(value = QryApi.PARAM_VER_CMNT) @RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {
		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return new PageImpl<ConceptViewDTO>(new ArrayList<ConceptViewDTO>());
		}
		
		return conceptService.getAncestorList(code, schemeSvc.getTcEffectiveTime(ver), page, size);
	}
	
	
	/**
	 * Entity 및 상위 목록 조회
	 * 
	 * @param version
	 * @param code
	 * @param offset
	 * @param limit
	 * @return
	 */
	@ApiOperation(value = "Get Ancestor List Or Self")
	@RequestMapping(value = QryApi.API_GET_ANCESTORS_OR_SELF_BY_CODE, method = RequestMethod.GET)
	public Page<ConceptViewDTO> getAncestorsOrSelfByCode(
			@ApiParam(value = QryApi.PARAM_CD_CMNT) @PathVariable(value = QryApi.PARAM_CD) String code,
			@RequestParam(value = QryApi.PARAM_PAGE, required = false, defaultValue = DEFAULT_PAGE) int page,
			@RequestParam(value = QryApi.PARAM_SIZE, required = false, defaultValue = DEFAULT_SIZE) int size,
			@ApiParam(value = QryApi.PARAM_VER_CMNT) @RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {
		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return new PageImpl<ConceptViewDTO>(new ArrayList<ConceptViewDTO>());
		}
		
		return conceptService.getAncestorListOrSelf(code, schemeSvc.getTcEffectiveTime(ver), page, size);
	}
	
	
	/**
	 * 두 코드의 포함관계 조회
	 * 
	 * @param version 버전
	 * @param code 코드
	 * @param criteriaCode 기준코드
	 * @return
	 */
	@ApiOperation(value = "Subsumption Test")
	@RequestMapping(value = QryApi.API_GET_SUBSUMPTION, method = RequestMethod.GET)
	public SubsumptionTestDTO subsumptionTest(
			@ApiParam(value = QryApi.PARAM_CD_CMNT) @RequestParam(value = QryApi.PARAM_CD) String code,
			@RequestParam(value = QryApi.PARAM_CRITERIA) String criteria,
			@ApiParam(value = QryApi.PARAM_VER_CMNT) @RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {
		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return new SubsumptionTestDTO();
		}
		
		return conceptService.subsumptionTest(criteria, code, schemeSvc.getTcEffectiveTime(ver));
	}
	
}