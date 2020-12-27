package co.infoclinic.term.snomedct.repository.custom;

import java.util.List;

import co.infoclinic.term.snomedct.model.entity.Description;

/**
 * Description Custom Repository
 */
public interface DescriptionRepositoryCustom {
	
	/**
	 * 경로를 부모로 갖는 Concept의 fsn 목록을 반환
	 * 
	 * @param paths
	 * @return
	 */
	List<Description> findByPaths(List<String> paths);
}
