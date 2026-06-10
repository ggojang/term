package co.infoclinic.term.snomedct.service.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.infoclinic.term.common.utils.SNOMEDCTComponentTypeEnum;
import co.infoclinic.term.common.utils.SNOMEDCTIdentifierGenerator;
import co.infoclinic.term.snomedct.service.ConceptEditService;

@Service("ConceptEditService")
@Transactional
public class ConceptEditServiceImpl implements ConceptEditService {
	
	Logger log = LoggerFactory.getLogger(ConceptEditServiceImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> setAllSCTID(Map<String, Object> map) throws Exception {
		// TODO Auto-generated method stub
		List<Map<String, Object>> attrs = (List<Map<String, Object>>) map.get("attributes");
		Map<String, Object> inMap = null;
		for(int i=0; i<attrs.size(); i++) {
			inMap = attrs.get(i);
			inMap.put("conceptId", SNOMEDCTIdentifierGenerator.create(SNOMEDCTComponentTypeEnum.RELATIONSHIP, null));
		}
		
		return map;
	}

}
