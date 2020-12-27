package co.infoclinic.term.snomedct.service;

import java.util.List;

import org.springframework.data.domain.Page;

import co.infoclinic.term.snomedct.model.dto.RefsetMemberQryDTO;
import co.infoclinic.term.snomedct.model.dto.RefsetMemberViewDTO;
import co.infoclinic.term.snomedct.model.entity.Referenceset;


/**
 * 레퍼런스세트 멤버 조회관련 처리를 담당하는 서비스
 */
public interface RefsetMemberQueryService {

	/**
	 * 레퍼런스세트 멤버 조회
	 * 
	 * @param refsetId
	 * @param effectiveTime
	 * @param q
	 * @param page
	 * @param size
	 * @return
	 */
	Page<RefsetMemberQryDTO> getMemberList(String refsetId, String effectiveTime, String q, int page, int size);

	
	/**
	 * 레퍼런스세트 멤버 조회(조건 :특정 레퍼런스세트, 특정 참조된 컴포넌트)
	 * 
	 * @param refsetId
	 * @param refCpntId
	 * @param effectiveTime
	 * @param page
	 * @param size
	 * @return
	 */
	Page<RefsetMemberQryDTO> getSpecificMemberList(String refsetId, String refCpntId, String effectiveTime, int page, int size);


	/**
	 * 레퍼런스세트 디스크립터 목록 조회
	 * 
	 * @param refsetId
	 * @param effectiveTime
	 * @return
	 */
	List<RefsetMemberQryDTO> getDescriptorList(String refsetId, String effectiveTime);


	/**
	 * 
	 * @param referencedComponentId
	 * @param version
	 * @return
	 */
	List<RefsetMemberViewDTO> getMemberList(String referencedComponentId, String version);

	
	/**
	 * 
	 * @param refsetId
	 * @param referencedComponentId
	 * @param version
	 * @return
	 */
	List<RefsetMemberViewDTO> getMemberList(String refsetId, String referencedComponentId, String version);


	/**
	 * 
	 * @param refsetId
	 * @param referencedComponentId
	 * @param effectiveTime
	 * @return
	 */
	List<Referenceset> getMemberEntityList(String refsetId, String referencedComponentId, String effectiveTime);
}
