package co.infoclinic.term.snomedct.model.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResults {
	
	private Page<TermSearchResult> page;
	
	private List<SemanticTag> semanticTags;
}
