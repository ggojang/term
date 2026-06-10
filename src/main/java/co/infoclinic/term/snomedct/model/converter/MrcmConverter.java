package co.infoclinic.term.snomedct.model.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.snomedct.model.dto.DefiningAttributeDTO;
import co.infoclinic.term.snomedct.model.dto.DefiningRangeDTO;
import co.infoclinic.term.snomedct.model.entity.MrcmConstraints;

@Component
public final class MrcmConverter {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(MrcmConverter.class);
	
	public static List<DefiningAttributeDTO> mapEntitiesIntoDTOList(final List<MrcmConstraints> mrcmList) {
		List<DefiningAttributeDTO> definingAttributeDTOList = new ArrayList<DefiningAttributeDTO>();
		
		List<DefiningRangeDTO> definingRangeDTOList = null;
		
		// ConcurrentHashMap 사용 이유 : 멀티쓰레드에 유리, 500만건기준 타해시맵보다 add,retrieve 속도 4배이상 빠름. (jdk1.5부터 지원)
		// http://crunchify.com/hashmap-vs-concurrenthashmap-vs-synchronizedmap-how-a-hashmap-can-be-synchronized-in-java/
		
		// ConcurrentHashMap : (attributeId : DefiningAttributeDTO)
		Map<String, DefiningAttributeDTO> attributeIdToDefiningAttributeDTOMap
			= new ConcurrentHashMap<String, DefiningAttributeDTO>();
		
		// ConcurrentMap : ()
		Map<String, List<DefiningRangeDTO>> attributeIdToDefiningRangeDTOListMap 
			= new ConcurrentHashMap<String, List<DefiningRangeDTO>>();
		
		// ConcurrentHashMap : (valueId : DefiningRangeDTO)
		Map<String, DefiningRangeDTO> valueIdToDefiningRangeDTOMap = new ConcurrentHashMap<String, DefiningRangeDTO>();
		
		DefiningRangeDTO definingRangeDTO = null;
		
		
		int mrcmListSize = mrcmList.size();
		for (int i = 0; i < mrcmListSize; i++) {
			MrcmConstraints mrcm = mrcmList.get(i);
			String attributeId = mrcm.getAttributeId();
			String attributeName = mrcm.getAttributeName();
			String valueId = mrcm.getValueId();
			String valueName = mrcm.getValueName();
			
			// Map Key:AttributeId, Value:AttributeName (AttributeId의 중복이 존재함)
			if (!attributeIdToDefiningAttributeDTOMap.containsKey(attributeId)) {
				attributeIdToDefiningAttributeDTOMap.put(
						attributeId, 
						newDefiningAttributeDTO(attributeId, attributeName, null));
			}
			
			// AttributeId별 Range 리스트 구성
			definingRangeDTOList = attributeIdToDefiningRangeDTOListMap.get(attributeId);
			if (definingRangeDTOList == null) {
				definingRangeDTOList = new ArrayList<DefiningRangeDTO>();
				attributeIdToDefiningRangeDTOListMap.put(attributeId, definingRangeDTOList);
			}
			
			if (!valueIdToDefiningRangeDTOMap.containsKey(valueId)) {
				definingRangeDTO = newDefiningRangeDTO(valueId, valueName);
				valueIdToDefiningRangeDTOMap.put(valueId, definingRangeDTO);
			} else {
				definingRangeDTO = valueIdToDefiningRangeDTOMap.get(valueId);
			}
			
			definingRangeDTOList.add(definingRangeDTO);
		}
		
		// set DefiningRangeDTO List per AttributeId
		for (Map.Entry<String, DefiningAttributeDTO> entry : attributeIdToDefiningAttributeDTOMap.entrySet()) {
			DefiningAttributeDTO definingAttributeDTO = entry.getValue();
			definingRangeDTOList = attributeIdToDefiningRangeDTOListMap.get(entry.getKey());
			definingAttributeDTO.setRanges(definingRangeDTOList);
			
			definingAttributeDTOList.add(definingAttributeDTO);
		}
		
		return definingAttributeDTOList;
	}
	
	private static DefiningAttributeDTO newDefiningAttributeDTO(String attributeId, String attributeName, final List<DefiningRangeDTO> definingRangeDTOList) {
		DefiningAttributeDTO dto = new DefiningAttributeDTO();
		dto.setId(attributeId);
		dto.setName(attributeName);
		dto.setRanges(definingRangeDTOList);
		
		return dto;
	}

	private static DefiningRangeDTO newDefiningRangeDTO(final String valueId, final  String valueName) {
		DefiningRangeDTO definingRangeDTO = new DefiningRangeDTO();
		definingRangeDTO.setId(valueId);
		definingRangeDTO.setName(valueName);
		
		return definingRangeDTO;
	}
}
