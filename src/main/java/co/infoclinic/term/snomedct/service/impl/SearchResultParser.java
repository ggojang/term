package co.infoclinic.term.snomedct.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import co.infoclinic.term.common.utils.SNOMEDCTUtils;
import co.infoclinic.term.snomedct.model.dto.SearchResults;
import co.infoclinic.term.snomedct.model.dto.SemanticTag;
import co.infoclinic.term.snomedct.model.dto.TermSearchResult;
import io.searchbox.core.SearchResult;

@Service
public class SearchResultParser {

  Logger log = LoggerFactory.getLogger(SearchResultParser.class);

  
  public SearchResults parseResults(SearchResult searchResults, Pageable pageable) {
    // processing for search results
    List<TermSearchResult> sourceList = new ArrayList<TermSearchResult>();
    JsonObject hits = searchResults.getJsonObject().get("hits").getAsJsonObject();
    int total = hits.get("total").getAsInt();
    JsonArray hitArray = hits.get("hits").getAsJsonArray();
    JsonArray semanticTagArray = searchResults.getJsonObject()
    								.get("aggregations").getAsJsonObject()
									.get("semanticTags").getAsJsonObject()
									.get("buckets").getAsJsonArray();
    
    if (!hitArray.isJsonNull()) {
      JsonElement source = null;
      int hitArraySize = hitArray.size();
      for (int i = 0; i < hitArraySize; i++) {
        source = hitArray.get(i).getAsJsonObject().get("_source");
        sourceList.add(prepareResult(source));
      }
    }
    List<SemanticTag> semanticList = new ArrayList<SemanticTag>();
    if (!semanticTagArray.isJsonNull()) {
      JsonObject bucket = null;
      SemanticTag semanticTag = null;
      int semanticTagArraySize = semanticTagArray.size();
      for (int i = 0; i < semanticTagArraySize; i++) {
        bucket = semanticTagArray.get(i).getAsJsonObject();

        String key = bucket.get("key").getAsString();
        int count = bucket.get("doc_count").getAsInt();
        semanticTag = new SemanticTag();
        semanticTag.setName(key);
        semanticTag.setCount(count);
        semanticList.add(semanticTag);
      }
    }

    PageImpl<TermSearchResult> page = new PageImpl<>(sourceList, pageable, total);
    SearchResults results = new SearchResults();
    results.setPage(page);
    results.setSemanticTags(semanticList);
    return results;
  }

  private TermSearchResult prepareResult(JsonElement jsonElement) {
    final JsonObject source = jsonElement.getAsJsonObject();
    int primitive = source.get("primitive").getAsInt();
    int type = source.get("type").getAsInt();
    String typeId = "";
    
    // Fully specified name
    if (type == 1) { // Fully specified name
    	typeId = SNOMEDCTUtils.DescriptionType.FullySpecifiedName;
    } else if (type == 3) { // Synonym
    	typeId = SNOMEDCTUtils.DescriptionType.Synonym;
    } else if (type == 4) { // Definition
    	typeId = SNOMEDCTUtils.DescriptionType.Definition;
    } else {
    	log.error("ERROR! DO NOT FIND TYPE OF DESCRIPTION : " + type);
    }

    TermSearchResult result = new TermSearchResult();
    result.setConceptActive(source.get("conceptActive").getAsInt() == 1 ? true : false);
    result.setDescriptionActive(source.get("descriptionActive").getAsInt() == 1 ? true : false);
    result.setConceptId(source.get("conceptId").getAsString());
    result.setDescriptionId(source.get("descriptionId").getAsString());
    result.setConceptEffectiveTime(source.get("conceptEffectiveTime").getAsString());
    result.setDescriptionEffectiveTime(source.get("descriptionEffectiveTime").getAsString());
    result.setDefinitionStatusId(primitive == 1 ? SNOMEDCTUtils.DefinitionStatus.Primitive:SNOMEDCTUtils.DefinitionStatus.Defined);
    result.setTypeId(typeId);
    result.setFsn(source.get("fsn").getAsString());
    result.setLength(source.get("length").getAsInt());
    result.setLang(source.get("lang").getAsString());
    result.setSemanticTag(source.get("semanticTag").getAsString());
    result.setTerm(source.get("term").getAsString());
    result.setAcceptabilityId(source.get("acceptabilityId").getAsString()); // 20200701 by Yu
    
    return result;
  }
}
