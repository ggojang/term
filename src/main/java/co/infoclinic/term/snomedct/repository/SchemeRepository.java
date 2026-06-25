package co.infoclinic.term.snomedct.repository;

import java.util.List;

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
	 * 모든 릴리즈(International + Extension) version 내림차순 조회
	 */
	@Query(value = "SELECT * " +
				   "FROM SCHEME " +
				   "ORDER BY TO_DATE(SUBSTRING(VERSION, 2), 'YYYYMMDD') DESC", nativeQuery = true)
	List<Scheme> findAllOrderByVersionDesc();


	/**
	 * International Edition 최신 Scheme (EXTENSION_NAME IS NULL)
	 */
	@Query(value = "SELECT * " +
				   "FROM SCHEME " +
				   "WHERE EXTENSION_NAME IS NULL " +
				   "ORDER BY TO_DATE(SUBSTRING(VERSION, 2), 'YYYYMMDD') DESC " +
				   "LIMIT 1", nativeQuery = true)
	Scheme findLatest();


	/**
	 * version으로 Scheme 조회
	 */
	@Query(value = "SELECT * " +
				   "FROM SCHEME " +
				   "WHERE VERSION = ?1", nativeQuery = true)
	Scheme findByVersion(String version);
}
