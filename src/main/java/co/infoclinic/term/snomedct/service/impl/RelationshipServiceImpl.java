package co.infoclinic.term.snomedct.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.infoclinic.term.snomedct.model.dto.RelationshipViewDTO;
import co.infoclinic.term.snomedct.repository.InferredRelationshipRepository;
import co.infoclinic.term.snomedct.repository.StatedRelationshipRepository;
import co.infoclinic.term.snomedct.service.RelationshipService;

/**
 * The Relationship Service
 */
@Service("SCTRelSvc")
@Transactional
public class RelationshipServiceImpl implements RelationshipService {

	/** Logger */
	Logger log = LoggerFactory.getLogger(RelationshipService.class);

	/** DI: Stated Relationship Repository */
	@Autowired
	private StatedRelationshipRepository statedRelationshipRepository;

	/** DI: Inferred Relationship Repository */
	@Autowired
	private InferredRelationshipRepository inferredRelationshipRepository;

	
	
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.RelationshipService#getRelationshipList(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public List<RelationshipViewDTO> getRelationshipList(String conceptId, String effectiveTime, boolean isStated) {

		List<RelationshipViewDTO> dtos;
		if (!isStated) {
			dtos = inferredRelationshipRepository.findListBySourceIdAndEffectiveTime(conceptId, effectiveTime);
		} else {
			dtos = statedRelationshipRepository.findListBySourceIdAndEffectiveTime(conceptId, effectiveTime);
		}
		
		return dtos;
	}


}
