package co.infoclinic.term.snomedct.repository;

import co.infoclinic.term.snomedct.model.entity.InferredRelationship;
import co.infoclinic.term.snomedct.repository.custom.InferredRelationshipRepositoryCustom;

/**
 * The Inferred Relationship Repository
 */
public interface InferredRelationshipRepository extends RelationshipRepository<InferredRelationship>, InferredRelationshipRepositoryCustom {

}
