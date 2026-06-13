package co.infoclinic.term.snomedct.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.snomedct.model.entity.LatestRefsetMember;
import co.infoclinic.term.snomedct.model.entity.LatestRefsetMemberId;

/**
 * 최근 레퍼런스세트 멤버 저장소 인터페이스
 */
public interface LatestRefsetMemberRepository extends JpaRepository<LatestRefsetMember, LatestRefsetMemberId> {

	
	/**
	 * 활성 멤버 개수 조회
	 * 
	 * @param edition
	 * @param version
	 * @param refsetId
	 * @return
	 */
	@Query(value =
		
			"SELECT COUNT(*) " +
			"FROM REFERENCESET_ACTIVE " +
			"WHERE VERSION = CONCAT(?1, '-', ?2) " +
			"AND REFSET_ID = ?3", nativeQuery = true)
			
	int findCountByEditionAndVersionAndRefsetId(String edition, String version, String refsetId);
	
	
	/**
	 * 활성 멤버 조회
	 * 
	 * @param edition
	 * @param version
	 * @param refsetId
	 * @param offset
	 * @param limit
	 * @return
	 */
	@Query(value =
			
			"SELECT * " +
			"FROM REFERENCESET_ACTIVE   " +
			"WHERE VERSION = CONCAT(?1, '-', ?2) " +
			"AND REFSET_ID = ?3 " +
			"LIMIT ?5 OFFSET ?4 ", nativeQuery = true)
			
	List<LatestRefsetMember> findByEditionAndVersionAndRefsetIdAndOffsetAndLimit(String edition, String version, String refsetId, int offset, int limit);
	
	
	/**
	 * 활성 멤버 개수 조회 (특정 참조된 컴포넌트)
	 * 
	 * @param edition
	 * @param version
	 * @param refsetId
	 * @return
	 */
	@Query(value =
			
			"SELECT COUNT(*) " +
			"FROM REFERENCESET_ACTIVE " +
			"WHERE VERSION = CONCAT(?1, '-', ?2) " +
			"AND REFSET_ID = ?3 " +
			"AND REFERENCED_COMPONENT_ID = ?4", nativeQuery = true)
			
	int findCountByEditionAndVersionAndRefsetIdAndRefCpntId(String edition, String version, String refsetId, String refCpntId);
	
	
	/**
	 * 활성 멤버 조회 (특정 참조된 컴포넌트)
	 *
	 * @param edition
	 * @param version
	 * @param refsetId
	 * @param refCpntId
	 * @param offset
	 * @param limit
	 * @return
	 */
	@Query(value = 
			"SELECT * " +
			"FROM REFERENCESET_ACTIVE   " +
			"WHERE VERSION = CONCAT(?1, '-', ?2) " +
			"AND REFSET_ID = ?3 " +
			"AND REFERENCED_COMPONENT_ID = ?4 " +
			"LIMIT ?6 OFFSET ?5 ", nativeQuery = true)
	List<LatestRefsetMember> findByEditionAndVersionAndRefsetIdAndRefCpntIdAndOffsetAndLimit(String edition, String version, String refsetId, String refCpntId, int offset, int limit);
	

	/**
	 * 디스크립터 목록 조회
	 * 
	 * @param string
	 * @param effectiveTime
	 * @param refsetId
	 * @return
	 */
	@Query(value = 
			"SELECT * " +
			"FROM REFERENCESET_ACTIVE   " +
			"WHERE VERSION = CONCAT(?1, '-', ?2) " +
			"AND REFSET_ID = '900000000000456007' " +
			"AND REFERENCED_COMPONENT_ID = ?3 " +
			"ORDER BY NULLIF(FIELD3_VALUE,'')::integer ASC NULLS LAST", nativeQuery = true)
	List<LatestRefsetMember> findByEditionAndVersionAndRefsetId(String string, String effectiveTime, String refsetId);

	
	
	/**
	 * 검색 결과 개수
	 * 
	 * @param edition
	 * @param version
	 * @param refsetId
	 * @return
	 */
	@Query(value =
			"SELECT COUNT(*) " +
			"FROM REFERENCESET_ACTIVE " +
			"WHERE VERSION = CONCAT(?1, '-', ?2) " +
			"AND REFSET_ID = ?3 " +
			"AND REFERENCED_COMPONENT_NAME ILIKE '%' || ?4 || '%'", nativeQuery = true)
	int findCountByEditionAndVersionAndRefsetIdAndTerm(String edition, String version, String refsetId, String term);


	/**
	 * 멤버 검색 (ILIKE + pg_trgm 인덱스 활용)
	 *
	 * @param string
	 * @param effectiveTime
	 * @param refsetId
	 * @param term
	 * @param offset
	 * @param limit
	 * @return
	 */
	@Query(value =
			"SELECT * " +
			"FROM REFERENCESET_ACTIVE " +
			"WHERE VERSION = CONCAT(?1, '-', ?2) " +
			"AND REFSET_ID = ?3 " +
			"AND REFERENCED_COMPONENT_NAME ILIKE '%' || ?4 || '%' " +
			"LIMIT ?6 OFFSET ?5", nativeQuery = true)
	List<LatestRefsetMember> findByEditionAndVersionAndRefsetIdAndTermAndOffsetAndLimit(String string, String effectiveTime, String refsetId, String term, int offset, int limit);
}
