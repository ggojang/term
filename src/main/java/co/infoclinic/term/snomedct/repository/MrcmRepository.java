package co.infoclinic.term.snomedct.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.snomedct.model.entity.MrcmConstraints;

/**
 * MRCM Repository
 */
public interface MrcmRepository extends JpaRepository<MrcmConstraints, Long> {

	/**
	 * 
	 * @param sourceId
	 * @return
	 */
	@Query("SELECT m " +
	       "FROM MrcmConstraints m " +
		   "WHERE m.sourceId = ?1")
	List<MrcmConstraints> findBySourceId(String sourceId);
	
	
	/**
	 * 
	 * @param sourceIdList
	 * @return
	 */
	@Query("SELECT m " +
	       "FROM MrcmConstraints m " +
		   "WHERE m.sourceId IN (?1)")
	List<MrcmConstraints> findBySourceIdList(List<String> sourceIdList);
}
