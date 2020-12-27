package co.infoclinic.term.snomedct.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.infoclinic.term.snomedct.api.QryApi;
import co.infoclinic.term.snomedct.model.dto.ConceptViewDTO;
import co.infoclinic.term.snomedct.service.ConceptService;
import co.infoclinic.term.snomedct.service.SchemeService;
import co.infoclinic.term.snomedct.utils.ECLParserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Constraint API를 제공하는 컨트롤러
 */
@Api(value = "Constraint", description = "Constraint", tags = QryApi.API_TAGS_CONSTRAINT)
@RestController(value = "SCTCnstCtrl")
public class ConstraintController {

	/** Logger */
	Logger log = LoggerFactory.getLogger(ConstraintController.class);

	/** The concept service. */
	@Autowired
	private ConceptService conceptService;
	
	/** DI: Scheme Service */
	@Autowired
	private SchemeService schemeSvc;
	
	
	
	/**
	 * 
	 * 
	 * @param version
	 * @param page
	 * @param size
	 * @param ecl
	 * @return
	 */
	@ApiOperation(value = "Get Entity List by ECL")
	@RequestMapping(value = QryApi.API_GET_ENTITIES, method = RequestMethod.GET)
	public List<ConceptViewDTO> getEntityListByECL(
			@RequestParam(value = "ecl", required = true) String ecl,
			@RequestParam(value = QryApi.PARAM_PAGE, required = false, defaultValue = "1") int page,
			@RequestParam(value = QryApi.PARAM_SIZE, required = false, defaultValue = "1000") int size,
			@RequestParam(value = QryApi.PARAM_VER, required = false) String ver) {
		if (StringUtils.isEmpty(ver)) {
			ver = schemeSvc.getLatestVersion();
		} else if (!schemeSvc.isValid(ver)) {
			return new ArrayList<ConceptViewDTO>();
		}
		
		List<ConceptViewDTO> dto = null;
		Map<String, Object> paramMap = ECLParserUtil.getParamExpression(ecl);
		
		if(paramMap.get("G") != null || paramMap.get("N") != null) {//Attribute
			dto = (List<ConceptViewDTO>) conceptService.getConceptList(paramMap,  schemeSvc.getEffectiveTime(ver));
		} else {
			String key = paramMap.keySet().iterator().next();
			if(key.equals("DESCENDANTOF")) dto = conceptService.getDescendantList(paramMap.get(key).toString(), schemeSvc.getEffectiveTime(ver), page, size).getContent();
			else if(key.equals("DESCENDANTORSELFOF")) dto = conceptService.getDescendantListOrSelf(paramMap.get(key).toString(), schemeSvc.getEffectiveTime(ver), page, size).getContent();
			else if(key.equals("CHILDOF")) dto = conceptService.getChildren(paramMap.get(key).toString(), schemeSvc.getEffectiveTime(ver));
			else if(key.equals("ANCESTOF")) dto = conceptService.getAncestorList(paramMap.get(key).toString(), schemeSvc.getEffectiveTime(ver), page, size).getContent();
			else if(key.equals("ANCESTORSELFOF")) dto = conceptService.getAncestorList(paramMap.get(key).toString(), schemeSvc.getEffectiveTime(ver), page, size).getContent();
			else if(key.equals("PARENTOF")) dto = conceptService.getParentList(paramMap.get(key).toString(), schemeSvc.getEffectiveTime(ver));
			//else if(key.equals("SCTID")) dto = conceptService.getConcept(paramMap.get(key).toString(), getEffectiveTime(version));
			else if(key.equals("SCTID")) {
				dto = new ArrayList<ConceptViewDTO>();
				dto.add(conceptService.getConcept(paramMap.get(key).toString(), schemeSvc.getEffectiveTime(ver)));
			}
		}
		return dto;
	}
}
