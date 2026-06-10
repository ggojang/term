package co.infoclinic.term.snomedct.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.infoclinic.term.snomedct.api.QryApi;
import co.infoclinic.term.snomedct.model.dto.DefiningAttributeDTO;
import co.infoclinic.term.snomedct.model.dto.TermSearchResult;
import co.infoclinic.term.snomedct.service.MrcmService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * MRCM API를 제공하는 컨트롤러
 */
@Api(value = "MRCM", description = "MRCM", tags = QryApi.API_TAGS_MRCM)
@RestController(value = "SCTMrcmCtrl")
public class MrcmController {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(MrcmController.class);
	
	/** DI: MRCM service */
	@Autowired
	private MrcmService mrcmSvc;
	
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------

	/// ----------------------------------------
	/// 조회
	/// ----------------------------------------
		
	
	/**
	 * 
	 * @param conceptId
	 * @return
	 */
	@ApiOperation(value = "Get Allow Defining Attribute List")
	@RequestMapping(value = QryApi.API_GET_MRCM_ALLOW_ATTR_LIST_BY_CODE, method = RequestMethod.GET)
	public List<DefiningAttributeDTO> getDefiningAttributesByTopLevelHierarchy(
		@ApiParam(value = QryApi.PARAM_CD_CMNT) @PathVariable(value = QryApi.PARAM_CD) String conceptId) {
		return mrcmSvc.getAllowDefiningAttributeList(conceptId);
	}
	
	
	
	/**
	 * Attribute에서 허용하는 범위의 Value 검색
	 * 
	 * @param version
	 * @param attrId 
	 * @param q
	 * @param size
	 * @return
	 */
	@ApiOperation(value = "Get Allow Value List")
	@RequestMapping(value = QryApi.API_GET_MRCM_ALLOW_VALUE_LIST_BY_CODE, method = RequestMethod.GET)
	public List<TermSearchResult> getValueList(
			@ApiParam(value = QryApi.PARAM_ATTR_ID_CMNT) @PathVariable(value = QryApi.PARAM_ATTR_ID) String attrId,
			@ApiParam(value = QryApi.PARAM_Q_CMNT) @RequestParam(value = QryApi.PARAM_Q) String q,
			@ApiParam(value = QryApi.PARAM_SIZE_CMNT) @RequestParam(value = QryApi.PARAM_SIZE, required = false, defaultValue = "10") int size) {
		return mrcmSvc.getValueList(attrId, q, size);
	}
}
