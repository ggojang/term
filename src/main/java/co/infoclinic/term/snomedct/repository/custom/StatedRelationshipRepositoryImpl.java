package co.infoclinic.term.snomedct.repository.custom;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaContext;

import co.infoclinic.term.common.model.dto.Value;
import co.infoclinic.term.common.utils.SNOMEDCTUtils;
import co.infoclinic.term.snomedct.model.dto.RelationshipViewDTO;
import co.infoclinic.term.snomedct.model.entity.StatedRelationship;
import co.infoclinic.term.snomedct.service.ConceptService;

/**
 * The Stated Relationship Repository for Custom Query
 */
public class StatedRelationshipRepositoryImpl implements StatedRelationshipRepositoryCustom {

	/** Logger */
	Logger log = LoggerFactory.getLogger(StatedRelationshipRepositoryImpl.class);

	private static final String TABLE = "STATED_RELATIONSHIP";
	private static final String RESULTSET_MAPPING = "StatedRelationshipWithName";
	private static final String LANG = "en";
	
	/** JPA Entity Manager */
	private final EntityManager em;
	
	/** Concept Service */
	private final ConceptService conceptSvc;
	
	/** DI: EntityManager, ConceptService */
	@Autowired
	public StatedRelationshipRepositoryImpl(JpaContext context, ConceptService conceptSvc) {
		this.em = context.getEntityManagerByManagedType(StatedRelationship.class);
		this.conceptSvc = conceptSvc;
	}
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.repository.custom.StatedRelationshipRepositoryCustom#findListBySourceIdAndEffectiveTime(java.lang.String, java.lang.String)
	 */
	@Override
	public List<RelationshipViewDTO> findListBySourceIdAndEffectiveTime(String conceptId, String effectiveTime) {
		String qry = getRelationshipListQuery(conceptId, effectiveTime);
		return getResultList(qry, effectiveTime);
	}
	
	
	/**
	 * 
	 * <pre>
	 *   r[0]: inferred relationship object
	 *   r[1]: source name
	 *	 r[2]: destination name
	 *	 r[3]: characteristic type name
	 *	 r[4]: type name
	 *   r[5]: module name
	 *	 r[5]: modifier name
	 * </pre>
	 * 
	 * @param query
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<RelationshipViewDTO> getResultList(String query, String effectiveTime) {
		List<RelationshipViewDTO> dtos = new ArrayList<RelationshipViewDTO>();
		RelationshipViewDTO dto;
		
		Query q = em.createNativeQuery(query, RESULTSET_MAPPING);
		List<Object[]> results = q.getResultList();
		for (Object[] r : results) {
			dto = createDTO(r, effectiveTime);
			dtos.add(dto);
		}
		
		return dtos;
	};
	
	
	/**
	 * Create DTO
	 * 
	 * @param row
	 * @param effectiveTime
	 * @return
	 */
	private RelationshipViewDTO createDTO(Object[] row, String effectiveTime) {
		RelationshipViewDTO dto = new RelationshipViewDTO();
		
		StatedRelationship r;
		r = (StatedRelationship) row[0];
		
		String charTypeName = (String) row[1];
		String moduleName = (String) row[2];
		String modifierName = (String) row[3];
		
		dto.setActive(r.isActive());
		dto.setEffectiveTime(r.getEffectiveTime());
		dto.setModule(new Value(r.getModuleId(), moduleName));
		dto.setSourceId(r.getSourceId());
		dto.setRelationshipGroup(r.getRelationshipGroup());
		dto.setCharacteristicType(new Value(r.getCharacteristicTypeId(), charTypeName));
		dto.setModifier(new Value(r.getModifierId(), modifierName));
		
		dto.setDestination(conceptSvc.getConcept(r.getDestinationId(), effectiveTime));
		dto.setType(conceptSvc.getConcept(r.getTypeId(), effectiveTime));
		
		return dto;
	}
	
	
	/**
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	private String getRelationshipListQuery(String conceptId, String effectiveTime) {
		String qry = "";
		
		qry = "SELECT " +
			  "    R.*, " +
			  "    CTYP.TERM AS CHARACTERISTIC_TYPE_NAME, " +
			  "    MODULE.TERM AS MODULE_NAME, " +
			  "    MODF.TERM AS MODIFIER_NAME " +
			  "FROM ( " +
			  "    SELECT r.* " +
			  "    FROM " + TABLE + " r " +
			  "    INNER JOIN ( " +
			  "        SELECT RELATIONSHIP_ID, MAX(EFFECTIVE_TIME) as MAX_EFFECTIVE_TIME " +
			  "        FROM " + TABLE +
			  "        WHERE SOURCE_ID = '" + conceptId + "' " +
			  "        AND EFFECTIVE_TIME <= '" + effectiveTime + "' " +
			  "        GROUP BY RELATIONSHIP_ID " +
			  "    ) as rj " +
			  "    ON r.RELATIONSHIP_ID = rj.RELATIONSHIP_ID " +
			  "    AND r.SOURCE_ID = '" + conceptId + "' " +
			  "    AND r.EFFECTIVE_TIME = rj.MAX_EFFECTIVE_TIME " +
			  "    AND r.ACTIVE = 1 " +
			  ") AS R ";
		
		// Characteristic Type 이름
		qry += getSpecificColumnJoinQuery(conceptId, effectiveTime, "CHARACTERISTIC_TYPE_ID", "R", "CTYP");
		// 모듈 이름
		qry += getSpecificColumnJoinQuery(conceptId, effectiveTime, "MODULE_ID", "R", "MODULE");
		// Modifier 이름
		qry += getSpecificColumnJoinQuery(conceptId, effectiveTime, "MODIFIER_ID", "R", "MODF");

		return qry;
	}

	
	/**
	 * 모듈 조인 쿼리
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	private String getSpecificColumnJoinQuery(String conceptId, String effectiveTime, String column, String sourceAlias, String targetAlias) {
		String query;
		
		query = "LEFT JOIN ( " +
				"  SELECT CONCEPT_ID, TERM " +
				"  FROM DESCRIPTION AS D " +
				"  INNER JOIN ( " +
				"	SELECT DESCRIPTION_ID, MAX(EFFECTIVE_TIME) AS MAX_ETIME " +
				"    FROM DESCRIPTION AS ID " +
				"    INNER JOIN ( " +
				"		SELECT DISTINCT(" + column + ") " +
				"		FROM " + TABLE +" " +
				"       WHERE SOURCE_ID = '" + conceptId +"' " + 
				"    ) AS DD " +
				"    ON ID.CONCEPT_ID = DD." + column +
				"    WHERE ID.EFFECTIVE_TIME <= '" + effectiveTime + "' " +
				"    AND ID.TYPE_ID = '" + SNOMEDCTUtils.DescriptionType.FullySpecifiedName + "' " +
				"    GROUP BY ID.DESCRIPTION_ID " +
				"  ) AS GD " +
				"  ON D.DESCRIPTION_ID = GD.DESCRIPTION_ID " +
				"  AND D.EFFECTIVE_TIME = GD.MAX_ETIME " +
				"  AND D.ACTIVE = 1 " +
				"  AND D.LANGUAGE_CODE = '" + LANG + "' " +
				") AS "+ targetAlias + " " +
				"ON " + sourceAlias + "." + column + " = " + targetAlias + ".CONCEPT_ID ";
		
		return query;
	}
}
