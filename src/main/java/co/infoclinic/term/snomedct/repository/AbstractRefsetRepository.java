package co.infoclinic.term.snomedct.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import co.infoclinic.term.snomedct.model.entity.AbstractReferenceset;

/**
 * Abstract Referenceset Repository
 *
 * @param <T>
 */
@NoRepositoryBean
public interface AbstractRefsetRepository<T extends AbstractReferenceset> extends JpaRepository<T, Long> {

	/**
	 * 
	 * @return
	 */
	@Query("select distinct(t.refsetId) from #{#entityName} t where t.refsetId != ''")
	Set<String> findRefsetId();


	/**
	 * 
	 * @param referenceSetId
	 * @param referencedComponentId
	 * @return
	 */
	@Query("select t from #{#entityName} t where t.refsetId = ?1 and t.referencedComponentId = ?2")
	List<T> findByRefsetIdAndReferencedComponentId(String referenceSetId, String referencedComponentId);

	
	
	/**
	 * 
	 * @param referencedComponentIdList
	 * @param languageReferencesetIdList
	 * @param effectiveTime
	 * @return
	 */
	abstract List<T> findByReferencedComponentIdsAndRefsetIdsAndEffectiveTime(List<String> referencedComponentIdList,
			List<String> languageReferencesetIdList, String effectiveTime);

	
	/**
	 * 
	 * @param refsetIdList
	 * @param referencedComponentId
	 * @param effectiveTime
	 * @return
	 */
	abstract List<T> findByRefsetIdsAndReferencedComponentIdAndEffectiveTime(List<String> refsetIdList,
			String referencedComponentId, String effectiveTime);
}
