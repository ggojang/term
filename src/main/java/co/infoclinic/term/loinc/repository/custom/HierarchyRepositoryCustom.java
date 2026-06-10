package co.infoclinic.term.loinc.repository.custom;

import java.util.List;

import co.infoclinic.term.loinc.model.entity.Hierarchy;

/**
 * Hierarchy Custom Repository
 */
public interface HierarchyRepositoryCustom {

	/**
	 * Entity 코드의 Entity 상위 목록 조회
	 * 
	 * @param code
	 * @return
	 */
	List<Hierarchy> findAncestorListByCodeAndInclSelf(String code, boolean inclSelf);
}
