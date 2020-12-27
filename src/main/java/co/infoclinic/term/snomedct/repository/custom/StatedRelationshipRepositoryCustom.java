package co.infoclinic.term.snomedct.repository.custom;

import java.util.List;

import co.infoclinic.term.snomedct.model.dto.RelationshipViewDTO;

/**
 * The Stated Relationship Repository
 */
public interface StatedRelationshipRepositoryCustom {

	/**
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	List<RelationshipViewDTO> findListBySourceIdAndEffectiveTime(String conceptId, String effectiveTime);
}
