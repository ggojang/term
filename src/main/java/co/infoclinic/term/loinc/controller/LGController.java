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
import co.infoclinic.term.loinc.model.dto.LGDTO;
import co.infoclinic.term.loinc.model.dto.LGAttrDTO;
import co.infoclinic.term.loinc.model.dto.LGTermDTO;
import co.infoclinic.term.loinc.model.dto.LGPDTO;
import co.infoclinic.term.loinc.model.dto.LGPAttrDTO;
import co.infoclinic.term.loinc.service.LGAttrService;
import co.infoclinic.term.loinc.service.LGPAttrService;
import co.infoclinic.term.loinc.service.LGPService;
import co.infoclinic.term.loinc.service.LGService;
import co.infoclinic.term.loinc.service.LGTermService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "LG", description = "Loinc Group", tags = QryApi.API_TAGS_LG)
@RestController(value = "LGCtrl")
public class LGController {

	/** Logger */
	Logger log = LoggerFactory.getLogger(LGController.class);

	/** DI: loinc group service */
	@Autowired
	private LGService LGSvc;
	
	@Autowired
	private LGAttrService LGAttrSvc;
	
	@Autowired
	private LGTermService LGTermSvc;
	
	@Autowired
	private LGPService LGPSvc;
	
	@Autowired
	private LGPAttrService LGPAttrSvc;

	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------

	/**
	 * LOINC Part
	 * 
	 * @param version
	 * @param partNumber
	 */
	@ApiOperation(value = "Get LG")
	@RequestMapping(value = QryApi.API_GET_LG_BY_CODE, method = RequestMethod.GET)
	public List<LGDTO> getLGByCode(@PathVariable(value = QryApi.PARAM_CD) String code) {
		return LGSvc.getLGListByCode(code);
	}	
	
	@ApiOperation(value = "Get LGATTR")
	@RequestMapping(value = QryApi.API_GET_LGATTR_BY_CODE, method = RequestMethod.GET)
	public List<LGAttrDTO> getLGAttrListByCode(@PathVariable(value = QryApi.PARAM_CD) String code) {
		return LGAttrSvc.getLGAttrListByCode(code);
	}
	
	@ApiOperation(value = "Get LGTerm")
	@RequestMapping(value = QryApi.API_GET_LGTERM_BY_CODE, method = RequestMethod.GET)
	public List<LGTermDTO> getLGTermListByCode(
			@ApiParam(value = "LOINC or Group Number") @PathVariable(value = QryApi.PARAM_CD) String code) {
		return LGTermSvc.getLGTermListByCode(code);
	}
	
	@ApiOperation(value = "Get LGP")
	@RequestMapping(value = QryApi.API_GET_LGP_BY_CODE, method = RequestMethod.GET)
	public LGPDTO getLGPByCode(@PathVariable(value = QryApi.PARAM_CD) String code) {
		return LGPSvc.getLGPByCode(code);
	}
	
	@ApiOperation(value = "Get LGPATTR")
	@RequestMapping(value = QryApi.API_GET_LGPATTR_BY_CODE, method = RequestMethod.GET)
	public LGPAttrDTO getLGPAttrByCode(@PathVariable(value = QryApi.PARAM_CD) String code) {
		return LGPAttrSvc.getLGPAttrByCode(code);
	}	
}
