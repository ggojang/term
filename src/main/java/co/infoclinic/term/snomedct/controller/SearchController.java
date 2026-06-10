package co.infoclinic.term.snomedct.controller;

import java.beans.PropertyEditorSupport;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.infoclinic.term.common.utils.MatchType;
import co.infoclinic.term.common.utils.SNOMEDCTUtils;
import co.infoclinic.term.common.utils.StateType;
import co.infoclinic.term.snomedct.api.QryApi;
import co.infoclinic.term.snomedct.model.dto.SearchResults;
import co.infoclinic.term.snomedct.model.dto.TermSearchResult;
import co.infoclinic.term.snomedct.service.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Search API를 제공하는 컨트롤러
 */
@Api(value = "Search", description = "Search", tags = QryApi.API_TAGS_SEARCH)
@RestController(value = "SCTSrchCtrl")
public class SearchController {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(SearchController.class);
	
	/** DI: Search Service */
	@Autowired
    private SearchService srchSvc;
	

	
	
	/**
	 * Enum의 값을 설정
	 */
	public static class EnumTypeConverter extends PropertyEditorSupport {
	  @Override
	  public void setAsText(String text) throws IllegalArgumentException {
		  // 대문자로 변환
	    setValue(text.toUpperCase());
	  }
	}
	
	
	/**
	 * InitBinder는 요청하고 Controller가 실행되기 전에 실행되는 어노테이션이다.
	 * WebDataBinder에 EnumTypeConverter를 등록한다.
	 * 즉, Enum을 String 타입으로 받아서 Enum으로 Convert한다.
	 * 
	 * @param binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
	  binder.registerCustomEditor(MatchType.class, new EnumTypeConverter());
	  binder.registerCustomEditor(StateType.class, new EnumTypeConverter());
	}
	
	
	/**
	 * SNOMED CT ID/용어 검색
	 * 
	 * @param matchType 매치유형 full, partial, regex
	 * @param stateType 상태유형 active:1, inactive:0
	 * @param q 검색어
	 * @param semFilter 시맨틱태그 필터
	 * @param pageRequest
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "Search By Query")
	@RequestMapping(value = QryApi.API_GET_SRCH_TRM, method = RequestMethod.GET)
	public SearchResults search(
		@ApiParam(value = QryApi.PARAM_MATCH_CMNT) @RequestParam(value = QryApi.PARAM_MATCH) MatchType matchType,
		@ApiParam(value = QryApi.PARAM_STATE_CMNT) @RequestParam(value = QryApi.PARAM_STATE) StateType stateType,
		@ApiParam(value = QryApi.PARAM_Q_CMNT) @RequestParam(value = QryApi.PARAM_Q) String q,
		@ApiParam(value = QryApi.PARAM_SEMFILTER_CMNT) @RequestParam(value = QryApi.PARAM_SEMFILTER, required = false) List<String> semanticFilters,
		@RequestParam(value = QryApi.PARAM_PAGE, required = false, defaultValue = "1") int page,
		@RequestParam(value = QryApi.PARAM_SIZE, required = false, defaultValue = "20") int size) throws Exception {
		if (page < 1 || size < 1) {
			return new SearchResults();
		}
					
		return srchSvc.searchTerm(matchType, stateType, semanticFilters, q, new PageRequest(page - 1, size));
	}
	
	
	/**
	 * Suggest Search; 전체 범위를 대상으로 자동완성 검색결과를 반환 하는 메소드 
	 * Narrow Suggest Search; 특정 컨셉 하위를 대상으로 자동완성 검색결과를 반환 하는 메소드 
	 * 
	 * @param q
	 * @param size
	 * @return 자동완성 검색 결과
	 */
	@ApiOperation(value = "Suggest Search By Query")
	@RequestMapping(value = QryApi.API_GET_SRCH_SGST_TRM, method = RequestMethod.GET)
	public List<TermSearchResult> suggestByQueryAndSize(
		@ApiParam(value = QryApi.PARAM_CD_CMNT) @RequestParam(value = QryApi.PARAM_CD, required = false) String code,
		@ApiParam(value = QryApi.PARAM_Q_CMNT)  @RequestParam(value = QryApi.PARAM_Q) String q,
	    @ApiParam(value = QryApi.PARAM_SIZE_CMNT) @RequestParam(value = QryApi.PARAM_SIZE, required = false, defaultValue = "100") int size) {
		List<TermSearchResult> list = null;
		
		// rangeId가 null 또는 Root일 경우
		if (code == null || SNOMEDCTUtils.PrimaryId.SnomedCTConcept.equals(code)) {
			list = srchSvc.getSuggestResultListByQueryAndSize(q, size);
		} else {
			list = srchSvc.getSuggestResultListByDescendantOrSelfIdAndQueryAndSize(code, q, size);
		}
		
		return list;
	}
	
}
