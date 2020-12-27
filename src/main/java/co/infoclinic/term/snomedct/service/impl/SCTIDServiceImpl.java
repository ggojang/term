package co.infoclinic.term.snomedct.service.impl;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.utils.SNOMEDCTComponentTypeEnum;
import co.infoclinic.term.snomedct.model.entity.SCTID;
import co.infoclinic.term.snomedct.repository.SCTIDRepository;
import co.infoclinic.term.snomedct.service.SCTIDService;

/**
 * SNOMED CT ID 관리 서비스
 * 
 * @author dongwon
 *
 */
@Service("SCTIDService")
@Transactional
public class SCTIDServiceImpl implements SCTIDService {

	@Autowired
	SCTIDRepository sctidRepository;
	
	/**
	 * 새로운 SCTD 생성에 필요한 Extension Item Identifier를 반환하는 메소드
	 * 
	 * @param componentType
	 * @return
	 */
	@Override
	public SCTID getId(SNOMEDCTComponentTypeEnum componentType) {
		// [LongType] Concept: 10 to 0, Description: 11 to 1, Relationship: 12 to 2
		int type = Integer.parseInt(componentType.getLongType().substring(1));
		SCTID reqEntity = new SCTID(), resEntity;
		
		reqEntity.setComponentType(type);
		
		resEntity = sctidRepository.save(reqEntity);
		
		return resEntity;
	}

}
