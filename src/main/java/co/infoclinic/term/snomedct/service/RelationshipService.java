package co.infoclinic.term.snomedct.service;

import java.util.List;

import co.infoclinic.term.snomedct.model.dto.RelationshipViewDTO;

/**
 * The Relationship Service
 */
public interface RelationshipService {

  /**
   * Relationship 목록을 반환하는 메소드
   * 
   * @param conceptId
   * @param effectiveTime
   * @param isActive
   * @param isStated
   * @return
   */
  List<RelationshipViewDTO> getRelationshipList(String conceptId, String effectiveTime, boolean isStated);

}
