package co.infoclinic.term.snomedct.service.impl;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import co.infoclinic.term.common.utils.MatchType;
import co.infoclinic.term.common.utils.SNOMEDCTComponentTypeEnum;
import co.infoclinic.term.common.utils.SNOMEDCTUtils;
import co.infoclinic.term.common.utils.StateType;
import co.infoclinic.term.snomedct.model.dto.SearchResults;
import co.infoclinic.term.snomedct.model.dto.TermSearchResult;
import co.infoclinic.term.snomedct.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

/**
 * SNOMED CT 검색 서비스
 */
@Service("SCTSrchSvc")
public class SearchServiceImpl implements SearchService {

	/** Logger */
	Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

	/** SNOMED CT 검색 인덱스 명  */
	private static String IDX_SCT_NRRW_SRCH = "sct_nrrw_srch";
	private static String IDX_SCT_SRCH = "snomedct_search";
	
	/** DI: jest client for elasticsearch  */
	@Inject
	private JestClient client;

	@Autowired
	private SearchResultParser searchResultParser;

  /*
   * (non-Javadoc)
   * @see co.infoclinic.term.snomedct.service.SearchService#searchTerm(co.infoclinic.term.common.utils.MatchType, co.infoclinic.term.common.utils.StateType, java.util.List, java.lang.String, org.springframework.data.domain.Pageable)
   */
  @Override
  public SearchResults searchTerm(final MatchType matchType, final StateType stateType, final List<String> semanticFilter, final String q, Pageable pageable) throws Exception {

		// 검색어 : 소문자로 변환 
		final String word = q.toLowerCase();
		// 검색어의 숫자형식 여부
		boolean isNumeric = StringUtils.isNumericSpace(word);
		
		String queryStatement = null;
		String stateStatement = null;
		String termStatement = null;
		String semanticTagStatement = null;
		String sortStatement = null;

		// 활성상태에 따른 조건 추가
		
		// StateType = Active
		if (stateType.equals(StateType.ACTIVE)) {
		  stateStatement = "{\n" +
		                   "  \"script\": {\n" +
		                   "    \"script\":\n" +
		                   "      \"doc['maxConceptDescriptionEffectiveTimeAndActive'].value == (doc['conceptEffectiveTime'].value + '+' + doc['descriptionEffectiveTime'].value + '+' + '1')\"\n" +
		                   "  }\n" +
		                   "}\n";
		}
		// StateType = Inactive
		else if (stateType.equals(StateType.INACTIVE)) {
		  stateStatement = "{\n" +
		                   "  \"script\": {\n" +
		                   "    \"script\":\n" +
		                   "      \"doc['maxConceptDescriptionEffectiveTimeAndActive'].value == (doc['conceptEffectiveTime'].value + '+' + doc['descriptionEffectiveTime'].value + '+' + '0')\"\n" +
		                   "  }\n" +
		                   "}\n";
		}
		// StateType = Active + Inactive
		else if (stateType.equals(StateType.BOTH)) {
		  stateStatement = "{\n" +
		                   "  \"script\": {\n" +
		                   "    \"script\":\n" +
		                   "      \"doc['maxConceptDescriptionEffectiveTimeAndActive'].value == (doc['conceptEffectiveTime'].value + '+' + doc['descriptionEffectiveTime'].value + '+' + '1') || doc['maxConceptDescriptionEffectiveTimeAndActive'].value == (doc['conceptEffectiveTime'].value + '+' + doc['descriptionEffectiveTime'].value + '+' + '0')\"\n" +
		                   "  }\n" +
		                   "}\n";
		}

		
		// 검색어가 숫자형식인 경우
		if (isNumeric) {
		  if (SNOMEDCTComponentTypeEnum.CONCEPT.equals(SNOMEDCTComponentTypeEnum.getById(word))) {
		    termStatement = "{\n" +
		                     "  \"term\": {\n" +
		                     "    \"conceptId\": \"" + word + "\"\n" +
		                     "  }\n" +
		                     "}";
		  } else if (SNOMEDCTComponentTypeEnum.DESCRIPTION.equals(SNOMEDCTComponentTypeEnum.getById(word))) {
		    termStatement = "{\n" +
		                     "  \"term\": {\n" +
		                     "    \"descriptionId\": \"" + word + "\"\n" +
		                     "  }\n" +
		                     "}";
		  } else {
		    log.error("SnomedCtComponentTypeEnum - Not Supported Type : " + word);
		    isNumeric = false;
		  }
		}
		
		// 검색어가 숫자형식이 아닐 경우
		if (!isNumeric) {
		  // MatchType = Partial(부분일치)
		  if (matchType.equals(MatchType.PARTIAL)) {
		    // q를 공백을 기준으로 구분하여 배열을 구성
		    List<String> queryPieceList = Arrays.asList(word.split(" "));
		    int queryPieceListSize = queryPieceList.size();
		    int queryPieceListTotalLength = 0;
		    double queryPieceLengthAverage = 0;
		    for (int i = 0; i < queryPieceListSize; i++) {
		      queryPieceListTotalLength += queryPieceList.get(i).length();
		    }
		    // 각 쿼리 조각별 길이 총합에 대한 평균 길이를 계산
		    queryPieceLengthAverage = queryPieceListTotalLength / queryPieceListSize;
		    
		    
		    // if stop_edge2 : 각각의 길이가 2이면 term.stop_edge2 필드를 사용하도록 한다.
		    // if stop_edge3 : 각각의 길이가 3이면 term.stop_edge2 필드를 사용하도록 한다.
		    if (queryPieceLengthAverage == 2 || queryPieceLengthAverage == 3) {
		      String field = "term.stop_edge" + ((int) queryPieceLengthAverage);
		      /*
		       * if stop_edge2 { "match_phrase": { "term.stop_edge2": { "query": {q} } } }
		       * 
		       * if stop_edge3 { "match_phrase": { "term.stop_edge3": { "query": {q} } } }
		       */
		      termStatement = "{\n" +
		                       "  \"match_phrase\": {\n" +
		                       "    \"" + field + "\": {\n" +
		                       "      \"query\": \"" + q + "\"\n" +
		                       "    }\n" +
		                       "  }\n" +
		                       "}\n";
		    } 
		    // term 필드를 사용한다.
		    else { 
		      termStatement = "";
		      for (int i = 0; i < queryPieceListSize; i++) {
		        termStatement += "{\n" +
		                         "  \"regexp\": {\n" +
		                         "    \"term\": \"@" + queryPieceList.get(i) + "@\"\n" +
		                         "  }\n" +
		                         "}";
		        if (i != queryPieceList.size() - 1) {
		          termStatement += ",";
		        }
		        termStatement += "\n";
		      }
		    }
		  }
		  // MatchType = Regular Expression(정규표현식)
		  else if (matchType.equals(MatchType.REGEX)) {
		    // 매치타입이 '정규식'일 경우
		    termStatement = "{\n" +
		                      "  \"regexp\": {\n" +
		                      "    \"term.raw_lc\": \"" + word + "\"\n" +
		                      "  }\n" +
		                      "}\n";
		  }
		  // MatchType = Full Text(전체일치)
		  else if (matchType.equals(MatchType.FULLTEXT)) {
		    // 매치타입이 '완전일치'일 경우
		    termStatement = "{\n" +
		                     "  \"term\": {\n" +
		                     "    \"term.raw_lc\": \"" + word + "\"\n" +
		                     "  }\n" +
		                     "}\n";
		  } else {
		    log.error("Implement Match Type : " + matchType);
		  }
		}
		
		if (semanticFilter != null) {
		  semanticTagStatement = "{\n" +
		                         "  \"terms\": {\n" +
		                         "    \"semanticTag\": [\n";
		  int semanticFilterSize = semanticFilter.size();
		  for (int i = 0; i < semanticFilterSize; i ++) {
		    semanticTagStatement += "\"" + semanticFilter.get(i) + "\"";
		    if (i < semanticFilterSize - 1) {
		      semanticTagStatement += ","; 
		    }
		  }
		  semanticTagStatement +="    ]\n" +
		                         "  }\n" +
		                         "}\n";
		}
		
		// sort statement
		List<String> sortList = new ArrayList<String>();
		sortList.add("length");
		sortList.add("term.raw_lc");
		sortStatement = "";
		int sortListSize = sortList.size();
		for (int i = 0; i < sortListSize; i++) {
		  sortStatement += "{\n" +
		                   "  \"" + sortList.get(i) + "\": \"asc\"" +
		                   "}\n";
		  if (i < sortListSize - 1) {
		    sortStatement += ",\n";
		  }
		}
		
		///////////////
		
		List<String> filterList = new ArrayList<String>();
		if (stateStatement != null) {
		  filterList.add(stateStatement);
		}
		if (termStatement != null) {
		  filterList.add(termStatement);
		}
		if (semanticTagStatement != null) {
		  filterList.add(semanticTagStatement);
		}
		
		queryStatement = "{\n" +
		                 "  \"query\": {\n" +
		                 "    \"bool\": {\n" +
		                 "      \"filter\": [\n";
		// add stateStatement + termStatement + semanticTagStatement
		int filterListSize = filterList.size();
		for (int i = 0; i < filterListSize; i++) {
		  queryStatement += filterList.get(i);
		  if (i < filterListSize - 1) {
		    queryStatement += ",\n";
		  }
		}
		queryStatement +="      ]\n" +
		                 "    }\n" +
		                 "  },\n" +
		                 "  \"size\": " + pageable.getPageSize() + ",\n" +
		                 "  \"from\": " + pageable.getOffset()  + ",\n" +
		                 "  \"sort\": [\n";
		// add sortStatement
		queryStatement += sortStatement;
		// add aggregation for semanticTag
		queryStatement +="  ],\n" +
		                 "  \"aggs\": {\n" +
		                 "    \"semanticTags\": {\n" +
		                 "      \"terms\": {\n" +
		                 "        \"field\": \"semanticTag\",\n" +
		                 "        \"size\": 1000\n" +
		                 "      }\n" +
		                 "    }\n" +
		                 "  }\n" +
		                 "}";
		
		Search search = new Search.Builder(queryStatement).addIndex(IDX_SCT_SRCH).build();
		SearchResults results = new SearchResults();
		
		Page<TermSearchResult> page = null;
		SearchResult searchResult = null;
		try {
		  searchResult = client.execute(search);

		  if (searchResult == null || !searchResult.isSucceeded()) {
		    page = new PageImpl<TermSearchResult>(new ArrayList<TermSearchResult>());
		    results.setPage(page);
		  } else {
		    JsonObject hits = searchResult.getJsonObject().get("hits").getAsJsonObject();
		    int total = hits.get("total").getAsInt();
		    if (total == 0) {
		      page = new PageImpl<TermSearchResult>(new ArrayList<TermSearchResult>());
		      results.setPage(page);
		    } else {
		      results = searchResultParser.parseResults(searchResult, pageable);
		    }
		  }
		} catch (SocketTimeoutException e) {
		  log.error(e.getMessage(), e);
		}
		return results;
  }

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.SearchService#getValueListByMrcmAttr(java.lang.String, java.lang.String)
	 */
	@Override
	public List<TermSearchResult> getValueListByMrcmAttr(String attr, String q, int size) {
		if (attr == null || q == null || !StringUtils.isNumeric(attr)) {
		      return new ArrayList<TermSearchResult>();
	    }
		 
		// 사용하는 변수들
	    String query = null;
	    Search srch = null;
	    SearchResult srchRslt = null;
	    List<TermSearchResult> trmSrchRslts = new ArrayList<TermSearchResult>();
	    TermSearchResult trmSrchRslt = null;
	    List<String> queryPieceList = Arrays.asList(q.toLowerCase().split(" "));
	    int queryPieceListSize = queryPieceList.size();
		    
	    query = "{\n" +
	        "   \"query\" : { \n" +
	        "     \"bool\" : { \n" +
	        "       \"must\" : [ \n" +
	        "          { \n" +
	        "           \"wildcard\": { \n" +
	        "              \"mrcmAttrs\": \"*" + attr +  "*\"\n" +
	        "            }" +
	        "          }";
	    if (queryPieceListSize > 0) {
	    	query += ", \n";
	    }
	    
	    if (!StringUtils.isNumeric(q)) {
	    	for (int i = 0; i < queryPieceListSize; i++) {
		    	query += "  {\n" +
		    			 "    \"term\" : { \n" +
		    	         "       \"term.autocomplete\" : \"" + queryPieceList.get(i) + "\" \n" +
		    	         "    }\n" +
		    	         "  }";
		    	if (i < queryPieceListSize - 1) {
		    		query += ",\n";
		    	}
		    }
	    }
	    
	       
	    query += "         ],\n" +
	        " \"filter\": [\n" +
			"{\n" +
	        "  \"script\": {\n" +
	        "    \"script\":\n" +
	        "      \"doc['maxConceptDescriptionEffectiveTimeAndActive'].value == (doc['conceptEffectiveTime'].value + '+' + doc['descriptionEffectiveTime'].value + '+' + '1')\"\n" +
	        "  }\n" +
	        "}\n" +
	        "]\n" +
	        "     }\n" +
	        "   }, \n" +
	        "   \"size\": " + size + ",\n" +
	        "   \"sort\": \"length\"\n" +
	        "}";
		    srch = new Search.Builder(query).addIndex(IDX_SCT_SRCH).build();
		    try {
		      srchRslt = client.execute(srch);
		      if (srchRslt == null) {
		        return new ArrayList<TermSearchResult>();
		      }
		      JsonObject jo = srchRslt.getJsonObject();
		      JsonArray hits = jo.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
		      int hitsSize = hits.size();
		      JsonObject hit = null;
		      for (int i = 0; i < hitsSize; i++) {
		        hit = hits.get(i).getAsJsonObject().get("_source").getAsJsonObject();
		        trmSrchRslt = new TermSearchResult();
		        
		        // Concept 활성화 여부
		        trmSrchRslt.setConceptActive("0".equals(hit.get("conceptActive").getAsInt()) ?  false:true);
		        // Description 활성화 여부 
		        trmSrchRslt.setDescriptionActive("0".equals(hit.get("descriptionActive").getAsInt()) ?  false:true);
		        // 용어
		        trmSrchRslt.setTerm(hit.get("term").getAsString());
		        // 대표 용어
		        trmSrchRslt.setFsn(hit.get("fsn").getAsString());
		        // 용어의 길이
		        trmSrchRslt.setLength(hit.get("length").getAsInt());
		        // 용어의 언어
		        trmSrchRslt.setLang(hit.get("lang").getAsString());
		        // Concept 식별자
		        trmSrchRslt.setConceptId(hit.get("conceptId").getAsString());
		        // Description 식별자
		        trmSrchRslt.setDescriptionId(hit.get("descriptionId").getAsString());
		        // Acceptability 식별자 20200701 by Yu
		        trmSrchRslt.setAcceptabilityId(hit.get("acceptabilityId").getAsString());
		        
		        trmSrchRslts.add(trmSrchRslt);
		      }
		    } catch (Exception e) {
		      log.error(e.getMessage(), e);
		    }
		    
		    // 결과 반환
		    return trmSrchRslts;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.SearchService#getSuggestResultListByQueryAndSize(java.lang.String, int)
	 *
	 * <pre>
	 * 
	 * Example :
	 *  {
	 *  	"took": 12,
	 *  	"timed_out": false,
	 *  	"_shards": {
	 *  		"total": 5,
	 *  		"successful": 5,
	 *  		"failed": 0
	 *  	},
	 *  	"hits": {
	 *  		"total": 1959310,
	 *  		"max_score": 1,
	 *  		"hits": [
	 *  			{
	 *  				"_index": "sct_srch",
	 *  				"_type": "sct_srch_type",
	 *  				"_id": "AVbBrgVwPTUYhd-nXqzV",
	 *  				"_score": 1,
	 *  				"_source": {
	 *  					"@timestamp": "2016-08-25T12:30:22.550Z",
	 *  					"conceptId": "133861000",
	 *  					"descriptionId": "213591013",
	 *  					"componentEffectiveTime": "20020131",
	 *  					"conceptEffectiveTime": "20020131",
	 *  					"descriptionEffectiveTime": "20020131",
	 *  					"maxConceptDescriptionEffectiveTimeAndActive": "20030131+20020131+1",
	 *  					"componentActive": "1",
	 *  					"conceptActive": "1",
	 *  					"descriptionActive": "1",
	 *  					"term": "Open reduction and fixation",
	 *  					"semanticTag": "procedure",
	 *  					"fsn": "Open reduction and fixation (procedure)",
	 *  					"primitive": "1",
	 *  					"type": "3",
	 *  					"acceptabilityId": 900000---- 20200701 by Yu
	 *  					"length": "27",
	 *  					"lang": "en",
	 *  					"mrcmAttrs": "+47429007+118171006+363702006+418775008+246090004+363589002+255234002"
	 *  				}
	 *  			}
	 *  		]
	 *  	}
	 *  }
	 * 
	 * </pre>
	 *
	 */
	@Override
	  public List<TermSearchResult> getSuggestResultListByQueryAndSize(String q, int size) {
	    if (q == null || q.length() == 0) {
	      return new ArrayList<TermSearchResult>();
	    }
	    
    	// FIXME: numeric & digit 6~18 ? true
 		boolean isNumeric = StringUtils.isNumeric(q);
 		boolean isSctId = false;
 		boolean isCnptTyp = false;
 		String cpntTyp = null;
 		
 		// 숫자타입 이면서 6~18자리라면 id 아니면 term
 		if (isNumeric) {
 			int num = (int)(Math.log10(Integer.parseInt(q))+1);
 			if (num > 5 && num < 19) {
 				isSctId = true;
 				
 				// SNOMED CT Component Type
 				SNOMEDCTComponentTypeEnum sctTyp = SNOMEDCTComponentTypeEnum.getById(q);
 				
 				// Concept
 				if (SNOMEDCTComponentTypeEnum.CONCEPT.equals(sctTyp)) {
 					isCnptTyp = true;
 				}
 				
 				cpntTyp = sctTyp.name().toLowerCase();
 			}
 		}
	    
	    String query = null;
	    Search srch = null;
	    SearchResult srchRslt = null;
	    List<TermSearchResult> trmSrchRslts = new ArrayList<TermSearchResult>();
	    TermSearchResult trmSrchRslt = null;
	    List<String> queryPieceList = Arrays.asList(q.toLowerCase().split(" "));
	    int queryPieceListSize = queryPieceList.size();
	    
	    query = "{\n" +
	        "   \"query\" : { \n" +
	        "     \"bool\" : { \n" +
	        "       \"must\" : [ \n";
	    
	    if (isSctId) {
	    	query += "         {\n" +
				     "          \"term\" : { \n" +
				     "            \"" + cpntTyp + "Id\" : \"" + q + "\" \n" +
					 "            }\n" +
				     "         }\n";
	    } else {
	    	for (int i = 0; i < queryPieceListSize; i++) {
		    	query += "  {\n" +
		    			 "    \"term\" : { \n" +
		    	         "       \"term.autocomplete\" : \"" + queryPieceList.get(i) + "\" \n" +
		    	         "    }\n" +
		    	         "  }";
		    	if (i < queryPieceListSize - 1) {
		    		query += ",\n";
		    	}
		    }
	    }
	    
	       
	    query += "         ],\n" +
	        " \"filter\": [\n" +
			"{\n" +
	        "  \"script\": {\n" +
	        "    \"script\":\n" +
	        "      \"doc['maxConceptDescriptionEffectiveTimeAndActive'].value == (doc['conceptEffectiveTime'].value + '+' + doc['descriptionEffectiveTime'].value + '+' + '1')\"\n" +
	        "  }\n" +
	        "}\n" +
	        "]\n" +
	        "     }\n" +
	        "   }, \n" +
	        "   \"size\": " + size + ",\n" +
	        "   \"sort\": \"length\"\n" +
	        "}";
	    
	    srch = new Search.Builder(query).addIndex(IDX_SCT_SRCH).build();
	    try {
	      srchRslt = client.execute(srch);
	      if (srchRslt == null) {
	        return new ArrayList<TermSearchResult>();
	      }
	      JsonObject jo = srchRslt.getJsonObject();
	      JsonArray hits = jo.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
	      int hitsSize = hits.size();
	      JsonObject hit = null;
	      for (int i = 0; i < hitsSize; i++) {
	        hit = hits.get(i).getAsJsonObject().get("_source").getAsJsonObject();
	        
	        int primitive = hit.get("primitive").getAsInt();
	        
	        trmSrchRslt = new TermSearchResult();
	        
	        // set term
	        trmSrchRslt.setTerm(hit.get("term").getAsString());
	        // set fsn
	        trmSrchRslt.setFsn(hit.get("fsn").getAsString());
	        // set concept id
	        trmSrchRslt.setConceptId(hit.get("conceptId").getAsString());
	        // set definition status id
	        trmSrchRslt.setDefinitionStatusId(primitive == 1 ? SNOMEDCTUtils.DefinitionStatus.Primitive:SNOMEDCTUtils.DefinitionStatus.Defined);
	        // set acceptability id 20200701 by Yu
	        trmSrchRslt.setAcceptabilityId(hit.get("acceptabilityId").getAsString());
	        
	        trmSrchRslts.add(trmSrchRslt);
	      }
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	    
	    return trmSrchRslts;
	  }

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.SearchService#getSuggestResultListByDescendantOrSelfIdAndQueryAndSize(java.lang.String, java.lang.String, int)
	 */
	@Override
	public List<TermSearchResult> getSuggestResultListByDescendantOrSelfIdAndQueryAndSize(String descendantOrSelfId, String q, int size) {
		// 검색 범위를 한정하는 식별자 또는 쿼리에 대한 유효성 검사
		// SCTID 규칙을 따르지 않는 경우 반환
		if (q == null || !SNOMEDCTComponentTypeEnum.isValidIdentifier(descendantOrSelfId)) {
		      return new ArrayList<TermSearchResult>();
	    }
		 
		// ========================================
		// 변수 선언
		// ========================================
		List<TermSearchResult> rslts = new ArrayList<TermSearchResult>();
	    TermSearchResult rslt;
	    
	    // Elasticsearch에 쿼리를 보내고 결과를 받는 객체 선언
	    Search srchObj;
	    SearchResult srchRsltObj;
	    
	    List<String> qrys;
	    int qrysSize;
	    
	    boolean isNum = false;
	    
	    String query, qprtWildcard, qprtTerm;
	    // ========================================
	    
	    // 문자열 내에 존재하는 모든 화이트스페이스 구분자(\\s+)를 기준으로 분리 , \\s+는 [ \\t\\n\\x0B\\f\\r]와 동치
	    // 참고: http://stackoverflow.com/questions/225337/how-do-i-split-a-string-with-any-whitespace-chars-as-delimiters
	    qrys = Arrays.asList(q.toLowerCase().split("\\s+"));
	    // 분리된 문자를 담은 qrys의 크기
	    qrysSize = qrys.size();
	    
	   // descendantOrSelfId의 elasticsearch wildcard 쿼리
	    qprtWildcard = "{\n" +
					   " \"wildcard\": {\n" +
					   "  \"ancestorPath\": \"*" + descendantOrSelfId + "*\"\n" +
					   " }\n" +
					   "},\n";
	    
	  
	   // 분리된 쿼리 문자의 elasticsearch term 쿼리 
	    if (qrysSize == 1 && StringUtils.isNumeric(q)) {
	    	SNOMEDCTComponentTypeEnum idTyp = SNOMEDCTComponentTypeEnum.getById(q);
	    	if (SNOMEDCTComponentTypeEnum.UNKNOWN.equals(idTyp) || SNOMEDCTComponentTypeEnum.RELATIONSHIP.equals(idTyp)) {
	    		log.error("ERROR SNOMEDCT Component Type");
	    	}
	    	
	    	qprtTerm = "{\n" +
				    " \"term\": {\n";
	    	if (SNOMEDCTComponentTypeEnum.CONCEPT.equals(idTyp)) {
	    		qprtTerm += "  \"descriptions.cid\": \"" + q + "\"\n";
	    	} else if (SNOMEDCTComponentTypeEnum.DESCRIPTION.equals(idTyp)) {
	    		qprtTerm += "  \"descriptions.id\": \"" + q + "\"\n";
	    	}
	    	qprtTerm += "  }\n" +
	    			   "}";
	    	
	    	isNum = true;
	    } else {
	    	qprtTerm = "";
	    	for (int i = 0; i < qrysSize; i++) {
		    	qprtTerm += "{" +
						    " \"term\": {" +
						    "  \"descriptions.term.autocomplete\": \"" + qrys.get(i) + "\"" +
						    "  }" +
						    "}";
				   
			   // loop의 index인 i가 마지막이 아니라면 
			   if (i != qrysSize - 1) {
				   qprtTerm += ",";
			   }
		   }
	    }
	    
	   query = "{" +
			   "  \"query\": {" +
			   "    \"bool\": {" +
			   "      \"must\": [" +
			   qprtWildcard + // descendantOrSelfId의 elasticsearch wildcard 쿼리
			   "        {" +
			   "          \"nested\": {" +
			   "            \"path\": \"descriptions\"," +
			   "            \"query\": {" +
			   "              \"bool\": {" +
			   "                \"must\": [" +
			   qprtTerm +
			   "                ]" +
			   "              }" +
			   "            }" +
			   "          }" +
			   "        }" +
			   "      ]" +
			   "    }" +
			   "  }," +
			   "  \"size\": 1000," +
			   "  \"aggs\": {" +
			   "    \"nested_ds\": {" +
			   "      \"nested\": {" +
			   "        \"path\": \"descriptions\"" +
			   "      }," +
			   "      \"aggs\": {" +
			   "        \"n_ds_filter\": {" +
			   "          \"filter\": {" +
			   "            \"bool\": {" +
			   "              \"must\": [" +
			   qprtTerm + // 분리된 쿼리 문자의 elasticsearch term 쿼리
			   "              ]" +
			   "            }" +
			   "          }," +
			   "          \"aggs\": {" +
			   "            \"descs\": {" +
			   "              \"terms\": {" +
			   "                \"field\": \"descriptions.term.raw\"," +
			   "                \"script\": \"_value\"," +
			   "                \"order\": {" +
			   "                  \"term_stats\": \"asc\"" +
			   "                }," +
			   "                \"size\": " + size + "" + // 검색 결과 중 가져올 최대 갯수
			   "              }," +
			   "              \"aggs\": {" +
			   "                \"term_stats\": {" +
			   "                  \"min\": {" +
			   "                    \"script\": \"doc['descriptions.term.raw_lc'].value.length()\"" +
			   "                  }" +
			   "                }," +
			   "                \"dedup\": {" +
			   "                  \"top_hits\": {" +
			   "                    \"size\": 1" +
			   "                  }" +
			   "                }," +
			   "                \"rev\": {" +
			   "                  \"reverse_nested\": {}," +
			   "                  \"aggs\": {" +
			   "                    \"cid\": {" +
			   "                      \"terms\": {" +
			   "                        \"field\": \"conceptId\"" +
			   "                      }" +
			   "                    }," +
			   "                    \"fsn\": {" +
			   "                      \"terms\": {" +
			   "                        \"field\": \"fsn\"" +
			   "                      }" +
			   "                    }" +
			   "                  }" +
			   "                }" +
			   "              }" +
			   "            }" +
			   "          }" +
			   "        }" +
			   "      }" +
			   "    }" +
			   "  }" +
			   "}";
	  
		    srchObj = new Search.Builder(query).addIndex(IDX_SCT_NRRW_SRCH).build();
		    try {
		      srchRsltObj = client.execute(srchObj);
		      if (srchRsltObj == null) {
		        return new ArrayList<TermSearchResult>();
		      }
		      
		      // 결과에서 필요한 데이터는 얻는 경로
		      // 1. ConceptId: aggregations/nested_ds/n_ds_filter/descs/buckets[n]/rev/cid/buckets[0].key
		      // 2. 일치하는 용어: aggregations/nested_ds/n_ds_filter/descs/buckets[n]/key
		      // 3. Description 객체: aggregations/nested_ds/n_ds_filter/descs/buckets[n]/dedup/hits/hits[0]/_source/{id,type,lang,term}
		      
		      JsonObject root = srchRsltObj.getJsonObject();
		      JsonArray buckets = root
							      	.get("aggregations").getAsJsonObject()
							      	.get("nested_ds").getAsJsonObject()
							      	.get("n_ds_filter").getAsJsonObject()
							      	.get("descs").getAsJsonObject()
							      	.get("buckets").getAsJsonArray();
		      
		      int bucketsSize = buckets.size();
		      JsonObject bucket, descSrc;
		      String cId, fsn, descId, descTyp, descLang, descTrm, acceptId; //20200701 by Yu
		      
		      for (int i = 0; i < bucketsSize; i++) {
		    	  // n번째 bucket
		    	  bucket = buckets.get(i).getAsJsonObject();
		    	  
		    	  // get conceptId
		    	  cId = bucket
				  		.get("rev").getAsJsonObject()
				  		.get("cid").getAsJsonObject()
				  		.get("buckets").getAsJsonArray()
				  		.get(0).getAsJsonObject()
				  		.get("key").getAsString();
		    	  
		    	  // get fsn
		    	  fsn = bucket
					  		.get("rev").getAsJsonObject()
					  		.get("fsn").getAsJsonObject()
					  		.get("buckets").getAsJsonArray()
					  		.get(0).getAsJsonObject()
					  		.get("key").getAsString();
		    	  
		    	  // get description object
		    	  descSrc = bucket
					  		.get("dedup").getAsJsonObject()
					  		.get("hits").getAsJsonObject()
					  		.get("hits").getAsJsonArray()
					  		.get(0).getAsJsonObject()
					  		.get("_source").getAsJsonObject();
		    	  
		    	  // description id
		    	  descId = descSrc.get("id").getAsString();
		    	  // description type
		    	  descTyp = descSrc.get("type").getAsString();
		    	  // description lang
		    	  descLang = descSrc.get("lang").getAsString();
		    	  // description term
		    	  descTrm = descSrc.get("term").getAsString();
		    	  // acceptability id 20200701 by Yu
		    	  acceptId = descSrc.get("acceptabilityId").getAsString();
		    	  
		        rslt = new TermSearchResult();
		        
		        rslt.setConceptActive(true);
		        rslt.setDescriptionActive(true);
		        
		        // 대표용어
		        rslt.setFsn(fsn);
		        // 용어
		        rslt.setTerm(descTrm);
		        // 용어의 길이 
		        rslt.setLength(descTrm.length());
		        // 용어의 언어
		        rslt.setLang(descLang);
		        // Concept 식별자
		        rslt.setConceptId(cId);
		        // Description 식별자
		        rslt.setDescriptionId(descId);
		        // Acceptability 식별자 20200701 by Yu
		        rslt.setAcceptabilityId(acceptId);
		        
		        rslts.add(rslt);
		      }
		    } catch (Exception e) {
		      log.error(e.getMessage(), e);
		    }
		    
		    // 결과 반환
		    return rslts;
	}

	/**
	 * 인덱스에 쿼리한 결과를 반환하는 메소드
	 */
	@Override
	public SearchResult getSearchResult(String idx, String q) {

		// ========================================
		// 변수 선언 및 초기화
		// ========================================

		// Elasticsearch에 쿼리를 보내고 결과를 받는 객체 선언
		Search srch = new Search.Builder(q).addIndex(idx).build();
		SearchResult srchRslt = null;

		// 검색 수행
		try {
			srchRslt = client.execute(srch);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 검색 결과가 널이면 반환
		if (srchRslt == null) {
			return new SearchResult(new Gson());
		}

		// TODO Auto-generated method stub
		return srchRslt;
	}

}
