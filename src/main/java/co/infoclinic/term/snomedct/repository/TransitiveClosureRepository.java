package co.infoclinic.term.snomedct.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.snomedct.model.entity.TransitiveClosure;

/**
 * The Transitive Closure Repository
 * TC 테이블: 직접 IS-A 관계 (child_id, parent_id)만 저장
 * 계층 탐색은 재귀 CTE로 실시간 계산
 */
public interface TransitiveClosureRepository extends JpaRepository<TransitiveClosure, Long> {

	/**
	 * TC에 저장된 릴리즈 effectiveTime 목록 (최신순) — tc_meta 테이블에서 빠르게 조회
	 */
	@Query(value = "SELECT EFFECTIVE_TIME FROM TC_META ORDER BY EFFECTIVE_TIME DESC", nativeQuery = true)
	List<String> findDistinctEffectiveTimes();


	/**
	 * 여러 개념의 직접 부모 ID 목록 조회 (경로 대용 — IS-A 기반)
	 */
	@Query(value = "SELECT DISTINCT PARENT_ID " +
				   "FROM TC " +
				   "WHERE CHILD_ID IN (?1) AND VALID_FROM <= ?2 AND VALID_TO > ?2", nativeQuery = true)
	List<String> findPathListByConceptIds(List<String> conceptIds, String effectiveTime);


	/**
	 * 개념 ID 자체를 반환 (하위 탐색 루트용 — 이전 PATH 방식 호환)
	 * 재귀 CTE descendant 쿼리의 시작점으로 conceptId를 사용
	 */
	@Query(value = "SELECT CHILD_ID " +
				   "FROM TC " +
				   "WHERE CHILD_ID = ?1 AND VALID_FROM <= ?2 AND VALID_TO > ?2 " +
				   "LIMIT 1", nativeQuery = true)
	List<String> findPathListByConceptId(String conceptId, String effectiveTime);


	/**
	 * 조상 ID 목록 조회 (재귀 CTE — 루트 방향 탐색)
	 */
	@Query(value = "WITH RECURSIVE anc AS (" +
				   "  SELECT PARENT_ID AS concept_id FROM TC " +
				   "  WHERE CHILD_ID = ?1 AND VALID_FROM <= ?2 AND VALID_TO > ?2 " +
				   "  UNION ALL " +
				   "  SELECT tc.PARENT_ID FROM TC tc " +
				   "  JOIN anc a ON tc.CHILD_ID = a.concept_id " +
				   "  WHERE tc.VALID_FROM <= ?2 AND tc.VALID_TO > ?2 " +
				   ") SELECT concept_id FROM anc", nativeQuery = true)
	List<String> findParentPathListByConceptId(String conceptId, String effectiveTime);


	/**
	 * Language Refset Id 목록 조회 (900000000000506000 하위 자손 — 재귀 CTE)
	 */
	@Query(value = "WITH RECURSIVE lang AS (" +
				   "  SELECT CHILD_ID AS concept_id FROM TC " +
				   "  WHERE PARENT_ID = '900000000000506000' AND VALID_FROM <= ?1 AND VALID_TO > ?1 " +
				   "  UNION ALL " +
				   "  SELECT tc.CHILD_ID FROM TC tc " +
				   "  JOIN lang l ON tc.PARENT_ID = l.concept_id " +
				   "  WHERE tc.VALID_FROM <= ?1 AND tc.VALID_TO > ?1 " +
				   ") SELECT concept_id FROM lang", nativeQuery = true)
	List<String> findLanguageRefsetIdList(String effectiveTime);


	/**
	 * 포함 여부 확인 (conceptId가 criteriaId의 자손인지 — 재귀 CTE)
	 */
	@Query(value = "WITH RECURSIVE desc_cte AS (" +
				   "  SELECT CHILD_ID AS concept_id FROM TC " +
				   "  WHERE PARENT_ID = ?1 AND VALID_FROM <= ?3 AND VALID_TO > ?3 " +
				   "  UNION ALL " +
				   "  SELECT tc.CHILD_ID FROM TC tc " +
				   "  JOIN desc_cte d ON tc.PARENT_ID = d.concept_id " +
				   "  WHERE tc.VALID_FROM <= ?3 AND tc.VALID_TO > ?3 " +
				   ") SELECT COUNT(*) FROM desc_cte WHERE concept_id = ?2", nativeQuery = true)
	int findCountByCriteriaIdAndConceptId(String criteriaId, String conceptId, String effectiveTime);


	/**
	 * 직접 자식 수 조회
	 */
	@Query(value = "SELECT COUNT(*) FROM TC " +
				   "WHERE PARENT_ID = ?1 AND VALID_FROM <= ?2 AND VALID_TO > ?2", nativeQuery = true)
	int findChildrenCountByConceptId(String conceptId, String effectiveTime);


	/**
	 * 자손 수 조회 (재귀 CTE)
	 */
	@Query(value = "WITH RECURSIVE desc_cte AS (" +
				   "  SELECT CHILD_ID AS concept_id FROM TC " +
				   "  WHERE PARENT_ID = ?1 AND VALID_FROM <= ?2 AND VALID_TO > ?2 " +
				   "  UNION ALL " +
				   "  SELECT tc.CHILD_ID FROM TC tc " +
				   "  JOIN desc_cte d ON tc.PARENT_ID = d.concept_id " +
				   "  WHERE tc.VALID_FROM <= ?2 AND tc.VALID_TO > ?2 " +
				   ") SELECT COUNT(*) FROM desc_cte", nativeQuery = true)
	int findDescendantCountByConceptId(String conceptId, String effectiveTime);
}
