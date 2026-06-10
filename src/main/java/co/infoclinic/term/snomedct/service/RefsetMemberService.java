package co.infoclinic.term.snomedct.service;

import java.util.List;

import co.infoclinic.term.snomedct.model.dto.LanguageRefsetDTO;
import co.infoclinic.term.snomedct.model.dto.RefsetMemberDTO;
import co.infoclinic.term.snomedct.model.dto.RefsetMemberViewDTO;

public interface RefsetMemberService {

  RefsetMemberDTO createReferencesetMember(RefsetMemberDTO referencesetMemberDTO);

  RefsetMemberDTO updateReferencesetMember(RefsetMemberDTO referencesetMemberDTO);

  List<RefsetMemberDTO> createReferencesetMemberList(List<RefsetMemberDTO> referencesetMemberDTOList);

  List<RefsetMemberDTO> updateReferencesetMemberList(List<RefsetMemberDTO> referencesetMemberDTOList);

  boolean deleteReferencesetMember(Long id);

  boolean deleteReferencesetMember(RefsetMemberDTO referencesetMemberDTO);

  boolean deleteReferencesetMemberList(List<RefsetMemberDTO> referencesetMemberDTOList);

  
  
  
  
  LanguageRefsetDTO createLanguageReferencesetMember(LanguageRefsetDTO languageRefernecesetDTO);

  List<LanguageRefsetDTO> createLanguageReferencesetMemberList(List<LanguageRefsetDTO> languageReferencesetDTOList, String referencedComponentId);

  List<LanguageRefsetDTO> updateLanguageReferencesetMemberList(List<LanguageRefsetDTO> languageReferencesetDTOList);

  Boolean deleteLanguageReferencesetMember(Long id, LanguageRefsetDTO dto);
  
  
  
  
  
  
  List<RefsetMemberViewDTO> getHistoricalReferencesetMemberList(String componentId, String effectiveTime);
  List<RefsetMemberViewDTO> getConceptInactivationReferencesetMemberList(String conceptId, String effectiveTime);
  List<RefsetMemberViewDTO> getDescriptionInactivationReferencesetMemberList(String descriptionId, String effectiveTime);



}
