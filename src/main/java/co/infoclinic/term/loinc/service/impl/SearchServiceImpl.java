package co.infoclinic.term.loinc.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import co.infoclinic.term.loinc.model.dto.SearchResultDTO;
import co.infoclinic.term.loinc.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

/**
 * LOINC Search Service
 */
@Service("LncSrchSvc")
public class SearchServiceImpl implements SearchService {

	/** Logger */
	Logger log = LoggerFactory.getLogger(SearchService.class);
	
	/** LOINC 검색 인덱스 명  */
	private static final String IDX_SRCH = "loinc_search";
	
	/** DI: jest client for elasticsearch  */
	@Inject
	private JestClient client;
	
	
	// ----------------------------------------
	// Public Methods
	// ----------------------------------------
	// searchByWord       : 코드 또는 FSN의 일부 용어에 대한 검색결과를 반환
	// searchByWordAndPart: 특정 파트의 코드 또는 용어에 대한 검색결과를 반환
	
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.service.SearchService#searchByWord(java.lang.String, int, int)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Page<SearchResultDTO> searchByWord(String word, int page, int size) {
		if (StringUtils.isEmpty(word) || page < 1 || size < 1) {
			return new PageImpl<SearchResultDTO>(new ArrayList<SearchResultDTO>());
		}
		
		// ----------------------------------------
		// 1. 지역변수 선언 및 초기화
		// ----------------------------------------
		Page<SearchResultDTO> rslts;
		List<SearchResultDTO> dtos;
		boolean isCd = false;
		String qry = "";
		SearchResult srchRslt = null;
		int total;
		int offset = (page - 1) * size;
		int limit = size;
		
		// ----------------------------------------
		// 2.검색어 분석 - 코드, 용어 구분
		// ----------------------------------------
		isCd = isCode(word);
		
		// ----------------------------------------
		// 3. 쿼리 생성
		// ----------------------------------------
		
		// 분석결과 코드일 경우: 쿼리 대상 필드는 'CODE'
		if (isCd) {
			qry = getCodeSearchQuery(word);
		} else {
			// 분석결과 용어일 경우: 쿼리 대상 필드는 'FSN, COMPONENT, PROPERTY, TIME, SYSTEM, SCALE, METHOD'
			qry = getFsnSearchQuery(word, offset, limit);
		}
		
		// ----------------------------------------
		// 4. 쿼리 수행
		// ----------------------------------------
		srchRslt = query(IDX_SRCH, qry);
		
		//log.debug("IDX: " + IDX_SRCH + "\n" + qry);
		
