package co.infoclinic.term.loinc.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import co.infoclinic.term.loinc.api.QryApi;
import co.infoclinic.term.loinc.model.dto.PanelDTO;
import co.infoclinic.term.loinc.service.PanelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * LOINC Panel API 컨트롤러
 */
@Api(value = "Panel", description = "Panel", tags = QryApi.API_TAGS_PANEL)
@RestController(value="LNCPanelCtrl")
public class PanelController {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(PanelController.class);

	/** DI: panel service */
	@Autowired
	private PanelService panelSvc;

	
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	
	/**
	 * 
	 * @param code
	 * @param ver
	 * @return
	 */
	@ApiOperation(value = "Get Panel")
	@RequestMapping(value = QryApi.API_GET_PANEL_BY_CODE, method = RequestMethod.GET)
	public PanelDTO getPanel(@PathVariable(value = QryApi.PARAM_CD) String code) {
		return panelSvc.getPanel(code);
	}
}
