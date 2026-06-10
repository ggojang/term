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
import co.infoclinic.term.loinc.model.dto.LinguisticVariantDTO;
import co.infoclinic.term.loinc.service.LinguisticVariantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "LV", description = "Loinc Lingustic Variant", tags = QryApi.API_TAGS_LV)
@RestController(value = "LinguisticVariantCtrl")
public class LinguisticVariantController {

	/** Logger */
	Logger log = LoggerFactory.getLogger(LinguisticVariantController.class);

	/** DI: loinc service */
	@Autowired
	private LinguisticVariantService LinguisticVariantSvc;

	// ----------------------------------------
	// Public methods
	// ----------------------------------------

	/**
	 * LOINC Part
	 * 
	 * @param version
	 * @param partNumber
	 */
	@ApiOperation(value = "Get LinguisticVariant")
	@RequestMapping(value = QryApi.API_GET_LV_BY_CODE, method = RequestMethod.GET)
	public List<LinguisticVariantDTO> getLinguisticVariantByCode(@PathVariable(value = QryApi.PARAM_CD) String code) {
		return LinguisticVariantSvc.getLinguisticVariantListByCode(code);
	}	
	
}