		// ----------------------------------------
		// 5. 반환 데이터 생성
		// ----------------------------------------
		dtos = srchRslt.getSourceAsObjectList(SearchResultDTO.class);
		total = srchRslt.getTotal();
		rslts = new PageImpl<>(dtos, new PageRequest(page - 1, size), total);
		
		
		// ----------------------------------------
		// 6. 반환
		// ----------------------------------------
		return rslts;
	};
	
	
	/**
	 * 검색 - 특정 필드의 검색결과를 반환
	 * 
	 * @param q
	 * @param field
	 * @param page
	 * @param size
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Page<SearchResultDTO> searchByWordAndField(String word, String field, int page, int size) {
		if (StringUtils.isEmpty(word) || page < 1 || size < 1) {
			return new PageImpl<SearchResultDTO>(new ArrayList<SearchResultDTO>());
		}
		
		// ----------------------------------------
		// 1. 지역변수 선언 및 초기화
		// ----------------------------------------
		Page<SearchResultDTO> rslts;
		List<SearchResultDTO> dtos;
		String qry = "";
		SearchResult srchRslt = null;
		int total;
		int offset = (page - 1) * size;
		int limit = size;
		
		// ----------------------------------------
		// 2. 쿼리 생성
		// ----------------------------------------
		
		qry = getSearchQuery(word, field, offset, limit);
		
		// ----------------------------------------
		// 3. 쿼리 수행
		// ----------------------------------------
		srchRslt = query(IDX_SRCH, qry);
		
		log.debug("IDX: " + IDX_SRCH + "\n" + qry);
		
		// ----------------------------------------
		// 4. 반환 데이터 생성
		// ----------------------------------------
		dtos = srchRslt.getSourceAsObjectList(SearchResultDTO.class);
		total = srchRslt.getTotal();
		rslts = new PageImpl<>(dtos, new PageRequest(page - 1, size), total);
		
		// ----------------------------------------
		// 5. 반환
		// ----------------------------------------
		return rslts;
	}
	
	
	// ----------------------------------------
	// Private Methods
	// ----------------------------------------
	// isCode:             LOINC의 코드가 맞는지 확인
	// getCodeSearchQuery: 코드 검색 쿼리 반환
	// getTermSearchQuery: 용어 검색 쿼리 반환
	
	
	/**
	 * LOINC 코드 여부
	 * 
	 * LOINC 코드 규칙: nnnnnnnn-n (최소 3자리 ~ 최대 10자리)
	 * 
	 * 코드: 검색어가 10자리 이하이면서, 대시(-)가 포함되어 있고 대시(-)뒤 한자리 숫자인 경우
	 * 용어: 나머지
	 * 
	 * @param q
	 * @return
	 */
	private boolean isCode(String q) {
		boolean valid = false;
		
		// 매개변수 q가 널 값이 아닌 경우만 처리
		if (q != null) {
			// q의 길이
			int len = q.length();
			// 대시(-) 포함 여부
			boolean isContainDash = q.contains("-");
			
			// q의 길이가 3이상 10이하이면서, 대시(-) 포함
			if ((len >= 3 && len <= 10) && isContainDash) {
				
				// q의 뒤에서 두번째 문자
				String lastSecondChar = q.substring(len - 2, len - 1);
				// q의 마지막 문자
				String lastChar = q.substring(len - 1);
				
				// q의 뒤에서 두번째 문자가 대시(-)이면서, 마지막 문자의 타입이 숫자인 경우 => LOINC Code
				valid = "-".equals(lastSecondChar) && StringUtils.isNumeric(lastChar) ? true:false;
			}
		}
		
		return valid;
	}
	
	
	/**
	 * 특정 필드 검색 쿼리 반환
	 * 
	 * @param word
	 * @param field
	 * @param pageRequest
	 * @return
	 */
	private String getSearchQuery(String word, String field, int offset, int limit) {
		String qry = "";
		
		/*
		Query
		----------------------------------------
		{
		  "query": {
		    "bool": {
		      "filter": [
		        {
		          "term": {
		            "{매개변수 field}.autocomplete": {매개변수 word (소문자)}
		          }
		        }
		      ]
		    }
		  },
		  "size": 0,
		  "from": 0
		}
		----------------------------------------
		*/
		
		qry = "{\n" +
			  "  \"query\": { \n" +
			  "    \"bool\": { \n" +
			  "      \"filter\": [ \n" +
			  "        { \n" +
			  "          \"term\": { \n" +
//			  "            \"" + field + ": \"" + word.toLowerCase() + "\"\n" +
			  "            \"" + field + ".autocomplete\": \"" + word.toLowerCase() + "\"\n" + // 2020.07.28 by Yu
			  "          } \n" +
			  "        } \n" +
			  "      ] \n" +
			  "    } \n" +
			  "  }, \n" +
			  "  \"size\": " + limit + ",\n" +
			  "  \"from\": " + offset + "\n" + 
			  "}";
		
		return qry;
	}
	
	
	/**
	 * 코드 검색 쿼리 반환
	 * 
	 * @param code
	 * @return
	 */
	private String getCodeSearchQuery(String code) {
		String qry = "";
		
		/*
		Query
		----------------------------------------
		{
		  "query": {
		    "bool": {
		      "filter": {
		        "term": {
		          "code": {매개변수 q (대문자)}
		        }
		      }
		    }
		  }
		}
		----------------------------------------
		*/
		
		qry = "{\n" +
			  "  \"query\": { \n" +
			  "    \"bool\": { \n" +
			  "      \"filter\": { \n" +
			  "        \"term\": { \n" +
//			  "	   \"match\": { \n" + // added by Yu
			  "          \"code\": \"" + code.toUpperCase() + "\"\n" +
			  "    } \n" +
			  "        } \n" +
			  "      } \n" +
			  "    } \n" +
			  "  } \n" +
			  "}";
		
		return qry;
	}
	
	
	/**
	 * 용어 FSN 검색 쿼리 반환
	 * 
	 * @param q
	 * @param offset
	 * @param limit
	 * @return
	 */
	private String getFsnSearchQuery(String q, int offset, int limit) {
		String qry = "";
		
		boolean isContainColon = q.contains(":");
		List<String> qryWords;
		int qryWordsSize = 0;
		
		/*
		Query
		
		콜론이 포함되지 않은 경우 (띄어쓰기로 분할)
		----------------------------------------
		{
		  "query": {
		    "bool": {
		      "must": [
		        "regexp": {
		          "fsn.split_colon": .*{매개변수 q[n] (소문자)}.*
		        },
		        ...
		      ]
		    }
		  },
		  "size": {매개변수 pageRequest.getPageSize()},
		  "from": {매개변수 pageRequest.getOffset()},
		  "sort": {
		    "_script": {
		      "script": "doc['fsn.raw'].value.length()",
		      "type": "number",
		      "order": "asc"
		    }
		  }
		}
		----------------------------------------
		
		콜론이 포함된 경우
		----------------------------------------
		{
		  "query": {
		    "bool": {
		      "filter": [
		        "term": {
		          "fsn.autocomplete": {매개변수 q (소문자)}
		        }
		      ]
		    }
		  },
		  "size": {매개변수 pageRequest.getPageSize()},
		  "from": {매개변수 pageRequest.getOffset()},
		  "sort": {
		    "_script": {
		      "script": "doc['fsn.raw'].value.length()",
		      "type": "number",
		      "order": "asc"
		    }
		  }
		}
		----------------------------------------
		*/
		
		qry = "{\n" +
			  "  \"query\": { \n" +
			  "    \"bool\": { \n";
		
		// 검색어에 콜론이 포함되지 않은 경우
		if (!isContainColon) {
			
			qryWords = Arrays.asList(q.toLowerCase().split(" "));
			qryWordsSize = qryWords.size();
			
			
			qry +=  "      \"must\": [ \n";
			
			// 검색어의 띄어쓰기 수 만큼 반복
			for (int i = 0; i < qryWordsSize; i++) {
				qry += "        { \n" +
					   "          \"regexp\": { \n" +
					   "            \"fsn.split_colon\": \".*" + qryWords.get(i) + ".*\"\n" +
					   "          } \n";
				// 반복문의 마지막에만 콤마를 붙이지 않음
				if (i < qryWordsSize - 1) {
					qry += "        }, \n";
				} else {
					qry += "        } \n";
				}
			}
		}
		// 검색어에 콜론이 포함된 경우
		else {
			qry +=  "      \"filter\": [ \n" +
					"        { \n" +
					"          \"term\": { \n" +
					"            \"fsn\": \"" + q.toLowerCase() + "\"\n" +
//					"            \"fsn.autocomplete\": \"" + q.toLowerCase() + "\"\n" +
					"          } \n" +
					"        } \n";
		}
			 
		
		qry +="      ] \n" +
			  "    } \n" +
			  "  }, \n" +
			  
			  // 검색 결과 최대 개수
			  "  \"size\": " + limit + ", \n " +
			  
			  // 검색 결과 시작 지점
			  "  \"from\": " + offset + ", \n" +
			  "  \"sort\": { \n" +
			  "    \"_script\": { \n" +
			  "      \"script\": \"doc['fsn.raw'].value.length()\", \n" +
			  "      \"type\": \"number\", \n" +
			  "      \"order\": \"asc\" \n" +
			  "    }\n" +
			  "  } \n" +
			  "}";
		
		
		return qry;
	}
	
	
	/**
	 * 쿼리 수행 결과 반환
	 * 
	 * @param idx
	 * @param qry
	 * @return
	 */
	private SearchResult query(String idx, String qry) {

		// ----------------------------------------
		// 1. 변수 선언 및 초기화
		// ----------------------------------------

		// Elasticsearch에 쿼리를 보내고 결과를 받는 객체 선언
		Search srch = new Search.Builder(qry).addIndex(idx).build();
		SearchResult srchRslt = null;
		
		// ----------------------------------------
		// 2. 검색 수행
		// ----------------------------------------
		try {
			srchRslt = client.execute(srch);
			// below 1 line, added by Yu, 2018.01.05 
			log.info("query: " + qry + "\n received search result: " + srchRslt.getJsonString() + "\n   response code:" + srchRslt.getResponseCode() + "\n   error message: " + srchRslt.getErrorMessage());
		} catch (IOException e) {
			// 로그기록: 에러
			log.error(e.getMessage(), e);
			// below 1 line, added by Yu, 2018.01.05 
			e.printStackTrace();
		}
		
		// 검색 결과가 널이면 반환
		if (srchRslt == null) {
			return new SearchResult(new Gson());
		}

		return srchRslt;
	}


	
	
	
	/*
	boolean isContainColon = false;
	
	
	// 검색어에 콜론(:) 포함 여부: 파트의 구분자가 콜론(:)이다. 포함되어있다면 검색 대상은 오직 fsn이다.
	isContainColon = q.contains(":");
	
	
	isContainColon: false
		----------------------------------------
		{
		  "query": {
		    "bool": {
		      "should": [
		        {
		          "term": {
		            "fsn": {매개변수 q}
		          }
		        },
		        {
		          "term": {
		            "component": {매개변수 q}
		          }
		        },
		        {
		          "term": {
		            "property": {매개변수 q}
		          }
		        },
		        {
		          "term": {
		            "time": {매개변수 q}
		          }
		        },
		        {
		          "term": {
		            "system": {매개변수 q}
		          }
		        },
		        {
		          "term": {
		            "scale": {매개변수 q}
		          }
		        },
		        {
		          "term": {
		            "method": {매개변수 q}
		          }
		        }
		      ],
		      "minimum_should_match": 1
		    }
		  }
		}
		----------------------------------------
	
	*/
	
	
}
