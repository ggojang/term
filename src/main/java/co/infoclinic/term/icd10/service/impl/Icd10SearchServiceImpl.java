package co.infoclinic.term.icd10.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.*;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
//import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import co.infoclinic.term.icd10.model.dto.Icd10SearchResultDTO;
import co.infoclinic.term.icd10.service.Icd10SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

/**
 * ICD10 Search Service
 */
@Service("ICD10SrchSvc")
public class Icd10SearchServiceImpl implements Icd10SearchService {

	/** Logger */
	Logger log = LoggerFactory.getLogger(Icd10SearchService.class);
	
	/** ICD10 검색 인덱스 명  */
	private static final String IDX_SRCH = "icd10_search";
	
	/** DI: jest client for elasticsearch  */
	@Inject
	private JestClient client;
	
	
	// ----------------------------------------
	// Public Methods
	// ----------------------------------------
	// searchByWord       : Code 또는 Label의 일부 용어에 대한 검색결과를 반환
	
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.service.SearchService#searchByWord(java.lang.String, int, int)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Page<Icd10SearchResultDTO> searchByWord(String word, int page, int size) {
		if (StringUtils.isEmpty(word) || page < 1 || size < 1) {
			return new PageImpl<Icd10SearchResultDTO>(new ArrayList<Icd10SearchResultDTO>());
		}
		
		// ----------------------------------------
		// 1. 지역변수 선언 및 초기화
		// ----------------------------------------
		Page<Icd10SearchResultDTO> rslts;
		List<Icd10SearchResultDTO> dtos;
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
			// 분석결과 용어일 경우: 쿼리 대상 필드는 'LABEL'
			qry = getLabelSearchQuery(word, offset, limit);
		}
		
		// ----------------------------------------
		// 4. 쿼리 수행
		// ----------------------------------------
		srchRslt = query(IDX_SRCH, qry);
		
		//log.debug("IDX: " + IDX_SRCH + "\n" + qry);
		
		// ----------------------------------------
		// 5. 반환 데이터 생성
		// ----------------------------------------
		dtos = srchRslt.getSourceAsObjectList(Icd10SearchResultDTO.class);
		total = srchRslt.getTotal();
		rslts = new PageImpl<>(dtos, new PageRequest(page - 1, size), total);
		
		
		// ----------------------------------------
		// 6. 반환
		// ----------------------------------------
		return rslts;
	};
	
	// ----------------------------------------
	// Private Methods
	// ----------------------------------------
	// isCode:             LOINC의 코드가 맞는지 확인
	// getCodeSearchQuery: 코드 검색 쿼리 반환
	// getTermSearchQuery: 용어 검색 쿼리 반환
	
	
	private boolean isCode(String q) {
		boolean valid = false;
		
		// 매개변수 q가 널 값이 아닌 경우만 처리
		if (q != null) {
			// q의 길이
			int len = q.length();
			// 대시(-) 포함 여부
			//boolean isContainDash = q.contains("-");
			
			// q의 길이가 3이상 10이하이면서, 대시(-) 포함
			
			Pattern pattern = Pattern.compile( "^[IVX]{1,5}" );
			Matcher matcher = pattern.matcher( q );
			
			if (matcher.find()) {
				valid = true;
				return valid;
			} else if (len >= 3 && len <= 7) {
				
				// q의 첫번째 문자
				String firstChar = q.substring(1, 1);
				// q의 2,3번 문
				String secondthirdChar = q.substring(2, 3);
				// q의 4번째 문자
				//String fourthChar = q.substring(4, 4);
				// q의 5번째 문자
				//String fifthChar = q.substring(5);
				
				// q의 1번쨰는 영문자, 2-3번째는 숫자, 4번쨰는 (. or - or ""), 5번째이후는 영문자+숫 => ICD10 Code

				valid = StringUtils.isAlpha(firstChar) 
						&& (StringUtils.isNumeric(secondthirdChar))
						//&& StringUtils.isAlphanumeric(fifthChar) 
						? true : false;

				//if (fourthChar >= 0) {
				//	valid = (".".equals(fourthChar)) || ("-".equals(fourthChar)) || ("".equals(fourthChar))
				//	? true : false;
				//} else {
				//	fourthChar = "";
				//}
			
			}
		}
		
		return valid;
	}
	
	/**
	 * 코드 검색 쿼리 반환
	 * 
	 * @param code
	 * @return
	 */
	private String getCodeSearchQuery(String code) {
		String qry = "";		
		
		qry = "{\n" +
			  "	\"query\": {\n" +
			  "		\"bool\": {\n" +
			  "			\"filter\": [\n" +
			  "				{\n" +
			  "        			\"term\": {\n" +
			  "          			\"kind\": \"" + "preferred" + "\"\n" +
			  "    				}\n" +
			  "				},\n" + 
			  "				{\n" +
			  "        			\"term\": {\n" +
			  "          			\"code\": \"" + code.toUpperCase() + "\"\n" +
			  "    				}\n" +
			  "				}\n" + 
			  "			]\n" +
			  "		}\n" +
			  "	}\n" +
			  "}";
		
		return qry;
	}
	
	
	/**
	 * 용어 Label 검색 쿼리 반환
	 * 
	 * @param q
	 * @param offset
	 * @param limit
	 * @return
	 */
	private String getLabelSearchQuery(String q, int offset, int limit) {
		String qry = "";
		
		List<String> qryWords;
		int qryWordsSize = 0;
		
		
		qry = "{\n" +
			  "  \"query\": { \n" +
			  "    \"bool\": { \n";
		
		qryWords = Arrays.asList(q.toLowerCase().split(" "));
		qryWordsSize = qryWords.size();
				
		qry +=  "      \"must\": [ \n";
			
		// 검색어의 띄어쓰기 수 만큼 반복
		for (int i = 0; i < qryWordsSize; i++) {
			qry += "        { \n" +
				   "          \"regexp\": { \n" +
				   "            \"label\": \".*" + qryWords.get(i) + ".*\"\n" +
				   "          } \n";
			// 반복문의 마지막에만 콤마를 붙이지 않음
			if (i < qryWordsSize - 1) {
				qry += "        }, \n";
			} else {
				qry += "        } \n";
			}
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
			  "      \"script\": \"doc['label.raw'].value.length()\", \n" +
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
	
}
