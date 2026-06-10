package co.infoclinic.term.snomedct.repository;


import co.infoclinic.term.snomedct.model.entity.StatedRelationship;
import co.infoclinic.term.snomedct.repository.custom.StatedRelationshipRepositoryCustom;

/**
 * The Stated Relationship Repository
 */
public interface StatedRelationshipRepository extends RelationshipRepository<StatedRelationship>, StatedRelationshipRepositoryCustom {

}
