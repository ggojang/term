package co.infoclinic.term.loinc.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.infoclinic.term.common.model.dto.SubsumptionTestDTO;
import co.infoclinic.term.loinc.api.QryApi;
import co.infoclinic.term.loinc.model.dto.HierarchyDTO;
import co.infoclinic.term.loinc.model.dto.LoincDTO;
import co.infoclinic.term.loinc.service.HierarchyService;
import co.infoclinic.term.loinc.service.LoincService;
import co.infoclinic.term.loinc.utils.PartEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "Entity", description = "Entity", tags = QryApi.API_TAGS_ENTITY)
@RestController(value = "LNCEntityCtrl")
public class EntityController {

	/** Logger */
	Logger log = LoggerFactory.getLogger(EntityController.class);

	/** DI: hierarchy service */
	@Autowired
	private HierarchyService hierSvc;

	/** DI: loinc service */
	@Autowired
	private LoincService loincSvc;

	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------

	
	/**
	 * LOINC Entity
	 * 
	 * @param version
	 * @param code
	 */
	@ApiOperation(value = "Get Entity")
	@RequestMapping(value = QryApi.API_GET_ENTITY_BY_CODE, method = RequestMethod.GET)
	public LoincDTO getEntityByCode(@PathVariable(value = QryApi.PARAM_CD) String code) {
		return loincSvc.getEntityByCode(code);
	}
	
	/**
	 * LOINC의 Entity의 자식 목록
	 * 
	 * @param version
	 * @param code
	 * @param path
	 * @param lang
	 * @return
	 */
	@ApiOperation(value = "Get Children")
	@RequestMapping(value = QryApi.API_GET_CHILDREN_BY_CODE, method = RequestMethod.GET)
	public List<HierarchyDTO> getChildrenByCode(
			@PathVariable(value = QryApi.PARAM_CD) String code,
			@RequestParam(value = QryApi.PARAM_PATH, required = false, defaultValue = "") String path,
			@RequestParam(value = QryApi.PARAM_LANG, required = false, defaultValue = "") String lang) {
		return hierSvc.getChildren(code, path, lang);
	}

	 
	/**
	 * LOINC Entity의 부모 목록
	 * 
	 * @param version
	 * @param code
	 * @return
	 */
	@ApiOperation(value = "Get Parent List")
	@RequestMapping(value = QryApi.API_GET_PARENTS_BY_CODE, method = RequestMethod.GET)
	public List<HierarchyDTO> getParentListByCode(
			@PathVariable(value = QryApi.PARAM_CD) String code) {
		return hierSvc.getParentListByCode(code);
	}
	
	
	/**
	 * LOINC Entity 하위 목록
	 * 
	 * @param version
	 * @param code
	 * @return
	 */
	@ApiOperation(value = "Get Descendant List")
	@RequestMapping(value = QryApi.API_GET_DESCENDANTS_BY_CODE, method = RequestMethod.GET)
	public List<HierarchyDTO> getDescendantList(
			@PathVariable(value = QryApi.PARAM_CD) String code,
			@RequestParam(value = QryApi.PARAM_PATH, required = false, defaultValue = "") String path) {
		return hierSvc.getDescendantList(code, path, false);
	}
	
	
	/**
	 * LOINC Entity 자신을 포함한 하위 목록
	 * 
	 * @param version
	 * @param code
	 * @return
	 */
	@ApiOperation(value = "Get Descendant or Self List")
	@RequestMapping(value = QryApi.API_GET_DESCENDANTS_OR_SELF_BY_CODE, method = RequestMethod.GET)
	public List<HierarchyDTO> getDescendantOrSelfList(
			@PathVariable(value = QryApi.PARAM_CD) String code,
			@RequestParam(value = QryApi.PARAM_PATH, required = false, defaultValue = "") String path) {
		return hierSvc.getDescendantList(code, path, true);
	}
	
	
	/**
	 * LOINC Entity의 상위 목록
	 * 
	 * @param version
	 * @param code
	 * @return
	 */
	@ApiOperation(value = "Get Ancestor List")
	@RequestMapping(value = QryApi.API_GET_ANCESTORS_BY_CODE, method = RequestMethod.GET)
	public List<HierarchyDTO> getAncestorList(
			@PathVariable(value = QryApi.PARAM_CD) String code) {
		return hierSvc.getAncestorList(code, false);
	}
	
	
	/**
	 * LOINC Entity 자신 포함 상위 목록
	 * 
	 * @param version
	 * @param code
	 * @return
	 */
	@ApiOperation(value = "Get Ancestor or Self List")
	@RequestMapping(value = QryApi.API_GET_ANCESTORS_OR_SELF_BY_CODE, method = RequestMethod.GET)
	public List<HierarchyDTO> getAncestorOrSelfList(
			@PathVariable(value = QryApi.PARAM_CD) String code) {
		return hierSvc.getAncestorList(code, true);
	}
	
	
	/**
	 * LOINC Entity 목록
	 * 
	 * @param version
	 * @param part
	 * @param value
	 * @param page
	 * @param size
	 * @return
	 */
	@ApiOperation(value = "Get Entity List By Version And Part And Value")
	@RequestMapping(value = QryApi.API_GET_ENTITIES, method = RequestMethod.GET)
	public Page<LoincDTO> getEntityList(
			@RequestParam(value = QryApi.PARAM_PART) PartEnum part,
			@RequestParam(value = QryApi.PARAM_VAL) String value,
			@RequestParam(value = QryApi.PARAM_PAGE, required = false, defaultValue = "1") int page,
			@RequestParam(value = QryApi.PARAM_SIZE, required = false, defaultValue = "1000") int size) {
		return loincSvc.getEntityList(part, value, page, size);
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
			@RequestParam(value = QryApi.PARAM_CD) String code,
			@RequestParam(value = QryApi.PARAM_CRITERIA_CD) String criteriaCode) {
		return loincSvc.subsumptionTest(criteriaCode, code);
	}
	
	
	/**
	 * LOINC Entity의 경로 목록
	 * 
	 * @param version
	 * @param code
	 */
	@ApiOperation(value = "Get Path List")
	@RequestMapping(value = QryApi.API_GET_PATHS_BY_CODE, method = RequestMethod.GET)
	public List<HierarchyDTO> getPathList(
			@PathVariable(value = QryApi.PARAM_CD) String code,
			@RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {
		return hierSvc.getPathListByCode(code);
	}
	
	
}
