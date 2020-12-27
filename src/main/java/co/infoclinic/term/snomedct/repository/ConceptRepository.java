package co.infoclinic.term.snomedct.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import co.infoclinic.term.snomedct.model.entity.Concept;
import co.infoclinic.term.snomedct.repository.custom.ConceptRepositoryCustom;

/**
 * Concept Repository
 */
@Transactional(readOnly = true)
public interface ConceptRepository extends JpaRepository<Concept, Long>, ConceptRepositoryCustom {

	/**
	 *
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	@Query(value = "SELECT c " +
				   "FROM Concept c " +
				   "WHERE c.conceptId=:conceptId " +
				   "AND   c.effectiveTime <= :effectiveTime " +
				   "ORDER BY c.effectiveTime desc")
	List<Concept> findConceptListByConceptIdAndEffectiveTime(
			@Param("conceptId") String conceptId,
			@Param("effectiveTime") String effectiveTime);
	


}