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
import co.infoclinic.term.snomedct.model.dto.ComponentDTO;
import co.infoclinic.term.snomedct.service.HistoryService;
import co.infoclinic.term.snomedct.service.SchemeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * History API를 제공하는 컨트롤러
 */
@Api(value = "History", description = "History", tags = QryApi.API_TAGS_HISTORY)
@RestController(value = "SCTHistCtrl")
public class HistoryController {

	/** Logger */
	Logger log = LoggerFactory.getLogger(HistoryController.class);

	/** DI: History service */
	@Autowired
	private HistoryService historyService;

	/** DI: Scheme Service */
	@Autowired
	private SchemeService schemeSvc;
	
	
	
	
	/**
	 * SNOMED CT Component의 히스토리 목록 조회
	 * 
	 * @param version v + yyyyMMdd; SNOMEDCT Release Date
	 * @param code SNOMEDCT Component Id
	 * @return
	 */
	@ApiOperation(value = "Get History List By Code")
	@RequestMapping(value = QryApi.API_GET_HISTORY_LIST_BY_CODE, method = RequestMethod.GET)
	public List<? extends ComponentDTO> getHistoryList(
			@PathVariable(value = QryApi.PARAM_CD) String code,
			@ApiParam(value = QryApi.PARAM_VER_CMNT) @RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {
		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return new ArrayList<>();
		}
		
		return historyService.getHistory(code, schemeSvc.getEffectiveTime(ver));
	}
}
