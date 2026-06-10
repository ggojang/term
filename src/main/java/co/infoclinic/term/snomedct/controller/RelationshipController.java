package co.infoclinic.term.snomedct.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.infoclinic.term.snomedct.api.QryApi;
import co.infoclinic.term.snomedct.model.dto.RelationshipViewDTO;
import co.infoclinic.term.snomedct.service.RelationshipService;
import co.infoclinic.term.snomedct.service.SchemeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Relationship API를 제공하는 컨트롤러
 */
@Api(value = "Association", description = "Relationship", tags = QryApi.API_TAGS_ASSOCIATION)
@RestController(value = "SCTRelCtrl")
public class RelationshipController {

	/** Logger */
	Logger log = LoggerFactory.getLogger(RelationshipController.class);

	/** DI: Relationship Service */
	@Autowired
	private RelationshipService relSvc;

	/** DI: Scheme Service */
	@Autowired
	private SchemeService schemeSvc;
	
	
	
	/**
	 * Relationship 조회
	 * <p>
	 * 요청 방법: http://api/associations/SNOMEDCT/{code}?stated=1?version={version}
	 * </p>
	 * 
	 * @param version EffectiveTime
	 * @param code SNOMEDCT ConceptId
	 * @param active
	 * @param stated
	 * @return
	 */
	@ApiOperation(value = "Get Relationship List")
	@RequestMapping(value = QryApi.API_GET_ASSOCIATION_LIST_BY_CODE, method = RequestMethod.GET)
	public List<RelationshipViewDTO> getRelationshipListByCode(
		@ApiParam(value = QryApi.PARAM_CD_CMNT) @PathVariable(value = QryApi.PARAM_CD) String code,
		@ApiParam(value = QryApi.PARAM_STATED_CMNT) @RequestParam(value = QryApi.PARAM_STATED, required = false, defaultValue = "0") int stated,
		@ApiParam(value = QryApi.PARAM_VER_CMNT) @RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {
		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return new ArrayList<RelationshipViewDTO>(); 
		}
		
		return relSvc.getRelationshipList(code, schemeSvc.getEffectiveTime(ver), stated == 1 ? true:false);
	}

}
