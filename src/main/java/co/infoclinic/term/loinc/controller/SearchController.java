package co.infoclinic.term.loinc.controller;

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

import co.infoclinic.term.loinc.api.QryApi;
import co.infoclinic.term.loinc.model.dto.SearchResultDTO;
import co.infoclinic.term.loinc.service.SearchService;
import co.infoclinic.term.loinc.utils.PartEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * LOINC 검색 API 컨트롤러
 */
@Api(value = "Search", description = "Search", tags = QryApi.API_TAGS_SEARCH)
@RestController(value="LNCSrchCtrl")
public class SearchController {

	/** Logger */
	Logger log = LoggerFactory.getLogger(SearchController.class);
	
	/** DI: search service */
	@Autowired
	private SearchService srchSvc;
	
	
	
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
	@ApiOperation(value = "Search")
	@RequestMapping(value = QryApi.API_GET_SRCH, method = RequestMethod.GET)
	public Page<SearchResultDTO> search(
		@RequestParam(value = QryApi.PARAM_Q) String q,
		@RequestParam(value = QryApi.PARAM_PART, required = false) PartEnum part,
		@RequestParam(value = QryApi.PARAM_PAGE, required = false, defaultValue = "1") int page,
		@RequestParam(value = QryApi.PARAM_SIZE, required = false, defaultValue = "20") int size) {
		Page<SearchResultDTO> dtos = null;
		
		if (part == null) {
			dtos = srchSvc.searchByWord(q, page, size);
		} else {
			String field = part.name().toLowerCase();
			dtos = srchSvc.searchByWordAndField(q, field, page, size);
		}
		 
		return dtos;
	}
	
	
	
	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------
	
	
	/**
	 * InitBinder는 요청하고 Controller가 실행되기 전에 실행되는 어노테이션이다.
	 * WebDataBinder에 EnumTypeConverter를 등록한다.
	 * 즉, Enum을 String 타입으로 받아서 Enum으로 Convert한다.
	 * 
	 * @param binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
	  binder.registerCustomEditor(PartEnum.class, new EnumTypeConverter());
	}
	
	
	
	
	// ----------------------------------------
	// Private classes
	// ----------------------------------------
	
	public static class EnumTypeConverter extends PropertyEditorSupport {
	  @Override
	  public void setAsText(String text) throws IllegalArgumentException {
		  setValue(text.toUpperCase());
	  }
	}
	
}
