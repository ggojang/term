package co.infoclinic.term.snomedct.controller;

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
import co.infoclinic.term.snomedct.service.ConceptService;
import co.infoclinic.term.snomedct.service.SchemeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Expression API를 제공하는 컨트롤러
 */
@Api(value = "Expression", description = "Expression", tags = QryApi.API_TAGS_EXPRESSION)
@RestController(value = "SCTExprCtrl")
public class ExpressionController {

	/** Logger */
	Logger log = LoggerFactory.getLogger(ExpressionController.class);

	/** The concept service. */
	@Autowired
	private ConceptService conceptService;
	
	/** DI: Scheme Service */
	@Autowired
	private SchemeService schemeSvc;
	
	
	
	
	/**
	 * Entity의 Post-coordinated Expression 조회
	 * 
	 * @param version
	 * @param code
	 * @return
	 */
	@ApiOperation(value = "Get Post-coordinated Expression Test")
	@RequestMapping(value = QryApi.API_GET_POST_EXPR, method = RequestMethod.GET)
	public String getPostExpr(@PathVariable(value = QryApi.PARAM_CD) String code,
			@RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {
		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return "";
		}
		
		return conceptService.getPostExpr(code, schemeSvc.getEffectiveTime(ver));
	}

}
