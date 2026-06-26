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
	 * TC가 적재된 릴리즈만 반환.
	 * - International: TC_META에 정확히 일치하는 날짜가 있는 경우
	 * - Extension/Refset: TC_META에 같은 날짜 이하인 항목이 있는 경우 (가장 가까운 International TC 공유)
	 */
	@Query(value = "SELECT s.* " +
				   "FROM term.SCHEME s " +
				   "WHERE EXISTS (" +
				   "  SELECT 1 FROM term.TC_META m " +
				   "  WHERE m.EFFECTIVE_TIME <= REPLACE(s.VERSION, 'v', '')" +
				   ") " +
				   "ORDER BY TO_DATE(SUBSTRING(s.VERSION, 2), 'YYYYMMDD') DESC", nativeQuery = true)
	List<Scheme> findAllOrderByVersionDesc();


	/**
	 * International Edition 최신 Scheme (EXTENSION_NAME IS NULL)
	 */
	@Query(value = "SELECT * " +
				   "FROM term.SCHEME " +
				   "WHERE EXTENSION_NAME IS NULL " +
				   "ORDER BY TO_DATE(SUBSTRING(VERSION, 2), 'YYYYMMDD') DESC " +
				   "LIMIT 1", nativeQuery = true)
	Scheme findLatest();


	/**
	 * version으로 Scheme 조회
	 */
	@Query(value = "SELECT * " +
				   "FROM term.SCHEME " +
				   "WHERE VERSION = ?1", nativeQuery = true)
	Scheme findByVersion(String version);
}
