package co.infoclinic.term.loinc.service;

import java.util.List;

import co.infoclinic.term.loinc.model.dto.HierarchyDTO;

/**
 * Hierarchy service
 */
public interface HierarchyService {

	List<HierarchyDTO> getParentListByCode(String code);
	
	List<HierarchyDTO> getChildren(String code, String path, String lang);

	List<HierarchyDTO> getPathListByCode(String code);

	List<HierarchyDTO> getDescendantList(String code, String path, boolean inclSelf);

	List<HierarchyDTO> getAncestorList(String code, boolean inclSelf);

	int isSubsumptionTest(String criteriaCode, String code);
}
