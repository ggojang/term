package co.infoclinic.term.snomedct.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import co.infoclinic.term.snomedct.model.entity.Scheme;

/**
 * The Scheme Repository
 */
@Transactional(readOnly = true)
public interface SchemeRepository extends JpaRepository<Scheme, String> {

	/**
	 * 가장 최신버전의 스키마 조회
	 * @return
	 */
	@Query(value = "SELECT * " +
				   "FROM SCHEME " +
				   "ORDER BY STR_TO_DATE(VERSION, 'v%Y%m%d') DESC " +
				   "LIMIT 1", nativeQuery = true)	
	Scheme findLatest();
	
	
	/**
	 * 버전의 스키마 조회
	 * @param version
	 * @return
	 */
	@Query(value = "SELECT * " +
			   	   "FROM SCHEME " +
			   	   "WHERE VERSION = ?1", nativeQuery = true)
	Scheme findByVersion(String version);
}
