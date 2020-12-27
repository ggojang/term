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
import co.infoclinic.term.loinc.model.dto.LADTO;
import co.infoclinic.term.loinc.service.LAService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "LA", description = "Loinc Answer List", tags = QryApi.API_TAGS_LA)
@RestController(value = "LACtrl")
public class LAController {

	/** Logger */
	Logger log = LoggerFactory.getLogger(LAController.class);

	/** DI: loinc anwser list service */
	@Autowired
	private LAService LASvc;
	
	//@Autowired
	//private LALinkService LALinkSvc;

	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------

	/**
	 * Loinc Answer List
	 */
	@ApiOperation(value = "Get LA")
	@RequestMapping(value = QryApi.API_GET_LA_BY_CODE, method = RequestMethod.GET)
	public List<LADTO> getLAListByCode(@PathVariable(value = QryApi.PARAM_CD) String code) {
		return LASvc.getLAListByCode(code);
	}	
	
	//@ApiOperation(value = "Get LALink")
	//@RequestMapping(value = QryApi.API_GET_LALINK_BY_CODE, method = RequestMethod.GET)
//	public List<LALinkDTO> getLALinkListByCode(@PathVariable(value = QryApi.PARAM_CD) String code) {
		//return LALinkSvc.getLALinkListByCode(code);
	//}	
	
}
