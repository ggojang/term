package co.infoclinic.term.loinc.model.converter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.loinc.model.dto.HierarchyDTO;
import co.infoclinic.term.loinc.model.entity.Hierarchy;

/**
 * Hierarchy DTO와 Entity간 객체 변환기
 */
@Component(value = "LNCHierCvt")
public final class HierarchyConverter {

	/** Logger */
	static final Logger log = LoggerFactory.getLogger(HierarchyConverter.class);
	
	/**
	 * Hierarchy Entity List를 DTO  List로 변환
	 * 
	 * @param hierarchies
	 * @return
	 */
	public static List<HierarchyDTO> toDTOList(List<Hierarchy> hierarchies) {
		// 반환 객체
		List<HierarchyDTO> dtos = new ArrayList<HierarchyDTO>();
		
		int hierarchiesSize = hierarchies.size();
		for (int i = 0; i < hierarchiesSize; i++) {
			dtos.add(toDTO(hierarchies.get(i)));
		}

		return dtos;
	}
	
	/**
	 * Hierarchy Entity를 DTO로 변환
	 * 
	 * @param hierarchy
	 * @return
	 */
	public static HierarchyDTO toDTO(Hierarchy hierarchy) {
		HierarchyDTO dto = new HierarchyDTO();
		
		dto.setCode(hierarchy.getCode());
		dto.setName(hierarchy.getName());
		dto.setPrefName(hierarchy.getPreferredName());
		dto.setParent(hierarchy.getParent());
		dto.setPath(hierarchy.getPath());
		dto.setChdCnt(hierarchy.getChildrenCount());
		dto.setDesCnt(hierarchy.getDescendantCount());
		
		return dto;
	}
}
