package co.infoclinic.term.loinc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.infoclinic.term.loinc.model.entity.Panel;

public interface PanelRepository extends JpaRepository<Panel, Integer> {

	@Query(value = 
			"SELECT P1.* " +
			"FROM loinc.PANEL AS P1 " +
			"INNER JOIN ( " +
			"  SELECT " +
			"    CASE WHEN ROOT_CODE != ?1 " +
			"    THEN CONCAT(ROOT_CODE, '~%', ?1, '%') " +
			"    ELSE CONCAT(ROOT_CODE, '%') " +
			"    END AS PATH " +
			"  FROM loinc.PANEL " +
			"  WHERE CODE = ?1 AND PARENT_CODE = 'ROOT' " + 
			"  LIMIT 1 " +
			") AS P2 " +
			"ON P1.PATH LIKE P2.PATH " +
			"ORDER BY PATH, SEQUENCE", nativeQuery = true)
	List<Panel> findByCode(String code);
	
	
	@Query(value = 
			"SELECT P1.* " +
			"FROM loinc.PANEL AS P1 " +
			"INNER JOIN ( " +
			"  SELECT ROOT_CODE " +
			"  FROM loinc.PANEL " +
			"  WHERE CODE = ?1 " +
			") AS P2 " +
			"ON P1.CODE = P2.ROOT_CODE AND P1.CODE <> ?1 " +
			"GROUP BY PARENT_NAME ORDER BY PARENT_NAME", nativeQuery = true) // 2020.12.27 by Yu
	List<Panel> findRootPanelListByCode(String code);
}
