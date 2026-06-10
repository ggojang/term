package co.infoclinic.term.icd10.controller;

import java.beans.PropertyEditorSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.infoclinic.term.icd10.api.QryApi;
import co.infoclinic.term.icd10.model.dto.Icd10SearchResultDTO;
import co.infoclinic.term.icd10.service.Icd10SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * LOINC 검색 API 컨트롤러
 */
@Api(value = "Search", description = "Search", tags = QryApi.API_TAGS_SEARCH)
@RestController(value="ICD10SrchCtrl")
public class Icd10SearchController {

	/** Logger */
	Logger log = LoggerFactory.getLogger(Icd10SearchController.class);
	
	/** DI: search service */
	@Autowired
	private Icd10SearchService srchSvc;
	
	
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	
	/**
	 * 검색
	 * 
	 * @param q
	 * @param pageRequest
	 * @return
	 */
	@ApiOperation(value = "Search By Query")
	@RequestMapping(value = QryApi.API_GET_SRCH, method = RequestMethod.GET)
	public Page<Icd10SearchResultDTO> search(
		@RequestParam(value = QryApi.PARAM_Q) String q,
		//@RequestParam(value = QryApi.PARAM_PART, required = false) PartEnum part,
		@RequestParam(value = QryApi.PARAM_PAGE, required = false, defaultValue = "1") int page,
		@RequestParam(value = QryApi.PARAM_SIZE, required = false, defaultValue = "20") int size) {
		Page<Icd10SearchResultDTO> dtos = null;
		
		dtos = srchSvc.searchByWord(q, page, size);
		 
		return dtos;
	}

}
