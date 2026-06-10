package co.infoclinic.term.snomedct.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import co.infoclinic.term.common.owl.OWLEntrance;
import co.infoclinic.term.common.utils.SNOMEDCTComponentTypeEnum;
import co.infoclinic.term.common.utils.SNOMEDCTIdentifierGenerator;
import co.infoclinic.term.snomedct.api.QryApi;
import co.infoclinic.term.snomedct.service.ConceptEditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "Entity", description = "Entity", tags = QryApi.API_TAGS_ENTITY)
@RestController
@RequestMapping("/snomedct/ccpteditor")
public class ConceptEditController {
	
	Logger log = LoggerFactory.getLogger(ConceptEditController.class);
	
	@Autowired
	private ConceptEditService service;
	
	@ApiOperation(value = "Stated")
	@RequestMapping(value = "/stated", method = RequestMethod.POST, consumes="application/json",produces="application/json")
	public Map<String, Object> stated(@RequestBody Map<String, Object> paramObj) throws Exception {
		
		HttpServletRequest req = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		String userIp = req.getHeader("X-FORWARDED-FOR");
		if(userIp == null) userIp = req.getRemoteAddr();
		
		//PropertiesUtil prop = new PropertiesUtil();
		//System.out.println("Properties===>"+prop.getPropValue("ws.log.file"));
		
		paramObj.replace("conceptId", SNOMEDCTIdentifierGenerator.create(SNOMEDCTComponentTypeEnum.CONCEPT, null));
		
		//RDF2ConceptEditor rdf2 = new RDF2ConceptEditor();
		//String path = rdf2.editor(paramObj);
		OWLEntrance owl = new OWLEntrance(userIp);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("conceptId", paramObj.get("conceptId"));
		map.put("path", owl.editor(paramObj));
		
		return map;
	}
	
	@ApiOperation(value = "Inferred")
	@RequestMapping(value = "/inferred", method = RequestMethod.POST, consumes="application/json",produces="application/json")
	public Map<String, Object> inferred(@RequestBody Map<String, Object> paramObj) throws Exception {
		
		//ELKDLQuery elk = new ELKDLQuery();
		//elk.loadInference(paramObj);
		HttpServletRequest req = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		String userIp = req.getHeader("X-FORWARDED-FOR");
		if(userIp == null) userIp = req.getRemoteAddr();
		
		OWLEntrance owl = new OWLEntrance(userIp);
		return owl.loadInference(paramObj);
		//return elk.loadInference(paramObj);
	}
	
}
