package co.infoclinic.term.snomedct.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DescriptionDTO extends ComponentDTO {

  private Long id;
  
  private String descriptionId;
  
  private String moduleId;
  
  private String conceptId;
  
  private String languageCode;
  
  private String typeId;
  
  private String term;
  
  private String caseSignificanceId;
  
  private List<LanguageRefsetDTO> languageReferencesetList;
}
