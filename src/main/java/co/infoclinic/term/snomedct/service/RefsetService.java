package co.infoclinic.term.snomedct.service;

import java.util.List;

import co.infoclinic.term.snomedct.model.entity.Referenceset;

public interface RefsetService {
	
	/**
	 * 모든 레퍼런스세트 ID 목록 조회
	 * @return
	 */
	List<String> getReferencesetIdList();
	
	
	/**
	 * 멤버가 하나이상 존재하는 레퍼런스세트 ID 목록 조회
	 * @return
	 */
    List<String> getReferencesetIdExistList();
    
    
    /**
     * 
     * @param referencedComponentIdList
     * @param languageReferencesetIdList
     * @param effectiveTime
     * @return
     */
    List<Referenceset> getRefsetIds(List<String> referencedComponentIdList, List<String> languageReferencesetIdList, String effectiveTime);
}
