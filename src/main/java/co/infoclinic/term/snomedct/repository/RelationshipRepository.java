package co.infoclinic.term.snomedct.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import co.infoclinic.term.snomedct.model.entity.Relationship;

/**
 * Relationship Repository
 * 
 * @param <T>
 */
@NoRepositoryBean
public interface RelationshipRepository<T extends Relationship> extends JpaRepository<T, Long> {

}
