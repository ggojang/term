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

import co.infoclinic.term.loinc.api.QryApi;
import co.infoclinic.term.loinc.model.dto.LPDTO;
import co.infoclinic.term.loinc.model.dto.LPLinkDTO;
import co.infoclinic.term.loinc.model.dto.LPMapDTO;
import co.infoclinic.term.loinc.service.LPLinkService;
import co.infoclinic.term.loinc.service.LPMapService;
import co.infoclinic.term.loinc.service.LPService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "LP", description = "Loinc Part", tags = QryApi.API_TAGS_LP)
@RestController(value = "LPCtrl")
public class LPController {

	/** Logger */
	Logger log = LoggerFactory.getLogger(LPController.class);

	/** DI: loinc service */
	@Autowired
	private LPService lpSvc;
	
	@Autowired
	private LPLinkService LPLinkSvc;
	
	@Autowired
	private LPMapService LPMapSvc;

	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------

	/**
	 * LOINC Part
	 * 
	 * @param version
	 * @param partNumber
	 */
	@ApiOperation(value = "Get LP")
	@RequestMapping(value = QryApi.API_GET_LP_BY_CODE, method = RequestMethod.GET)
	public LPDTO getLPByCode(@PathVariable(value = QryApi.PARAM_CD) String code) {
		return lpSvc.getLPByCode(code);
	}	
	
	@ApiOperation(value = "Get LPLINK")
	@RequestMapping(value = QryApi.API_GET_LPLINK_BY_CODE, method = RequestMethod.GET)
	public List<LPLinkDTO> getLPLinkListByCode(
			@ApiParam(value = "LOINC or Part Number") @PathVariable(value = QryApi.PARAM_CD) String code) {
		return LPLinkSvc.getLPLinkListByCode(code);
	}	
	
	@ApiOperation(value = "Get LPMAP")
	@RequestMapping(value = QryApi.API_GET_LPMAP_BY_CODE, method = RequestMethod.GET)
	public List<LPMapDTO> getLPMapListByCode(@PathVariable(value = QryApi.PARAM_CD) String code) {
		return LPMapSvc.getLPMapListByCode(code);
	}	
	
}
