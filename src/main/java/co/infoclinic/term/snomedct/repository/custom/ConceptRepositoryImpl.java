package co.infoclinic.term.snomedct.repository.custom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaContext;

import co.infoclinic.term.common.model.dto.Value;
import co.infoclinic.term.common.utils.SNOMEDCTComponentTypeEnum;
import co.infoclinic.term.common.utils.SNOMEDCTUtils;
import co.infoclinic.term.snomedct.model.dto.ConceptViewDTO;
import co.infoclinic.term.snomedct.model.dto.ConceptTreeDTO;
import co.infoclinic.term.snomedct.model.entity.Concept;

/**
 * The Concept Repository for Custom Query
 */
public class ConceptRepositoryImpl implements ConceptRepositoryCustom {

	/** Logger */
	Logger log = LoggerFactory.getLogger(ConceptRepositoryImpl.class);
	
	/** Relationship Table Name */
	private static final String TBL_REL = "INFERRED_RELATIONSHIP_SNAP";
	private static final String COL_ATTR = "TYPE_ID";
	private static final String COL_VAL = "DESTINATION_ID";
	
	private static final String LANG = "en";
	
	/** JPA Entity Manager */
	private final EntityManager em;
	
	/** DI: EntityManager */
	@Autowired
	public ConceptRepositoryImpl(JpaContext context) {
		this.em = context.getEntityManagerByManagedType(Concept.class);
	}
	
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	/**
	 * 한 쌍이상의 속성과 값이 형태(그룹인 경우, 그룹이 아닌경우)를 조건으로 조건과 같이 정의된 혹은 정의의 일부로 포함되는 개념 목록 조회
	 * 
	 * @param map
	 * @param effectiveTime
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Concept> findByAttrVals(Map<String, Object> map, String effectiveTime) {
		List<Concept> entities;
		
		String qry = getRefineEntityQuery(map, effectiveTime);
		Query q = em.createNativeQuery(qry, Concept.class);

		entities = q.getResultList();
		return entities;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.repository.custom.ConceptRepositoryCustom#findByConceptIdAndEffectiveTime(java.lang.String, java.lang.String)
	 */
	@Override
	public ConceptViewDTO findByConceptIdAndEffectiveTime(String conceptId, String effectiveTime) {
		String qry = getEntityQuery(conceptId, effectiveTime);
		return getSingleResult(qry);
	}
	
	@Override
	public ConceptTreeDTO findByConceptIdAndEffectiveTime2(String conceptId, String effectiveTime) {
		String qry = getEntityQuery(conceptId, effectiveTime);
		return getSingleResult2(qry);
	}
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.repository.custom.ConceptRepositoryCustom#findParentListByConceptIdAndEffectiveTime(java.lang.String, java.lang.String)
	 */
	@Override
	public List<ConceptViewDTO> findParentListByConceptIdAndEffectiveTime(String conceptId, String effectiveTime) {
		String qry = getParentListQuery(conceptId, effectiveTime);
		return getResultList(qry);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.repository.custom.ConceptRepositoryCustom#findChildrenByConceptIdAndEffectiveTime(java.lang.String, java.lang.String)
	 */
	@Override
	public List<ConceptViewDTO> findChildrenByConceptIdAndEffectiveTime(String conceptId, String effectiveTime) {
		String qry = getChildrenQuery(conceptId, effectiveTime);
		return getResultList(qry);
	}
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.repository.custom.ConceptRepositoryCustom#findChildrenByConceptIdAndEffectiveTime(java.lang.String, java.lang.String)
	 */
	@Override
	public List<ConceptTreeDTO> findChildrenByConceptIdAndEffectiveTime2(String conceptId, String effectiveTime) {
		String qry = getChildrenQuery2(conceptId, effectiveTime);
		return getResultTree(qry);
	}
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.repository.custom.ConceptRepositoryCustom#findDescendantListByPathsAndEffectiveTime(java.util.List, java.lang.String)
	 */
	@Override
	public List<ConceptViewDTO> findDescendantListByPathsAndEffectiveTime(List<String> paths, String effectiveTime, int offset, int limit) {
		String qry = getDescendantListQuery("", paths, effectiveTime, offset, limit);
		return getResultList(qry);
	}

	@Override
	public List<ConceptViewDTO> findAllDescendantListByPathsAndEffectiveTime(List<String> paths, String effectiveTime) {
		String qry = getAllDescendantListQuery(paths, effectiveTime);
		return getResultList(qry);
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.repository.custom.ConceptRepositoryCustom#findAncestorListByPathsAndEffectiveTime(java.util.List, java.lang.String)
	 */
	@Override
	public List<ConceptViewDTO> findAncestorListByIdsAndEffectiveTime(List<String> ids, String effectiveTime, int offset, int limit) {
		String qry = getAncestorListQuery("", ids, effectiveTime, offset, limit, false);
		return getResultList(qry);
	}


	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.repository.custom.ConceptRepositoryCustom#findAncestorListOrSelfByConceptIdAndPathsAndEffectiveTime(java.lang.String, java.util.List, java.lang.String)
	 */
	@Override
	public List<ConceptViewDTO> findAncestorListOrSelfByConceptIdAndIdsAndEffectiveTime(String conceptId,
			List<String> ids, String effectiveTime, int offset, int limit) {
		String qry = getAncestorListQuery(conceptId, ids, effectiveTime, offset, limit, true);
		return getResultList(qry);
	}
	
	
	
	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------

	/**
	 * Refinement를 만족하는 Entity를 조회하는 쿼리 반환
	 * 
	 * @param map 속성과 값을 표현하는 객체
	 * @param effectiveTime 유효시작시간
	 * @return
	 */
	private String getRefineEntityQuery(Map<String, Object> map, String effectiveTime) {

		// entity query
		String qry = "SELECT C.* " +
					 "FROM ( " +
					 "  SELECT C1.* " +
					 "  FROM CONCEPT AS C1 " +
					 "  INNER JOIN ( " +
					 "    SELECT CONCEPT_ID, MAX(EFFECTIVE_TIME) AS MAX_ETIME " +
					 "    FROM CONCEPT " +
					 "    WHERE EFFECTIVE_TIME <= '" + effectiveTime + "' " +
					 "    GROUP BY CONCEPT_ID " +
					 "  ) AS C2 " +
					 "  ON C1.CONCEPT_ID = C2.CONCEPT_ID " +
					 "  AND C1.EFFECTIVE_TIME = C2.MAX_ETIME " +
					 "  AND C1.ACTIVE = 1 " +
					 ") AS C " +
					 "INNER JOIN ( ";
		
		qry += getRefinementQuery(map, effectiveTime);
		
		qry += ") AS R " +
			   "ON C.CONCEPT_ID = R.SOURCE_ID;";
		
		return qry;
	}
	
	
	/**
	 * Refinement를 만족하는 Entity Id 목록을 조회하는 쿼리 반환
	 * 
	 * <pre>
	 * Syntax: refinement = (attributeSet / attributeGroup) *( ws ["," ws] attributeGroup )
	 * Attribute Group 또는 Attribute Set(non-group)내의 조건(Attribute와 Value)을 만족하는 아이디를 조회하는 쿼리 생성
	 * </pre>
	 * 
	 * @param map
	 * @param effectiveTime
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String getRefinementQuery(Map<String, Object> map, String effectiveTime) {
		List<String> qrys = new ArrayList<String>();
		
		// map의 키
		String k;
		// map의 값
		Object v;
		// Non-Group 여부
		boolean isNonGroup = false;
		// Group 여부
		boolean isGroup = false;
		// SCTID 여부
		boolean isSctId = false;
		
		Iterator<String> itr = map.keySet().iterator();
		while (itr.hasNext()) {
			// key
			k = itr.next();
			// value
			v = map.get(k);
			
			// 키값이 Non-Group인지 판단
			isNonGroup = "n".equals(k.toLowerCase()) && v instanceof Map;
			// 키값이 Group인지 판단
			isGroup = "g".equals(k.toLowerCase()) && v instanceof List;
			// 키값이 SNOMED CT ID인지 판단(Attribute Id를 의미함)
			isSctId = !SNOMEDCTComponentTypeEnum.UNKNOWN.equals(SNOMEDCTComponentTypeEnum.getById(k));
			
			// if. 키값이 Non-Group인 경우
			if (isNonGroup) {
				qrys.add(getAttrSetQuery((Map<String, Object>) v));
			}
			// if. 키값이 Group인 경우
			else if (isGroup) {
				qrys.add(getAttrGroupQuery((List<Map<String, Object>>) v));
			}
			// if. 키값이 SNOMED CT ID인 경우(Attribute Id를 의미함)
			else if (isSctId) {
				qrys.add(getAttrValQuery(k, (String) v, false));
			}
			// else. 위의 조건에 걸리지 않았다면
			else {
				// TODO: Error
				log.error("ConceptRepositoryImpl:getRelQuery, ERROR! Do not find condition");
			}
		}
		
		return joinRelationshipQuery(qrys);
	}
	
	
	/**
	 * 각 Group, Non-Group내의 조건(Attribute와 Value)들을 하나의 쿼리로 결합 후 반환
	 * 
	 * @param qrys
	 * @return
	 */
	private String joinRelationshipQuery(List<String> pieceQrys) {
		String qry = "";
		int pieceQrysSize = pieceQrys.size();
		
		// 1번째[0] 쿼리는 FROM절에 위치
		qry = "SELECT DISTINCT(P.SOURCE_ID) " +
			  "FROM ( ";
		qry += pieceQrys.get(0);
		qry += ") AS P ";
		
		// 2번째[1] 쿼리는 INNER JOIN절에 위치
		for (int i = 1; i < pieceQrysSize; i++) {
			// i번째 쿼리
			String pieceQry = pieceQrys.get(i);
			// 이너조인 쿼리
			String joinQry = "INNER JOIN ( ";
			joinQry += pieceQry;
			joinQry += ") AS P" + (i) + " ";
			joinQry += "ON P.SOURCE_ID = P" + (i++) + ".SOURCE_ID ";
			
			// 쿼리에 이너조인 쿼리반영
			qry += joinQry;
		}
		
		return qry;
	}
	
	
	/**
	 * 그룹내의 속성과 값을 검색하는 쿼리 반환
	 * 
	 * <pre>
	 * SELECT DISTINCT(G1.SOURCE_ID)
	 *	FROM (
	 *		SELECT DISTINCT(A1.SOURCE_ID)
	 *		FROM (
	 *			SELECT r.SOURCE_ID, r.RELATIONSHIP_GROUP
	 *			FROM INFERRED_RELATIONSHIP_SNAP r 
	 *			WHERE (r.TYPE_ID = '260686004' AND r.DESTINATION_ID = '129287005')  # 260686004|Method (attribute)| = 129287005|Incision - action (qualifier value)|
	 *		) AS A1
	 *		INNER JOIN (
	 *			SELECT r.SOURCE_ID, r.RELATIONSHIP_GROUP
	 *			FROM INFERRED_RELATIONSHIP_SNAP r 
	 *			WHERE (r.TYPE_ID = '405813007' AND r.DESTINATION_ID = '79741001')  # 405813007|Procedure site - Direct (attribute)| = 79741001|Common bile duct structure (body structure)| 
	 *		) AS A2
	 *		ON A2.SOURCE_ID = A1.SOURCE_ID
	 *		AND A2.RELATIONSHIP_GROUP = A1.RELATIONSHIP_GROUP
	 *	) AS G1 -- 그룹1
	 *	INNER JOIN (
	 *		SELECT DISTINCT(A1.SOURCE_ID)
	 *		FROM (
	 *			SELECT r.SOURCE_ID, r.RELATIONSHIP_GROUP
	 *			FROM INFERRED_RELATIONSHIP_SNAP r 
	 *			WHERE (r.TYPE_ID = '260686004' AND r.DESTINATION_ID = '281615006')  # 260686004|Method (attribute)| = 281615006|Exploration - action (qualifier value)|
	 *		) AS A1
	 *		INNER JOIN (
	 *			SELECT r.SOURCE_ID, r.RELATIONSHIP_GROUP
	 *			FROM INFERRED_RELATIONSHIP_SNAP r 
	 *			WHERE (r.TYPE_ID = '405813007' AND r.DESTINATION_ID = '79741001')  # 405813007|Procedure site - Direct (attribute)| = 79741001|Common bile duct structure (body structure)| 
	 *		) AS A2
	 *		ON A2.SOURCE_ID = A1.SOURCE_ID
	 *		AND A2.RELATIONSHIP_GROUP = A1.RELATIONSHIP_GROUP
	 *	) AS G2 -- 그룹2
	 *	ON G1.SOURCE_ID = G2.SOURCE_ID; -- 그룹1과 그룹2간 일치하는 소스아이디만
	 * ...
	 * </pre>
	 * 
	 * 
	 * @param grps
	 * @return
	 */
	private String getAttrGroupQuery(List<Map<String, Object>> groups) {
		
		List<String> qrys = new ArrayList<String>();
		
		String qry = "";
		
		Map<String, Object> group;
		int groupsSize = groups.size();
		
		List<Entry<String, Object>> list;
		int listSize = 0;
		
		Entry<String, Object> e;
		String eAttr;
		String eVal;
		
		for (int i = 0; i < groupsSize; i++) {
			group = groups.get(i);
			list = new ArrayList<Entry<String, Object>>(group.entrySet());
			listSize = list.size();
			
			e = list.get(0);
			eAttr = e.getKey();
			eVal = String.valueOf(e.getValue());
			
			// 1번째[0] 쿼리는 FROM절에 위치
			qry = "SELECT DISTINCT(A.SOURCE_ID) " +
				  "FROM ( " +
				  "  SELECT SOURCE_ID, RELATIONSHIP_GROUP " +
				  "  FROM " + TBL_REL + " " +
				  "  WHERE " + COL_ATTR + " = '" + eAttr + "' AND " + COL_VAL + " = '" + eVal + "' " +
				  "  AND RELATIONSHIP_GROUP > 0 " +
				  ") AS A ";
			
			// 2번째[1] 쿼리는 INNER JOIN절에 위치
			for (int j = 1; j < listSize; j++) {
				// i번째 Entry
				e = list.get(j);
				eAttr = e.getKey();
				eVal = String.valueOf(e.getValue());
				
				// 이너조인 쿼리
				String joinQry = "INNER JOIN ( " +
								 "  SELECT SOURCE_ID, RELATIONSHIP_GROUP " +
								 "  FROM " + TBL_REL + " " +
								 "  WHERE " + COL_ATTR + " = '" + eAttr + "' AND " + COL_VAL + " = '" + eVal + "' " +
								 "  AND RELATIONSHIP_GROUP > 0 " +
								 ") AS A" + (j) + " " +
								 "ON A.SOURCE_ID = A" + (j) + ".SOURCE_ID " +
								 "AND A.RELATIONSHIP_GROUP = A" + (j++) + ".RELATIONSHIP_GROUP ";
				
				// 쿼리에 이너조인 쿼리반영
				qry += joinQry;
			}
			
			qrys.add(qry);
		}
		
		return joinRelationshipQuery(qrys);
	}
	
	
	/**
	 * 그룹이 없는 속성과 값을 검색하는 쿼리 반환
	 * 
	 * <pre>
	 * SELECT DISTINCT(A1.SOURCE_ID)
	 *	FROM (
	 *		SELECT SOURCE_ID
	 *		FROM INFERRED_RELATIONSHIP_SNAP
	 *		WHERE TYPE_ID = '363702006' AND DESTINATION_ID = '307132003'  # 363702006|Has focus (attribute)| = 307132003|Common bile duct calculus (disorder)|
	 *		AND RELATIONSHIP_GROUP = 0
	 *	) AS A1
	 *	INNER JOIN (
	 *	
	 *	) AS A2
	 *	ON A1.SOURCE_ID = A2.SOURCE_ID
	 *	...
	 * </pre>
	 * 
	 * @param attrObjMap
	 * @return
	 */
	private String getAttrSetQuery(Map<String, Object> attrObjMap) {
		// key: String
		// Val: String
		
		String qry = "";
		
		List<Entry<String, Object>> list = new ArrayList<Entry<String, Object>>(attrObjMap.entrySet());
		int listSize = list.size();
		
		Entry<String, Object> e = list.get(0);
		String eAttr = e.getKey();
		String eVal = String.valueOf(e.getValue());
		
		// 1번째[0] 쿼리는 FROM절에 위치
		// ECL 비그룹 속성 제약은 ANY relationship group에서 속성-값 쌍을 매칭 (group 0 포함)
		qry = "SELECT DISTINCT(A.SOURCE_ID) " +
			  "FROM ( " +
			  "  SELECT SOURCE_ID " +
			  "  FROM " + TBL_REL + " " +
			  "  WHERE " + COL_ATTR + " = '" + eAttr + "' AND " + COL_VAL + " = '" + eVal + "' " +
			  "  AND ACTIVE = 1 " +
			  ") AS A ";

		// 2번째[1] 쿼리는 INNER JOIN절에 위치
		for (int i = 1; i < listSize; i++) {
			// i번째 Entry
			e = list.get(i);
			eAttr = e.getKey();
			eVal = String.valueOf(e.getValue());

			// 이너조인 쿼리
			String joinQry = "INNER JOIN ( " +
							 "  SELECT SOURCE_ID " +
							 "  FROM " + TBL_REL + " " +
							 "  WHERE " + COL_ATTR + " = '" + eAttr + "' AND " + COL_VAL + " = '" + eVal + "' " +
							 "  AND ACTIVE = 1 " +
							 ") AS A" + (i) + " " +
							 "ON A.SOURCE_ID = A" + (i++) + ".SOURCE_ID ";
			
			// 쿼리에 이너조인 쿼리반영
			qry += joinQry;
		}
		
		return qry;
	}
	
	
	
	/**
	 * 속성과 값을 가진 엔티티 아이디 목록을 찾는 쿼리 반환
	 * 
	 * <pre>
	 * 호출: getAttrValQuery(attrId, valId, isGroup)
	 * 반환: SELECT SOURCE_ID, RELATIONSHIP_GROUP
	 *      FROM INFERRED_RELATIONSHIP
	 *      WHERE TYPE_ID = "" AND DESTINATION_ID = ""
	 *      --- if group
	 *      AND RELATIONSHIP_GROUP != 0
	 *      --- if non-group
	 *      AND RELATIONSHIP_GROUP = 0
	 * </pre>
	 * 
	 * @param attrId
	 * @param valId
	 * @param isGroup
	 * 
	 * @return
	 */
	private String getAttrValQuery(String attrId, String valId, boolean isGroup) {
		String qry = "SELECT SOURCE_ID, RELATIONSHIP_GROUP " +
				     "FROM INFERRED_RELATIONSHIP " +
				     "WHERE " + getAttrValStmt(attrId, valId) +
				     "AND RELATIONSHIP_GROUP ";
		// 그룹 내의 속성과 값일 경우 그룹번호가 0이 아니여야하고, 그룹이 아닐 경우 그룹번호는 0이어야 한다.
		qry += isGroup ? "!= 0 ":"= 0 ";
		
		return qry;
	};
	
	
	/**
	 * 속성과 값에 대한 조건 문장 반환
	 * 
	 * <pre>
	 * 예: 405813007|Procedure site - Direct (attribute)| = 79741001|Common bile duct structure (body structure)| 
	 * 
	 * 호출: getAttrValStmt("405813007", "79741001")
	 * 결과: TYPE_ID=405813007 AND DESTINATION_ID=79741001
	 * </pre>
	 * 
	 * @param attrId
	 * @param valId
	 * @return
	 */
	private String getAttrValStmt(String attrId, String valId) {
		return COL_ATTR + " = '" + attrId + "' AND " + COL_VAL + " = '" + valId + "' ";	
	}
	
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	private ConceptViewDTO getSingleResult(String query) {
		ConceptViewDTO dto;
		
		Query q = em.createNativeQuery(query, "ConceptWithHierarchy");
		
		Object[] result;
		
		try {
			result = (Object[]) q.getSingleResult();
			if (result[4] == null)
				result[4] = 0;
			if (result[5] == null)
				result[5] =0;
		} catch (NonUniqueResultException e) {
			result = null;
		} 

		dto = createDTO(result);
		return dto;
	};
	
	private ConceptTreeDTO getSingleResult2(String query) {
		ConceptTreeDTO dto;
		
		Query q = em.createNativeQuery(query, "ConceptWithHierarchy");
		
		Object[] result;
		
		try {
			result = (Object[]) q.getSingleResult();
			if (result[4] == null)
				result[4] = 0;
			if (result[5] == null)
				result[5] =0;
		} catch (NonUniqueResultException e) {
			result = null;
		} 

		dto = createTreeDTO(result);
		return dto;
	};
	
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<ConceptViewDTO> getResultList(String query) {
		List<ConceptViewDTO> dtos = new ArrayList<ConceptViewDTO>();
		ConceptViewDTO dto;
		
		Query q = em.createNativeQuery(query, "ConceptWithHierarchy");
		List<Object[]> results = q.getResultList();
		for (Object[] r : results) {
			if (r != null && r.length == 6) {
				dto = createDTO(r);
				dtos.add(dto);
			}
		}
		
		return dtos;
	};
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<ConceptTreeDTO> getResultTree(String query) {
		List<ConceptTreeDTO> dtos = new ArrayList<ConceptTreeDTO>();
		ConceptTreeDTO dto;
		
		Query q = em.createNativeQuery(query, "ConceptWithHierarchy");
		List<Object[]> results = q.getResultList();
		for (Object[] r : results) {
			if (r != null && r.length == 6) {
				dto = createTreeDTO(r);
				dtos.add(dto);
			}
		}
		
		return dtos;
	};
	
	/**
	 * Tree DTO 객체 생성
	 * 
	 */
	private ConceptTreeDTO createTreeDTO(Object[] row) {
		if (row == null || row.length != 6) {
			return new ConceptTreeDTO();
		}
		
		ConceptTreeDTO dto = new ConceptTreeDTO();
		
		Concept concept;
		concept = (Concept) row[0];
		
		String fsn = (String) row[1];
		String moduleName = (String) row[2];
		String defName = (String) row[3];
		int childrenCount = Integer.valueOf(String.valueOf(row[4]));
		int descendantCount = Integer.valueOf(String.valueOf(row[5]));
		
		dto = new ConceptTreeDTO();
		dto.setConceptId(concept.getConceptId());
		
		String term = "";
		String semTag = "";
		try {
			term = fsn.substring(0, fsn.lastIndexOf("(") - 1);
			semTag = fsn.substring(fsn.lastIndexOf("(") + 1, fsn.length() - 1);
		} catch (java.lang.StringIndexOutOfBoundsException ex) {
			term = fsn;
			semTag = "";
		} finally {
			dto.setTerm(term);
			dto.setSemanticTag(semTag);
		}
		
		dto.setEffectiveTime(concept.getEffectiveTime());
		dto.setActive(concept.isActive());
		dto.setModule(new Value(concept.getModuleId(), moduleName));
		dto.setDefinitionStatus(new Value(concept.getDefinitionStatusId(), defName));
		dto.setChildrenCount(childrenCount);
		dto.setDescendantCount(descendantCount);
		
		return dto;
	}
	
	
	/**
	 * DTO 객체 생성
	 * 
	 * <pre>
	 *   r[0]: concept object
	 *   r[1]: term
	 *	 r[2]: module name
	 *	 r[3]: definition status name
	 *	 r[4]: children count
	 *	 r[5]: descendant count
	 * </pre>
	 * 
	 * @param row
	 * @return
	 */
	private ConceptViewDTO createDTO(Object[] row) {
		if (row == null || row.length != 6) {
			return new ConceptViewDTO();
		}
		
		ConceptViewDTO dto = new ConceptViewDTO();
		
		Concept concept;
		concept = (Concept) row[0];
		
		String fsn = (String) row[1];
		String moduleName = (String) row[2];
		String defName = (String) row[3];
		int childrenCount = Integer.valueOf(String.valueOf(row[4]));
		int descendantCount = Integer.valueOf(String.valueOf(row[5]));
		
		dto = new ConceptViewDTO();
		dto.setConceptId(concept.getConceptId());
		
		String term = "";
		String semTag = "";
		try {
			term = fsn.substring(0, fsn.lastIndexOf("(") - 1);
			semTag = fsn.substring(fsn.lastIndexOf("(") + 1, fsn.length() - 1);
		} catch (java.lang.StringIndexOutOfBoundsException ex) {
			term = fsn;
			semTag = "";
		} finally {
			dto.setTerm(term);
			dto.setSemanticTag(semTag);
		}
		
		dto.setEffectiveTime(concept.getEffectiveTime());
		dto.setActive(concept.isActive());
		dto.setModule(new Value(concept.getModuleId(), moduleName));
		dto.setDefinitionStatus(new Value(concept.getDefinitionStatusId(), defName));
		dto.setChildrenCount(childrenCount);
		dto.setDescendantCount(descendantCount);
		
		return dto;
	}
	
	
	/**
	 * 엔티티 조회 쿼리
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	private String getEntityQuery(String conceptId, String effectiveTime) {
		String qry;

		qry = "SELECT C.*, D.TERM, MODULE.TERM AS MODULE_NAME, DEF.TERM AS DEF_NAME, T.CHILDREN_COUNT, T.DESCENDANT_COUNT " +
				"FROM ( " +
				"  SELECT C1.* " +
				"  FROM CONCEPT AS C1 " +
				"  INNER JOIN ( " +
				"    SELECT IC.CONCEPT_ID, MAX(IC.EFFECTIVE_TIME) AS MAX_ETIME " +
				"    FROM CONCEPT AS IC " +
				"    WHERE IC.CONCEPT_ID = '" + conceptId + "' " +
				"      AND IC.EFFECTIVE_TIME <= '" + effectiveTime + "' " +
				"    GROUP BY IC.CONCEPT_ID " +
				"  ) AS C2 " +
				"  ON C1.CONCEPT_ID = C2.CONCEPT_ID " +
				"    AND C1.EFFECTIVE_TIME = C2.MAX_ETIME " +
				// inactive concept 도 검색하도록 아래 줄 comment 처리 20191007 by Yu
				//"  AND C1.ACTIVE = 1 " +
				") AS C " +
				"INNER JOIN ( " +
				"  SELECT D.CONCEPT_ID, D.TERM " + 
				"  FROM DESCRIPTION AS D " +
				"  INNER JOIN ( " +
				"	 SELECT ID.DESCRIPTION_ID, MAX(ID.EFFECTIVE_TIME) AS MAX_ETIME " +
				"      FROM DESCRIPTION AS ID " +
				"      WHERE ID.CONCEPT_ID = '" + conceptId + "' " +
				"        AND ID.EFFECTIVE_TIME <= '" + effectiveTime + "' " +
				"        AND ID.TYPE_ID = '" + SNOMEDCTUtils.DescriptionType.FullySpecifiedName + "' " +
				"      GROUP BY ID.DESCRIPTION_ID " +
				"  ) AS GD " +
				"  ON D.DESCRIPTION_ID = GD.DESCRIPTION_ID " +
				"    AND D.EFFECTIVE_TIME = GD.MAX_ETIME " +
				"    AND D.ACTIVE = 1 " +
				"    AND D.LANGUAGE_CODE = '" + LANG + "' " +
				"	GROUP BY D.CONCEPT_ID, D.TERM" + // add 2022,2,22 by Yu; D.TERM added for PostgreSQL compatibility
				") AS D " +
				"ON C.CONCEPT_ID = D.CONCEPT_ID " +
				"LEFT JOIN ( " +
				"  SELECT DISTINCT(CONCEPT_ID), CHILDREN_COUNT, DESCENDANT_COUNT " +
				"  FROM TC " +
				"  WHERE CONCEPT_ID = '" + conceptId + "' " +
				") AS T " +
				"ON C.CONCEPT_ID = T.CONCEPT_ID ";
				
		qry += getModuleJoinQuery(effectiveTime);
		qry += getDefinitionStatusJoinQuery(effectiveTime);
		
		return qry;
	}
	
	
	/**
	 * 부모 목록 조회 쿼리
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	private String getParentListQuery(String conceptId, String effectiveTime) {
		String qry;
		String closureQry;
		
		closureQry = "SELECT DISTINCT(PARENT_ID) AS CONCEPT_ID, CHILDREN_COUNT, DESCENDANT_COUNT " +
					 "FROM TC " +
			 		 "WHERE CONCEPT_ID = '" + conceptId + "' ";
		
		//closureQry += "ORDER BY PATH ";
		
		qry = "SELECT C.*, D.TERM, MODULE.TERM AS MODULE_NAME, DEF.TERM AS DEF_NAME, T.CHILDREN_COUNT, T.DESCENDANT_COUNT " +
				"FROM ( " +
				"  SELECT C1.* " +
				"  FROM CONCEPT AS C1 " +
				"  INNER JOIN ( " +
				"    SELECT IC.CONCEPT_ID, MAX(IC.EFFECTIVE_TIME) AS MAX_ETIME " +
				"    FROM CONCEPT AS IC " +
				"    INNER JOIN ( ";
				
		qry += closureQry;
		
		qry +=	"	) AS T " +
				"	ON IC.CONCEPT_ID = T.CONCEPT_ID " +
				"    WHERE IC.EFFECTIVE_TIME <= '" + effectiveTime + "' " +
				"    GROUP BY IC.CONCEPT_ID " +
				"  ) AS C2 " +
				"  ON C1.CONCEPT_ID = C2.CONCEPT_ID " +
				"  AND C1.EFFECTIVE_TIME = C2.MAX_ETIME " +
				"  AND C1.ACTIVE = 1 " +
				") AS C " +
				"INNER JOIN ( " +
				"  SELECT D.CONCEPT_ID, D.TERM " +
				"  FROM DESCRIPTION AS D " +
				"  INNER JOIN ( " +
				"	SELECT ID.DESCRIPTION_ID, MAX(ID.EFFECTIVE_TIME) AS MAX_ETIME " +
				"    FROM DESCRIPTION AS ID " +
				"    INNER JOIN ( ";
		
		qry += closureQry;
		
		qry +=	"    ) AS DD " +
				"    ON ID.CONCEPT_ID = DD.CONCEPT_ID " +
				"    WHERE ID.EFFECTIVE_TIME <= '" + effectiveTime + "' " +
				"    AND ID.TYPE_ID = '" + SNOMEDCTUtils.DescriptionType.FullySpecifiedName + "' " +
				"    GROUP BY ID.DESCRIPTION_ID " +
				"  ) AS GD " +
				"  ON D.DESCRIPTION_ID = GD.DESCRIPTION_ID " +
				"  AND D.EFFECTIVE_TIME = GD.MAX_ETIME " +
				"  AND D.ACTIVE = 1 " +
				"  AND D.LANGUAGE_CODE = '" + LANG + "' " +
				"  GROUP BY D.CONCEPT_ID, D.TERM" + // PostgreSQL: all SELECT columns must appear in GROUP BY
				") AS D " +
				"ON C.CONCEPT_ID = D.CONCEPT_ID " +
				"LEFT JOIN ( ";
		qry += closureQry;
		qry +=  ") AS T " +
				"ON C.CONCEPT_ID = T.CONCEPT_ID ";
				
		qry += getModuleJoinQuery(effectiveTime);
		qry += getDefinitionStatusJoinQuery(effectiveTime);
		
		return qry;
	}
	
	
	/**
	 * 자식 목록 조회 쿼리
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	private String getChildrenQuery(String conceptId, String effectiveTime) {
		String qry;
		String closureQry;
		
		closureQry = "SELECT DISTINCT(CONCEPT_ID), CHILDREN_COUNT, DESCENDANT_COUNT " +
					 "FROM TC " +
			 		 "WHERE PARENT_ID = '" + conceptId + "' ";
		
		//closureQry += "ORDER BY PATH ";
		
		qry = "SELECT C.*, D.TERM, MODULE.TERM AS MODULE_NAME, DEF.TERM AS DEF_NAME, T.CHILDREN_COUNT, T.DESCENDANT_COUNT " +
				"FROM ( " +
				"  SELECT C1.* " +
				"  FROM CONCEPT AS C1 " +
				"  INNER JOIN ( " +
				"    SELECT IC.CONCEPT_ID, MAX(IC.EFFECTIVE_TIME) AS MAX_ETIME " +
				"    FROM CONCEPT AS IC " +
				"    INNER JOIN ( ";
				
		qry += closureQry;
		
		qry +=	"	) AS T " +
				"	ON IC.CONCEPT_ID = T.CONCEPT_ID " +
				"    WHERE IC.EFFECTIVE_TIME <= '" + effectiveTime + "' " +
				"    GROUP BY IC.CONCEPT_ID " +
				"  ) AS C2 " +
				"  ON C1.CONCEPT_ID = C2.CONCEPT_ID " +
				"  AND C1.EFFECTIVE_TIME = C2.MAX_ETIME " +
				"  AND C1.ACTIVE = 1 " +
				") AS C " +
				"INNER JOIN ( " +
				"  SELECT D.CONCEPT_ID, D.TERM " +
				"  FROM DESCRIPTION AS D " +
				"  INNER JOIN ( " +
				"	SELECT ID.DESCRIPTION_ID, MAX(ID.EFFECTIVE_TIME) AS MAX_ETIME " +
				"    FROM DESCRIPTION AS ID " +
				"    INNER JOIN ( ";
		
		qry += closureQry;
		
		qry +=	"    ) AS DD " +
				"    ON ID.CONCEPT_ID = DD.CONCEPT_ID " +
				"    WHERE ID.EFFECTIVE_TIME <= '" + effectiveTime + "' " +
				"    AND ID.TYPE_ID = '" + SNOMEDCTUtils.DescriptionType.FullySpecifiedName + "' " +
				"    GROUP BY ID.DESCRIPTION_ID " +
				"  ) AS GD " +
				"  ON D.DESCRIPTION_ID = GD.DESCRIPTION_ID " +
				"  AND D.EFFECTIVE_TIME = GD.MAX_ETIME " +
				"  AND D.ACTIVE = 1 " +
				"  AND D.LANGUAGE_CODE = '" + LANG + "' " +
				"  GROUP BY D.CONCEPT_ID, D.TERM" + // PostgreSQL: all SELECT columns must appear in GROUP BY
				") AS D " +
				"ON C.CONCEPT_ID = D.CONCEPT_ID " +
				"LEFT JOIN ( ";
		qry += closureQry;
		qry +=  ") AS T " +
				"ON C.CONCEPT_ID = T.CONCEPT_ID ";
				
		qry += getModuleJoinQuery(effectiveTime);
		qry += getDefinitionStatusJoinQuery(effectiveTime);
		
		return qry;
	}

	private String getChildrenQuery2(String conceptId, String effectiveTime) {
		String qry;
		String closureQry;
		
		closureQry = "SELECT DISTINCT(CONCEPT_ID), CHILDREN_COUNT, DESCENDANT_COUNT " +
					 "FROM TC " +
			 		 "WHERE PARENT_ID = '" + conceptId + "' ";
			 		
		
		//closureQry += "ORDER BY PATH ";
		
		qry = "SELECT C.*, D.TERM, MODULE.TERM AS MODULE_NAME, DEF.TERM AS DEF_NAME, T.CHILDREN_COUNT, T.DESCENDANT_COUNT " +
				"FROM ( " +
				"  SELECT C1.* " +
				"  FROM CONCEPT AS C1 " +
				"  INNER JOIN ( " +
				"    SELECT IC.CONCEPT_ID, MAX(IC.EFFECTIVE_TIME) AS MAX_ETIME " +
				"    FROM CONCEPT AS IC " +
				"    INNER JOIN ( ";
				
		qry += closureQry;
		
		qry +=	"	) AS T " +
				"	ON IC.CONCEPT_ID = T.CONCEPT_ID " +
				"    WHERE IC.EFFECTIVE_TIME <= '" + effectiveTime + "' " +
				"    GROUP BY IC.CONCEPT_ID " +
				"  ) AS C2 " +
				"  ON C1.CONCEPT_ID = C2.CONCEPT_ID " +
				"  AND C1.EFFECTIVE_TIME = C2.MAX_ETIME " +
				"  AND C1.ACTIVE = 1 " +
				") AS C " +
				"INNER JOIN ( " +
				"  SELECT D.CONCEPT_ID, D.TERM " +
				"  FROM DESCRIPTION AS D " +
				"  INNER JOIN ( " +
				"	SELECT ID.DESCRIPTION_ID, MAX(ID.EFFECTIVE_TIME) AS MAX_ETIME " +
				"    FROM DESCRIPTION AS ID " +
				"    INNER JOIN ( ";
		
		qry += closureQry;
		
		qry +=	"    ) AS DD " +
				"    ON ID.CONCEPT_ID = DD.CONCEPT_ID " +
				"    WHERE ID.EFFECTIVE_TIME <= '" + effectiveTime + "' " +
				"    AND ID.TYPE_ID = '" + SNOMEDCTUtils.DescriptionType.FullySpecifiedName + "' " +
				"    GROUP BY ID.DESCRIPTION_ID " +
				"  ) AS GD " +
				"  ON D.DESCRIPTION_ID = GD.DESCRIPTION_ID " +
				"  AND D.EFFECTIVE_TIME = GD.MAX_ETIME " +
				"  AND D.ACTIVE = 1 " +
				"  AND D.LANGUAGE_CODE = '" + LANG + "' " +
				"  GROUP BY D.CONCEPT_ID, D.TERM" + // PostgreSQL: all SELECT columns must appear in GROUP BY
				") AS D " +
				"ON C.CONCEPT_ID = D.CONCEPT_ID " +
				"LEFT JOIN ( ";
		qry += closureQry;
		qry +=  ") AS T " +
				"ON C.CONCEPT_ID = T.CONCEPT_ID ";
				
		qry += getModuleJoinQuery(effectiveTime);
		qry += getDefinitionStatusJoinQuery(effectiveTime);
		
		return qry;
	}
	
	/**
	 * 
	 * @param paths
	 * @param effectiveTime
	 * @param offset
	 * @param limit
	 * @return
	 */
	private String getDescendantListQuery(String conceptId, List<String> paths, String effectiveTime, int offset, int limit) {
		if (CollectionUtils.isEmpty(paths)) {
			return "";
		}
		
		String qry;
		String closureQry;
		int pathsSize = paths.size();
		
		closureQry = "SELECT DISTINCT(CONCEPT_ID), CHILDREN_COUNT, DESCENDANT_COUNT " +
					 "FROM TC " +
			 		 "WHERE PATH LIKE '" + paths.get(0) + "%' ";
		
		for (int i = 1; i < pathsSize; i++) {
			closureQry += "OR PATH LIKE '" + paths.get(i) + "%' "; 
		}
		
		closureQry += "LIMIT " + limit +  " " +
					  "OFFSET " + offset + " ";

		qry = "SELECT C.*, D.TERM, MODULE.TERM AS MODULE_NAME, DEF.TERM AS DEF_NAME, T.CHILDREN_COUNT, T.DESCENDANT_COUNT " +
				"FROM ( " +
				"  SELECT C1.* " +
				"  FROM CONCEPT AS C1 " +
				"  INNER JOIN ( " +
				"    SELECT IC.CONCEPT_ID, MAX(IC.EFFECTIVE_TIME) AS MAX_ETIME " +
				"    FROM CONCEPT AS IC " +
				"    INNER JOIN ( ";
				
		qry += closureQry;
		
		qry +=	"	) AS T " +
				"	ON IC.CONCEPT_ID = T.CONCEPT_ID " +
				"    WHERE IC.EFFECTIVE_TIME <= '" + effectiveTime + "' " +
				"    GROUP BY IC.CONCEPT_ID " +
				"  ) AS C2 " +
				"  ON C1.CONCEPT_ID = C2.CONCEPT_ID " +
				"  AND C1.EFFECTIVE_TIME = C2.MAX_ETIME " +
				"  AND C1.ACTIVE = 1 " +
				") AS C " +
				"INNER JOIN ( " +
				"  SELECT D.CONCEPT_ID, D.TERM " +
				"  FROM DESCRIPTION AS D " +
				"  INNER JOIN ( " +
				"	SELECT ID.DESCRIPTION_ID, MAX(ID.EFFECTIVE_TIME) AS MAX_ETIME " +
				"    FROM DESCRIPTION AS ID " +
				"    INNER JOIN ( ";
		
		qry += closureQry;
		
		qry +=	"    ) AS DD " +
				"    ON ID.CONCEPT_ID = DD.CONCEPT_ID " +
				"    WHERE ID.EFFECTIVE_TIME <= '" + effectiveTime + "' " +
				"    AND ID.TYPE_ID = '" + SNOMEDCTUtils.DescriptionType.FullySpecifiedName + "' " +
				"    GROUP BY ID.DESCRIPTION_ID " +
				"  ) AS GD " +
				"  ON D.DESCRIPTION_ID = GD.DESCRIPTION_ID " +
				"  AND D.EFFECTIVE_TIME = GD.MAX_ETIME " +
				"  AND D.ACTIVE = 1 " +
				"  AND D.LANGUAGE_CODE = '" + LANG + "' " +
				"  GROUP BY D.CONCEPT_ID, D.TERM" + // PostgreSQL: all SELECT columns must appear in GROUP BY
				") AS D " +
				"ON C.CONCEPT_ID = D.CONCEPT_ID " +
				"LEFT JOIN ( ";
		qry += closureQry;
		qry +=  ") AS T " +
				"ON C.CONCEPT_ID = T.CONCEPT_ID ";
				
		qry += getModuleJoinQuery(effectiveTime);
		qry += getDefinitionStatusJoinQuery(effectiveTime);
		
		return qry;
	}


	/** LIMIT 없이 전체 하위 목록 조회 쿼리 (ECL 전용) */
	private String getAllDescendantListQuery(List<String> paths, String effectiveTime) {
		if (CollectionUtils.isEmpty(paths)) return "";

		String closureQry = "SELECT DISTINCT(CONCEPT_ID), CHILDREN_COUNT, DESCENDANT_COUNT " +
							"FROM TC " +
							"WHERE PATH LIKE '" + paths.get(0) + "%' ";
		for (int i = 1; i < paths.size(); i++) {
			closureQry += "OR PATH LIKE '" + paths.get(i) + "%' ";
		}
		// LIMIT 없음

		String qry = "SELECT C.*, D.TERM, MODULE.TERM AS MODULE_NAME, DEF.TERM AS DEF_NAME, T.CHILDREN_COUNT, T.DESCENDANT_COUNT " +
				"FROM ( " +
				"  SELECT C1.* " +
				"  FROM CONCEPT AS C1 " +
				"  INNER JOIN ( " +
				"    SELECT IC.CONCEPT_ID, MAX(IC.EFFECTIVE_TIME) AS MAX_ETIME " +
				"    FROM CONCEPT AS IC " +
				"    INNER JOIN ( ";
		qry += closureQry;
		qry +=	"    ) AS T " +
				"    ON IC.CONCEPT_ID = T.CONCEPT_ID " +
				"    WHERE IC.EFFECTIVE_TIME <= '" + effectiveTime + "' " +
				"    GROUP BY IC.CONCEPT_ID " +
				"  ) AS C2 " +
				"  ON C1.CONCEPT_ID = C2.CONCEPT_ID " +
				"  AND C1.EFFECTIVE_TIME = C2.MAX_ETIME " +
				"  AND C1.ACTIVE = 1 " +
				") AS C " +
				"INNER JOIN ( " +
				"  SELECT D.CONCEPT_ID, D.TERM " +
				"  FROM DESCRIPTION AS D " +
				"  INNER JOIN ( " +
				"    SELECT ID.DESCRIPTION_ID, MAX(ID.EFFECTIVE_TIME) AS MAX_ETIME " +
				"    FROM DESCRIPTION AS ID " +
				"    INNER JOIN ( ";
		qry += closureQry;
		qry +=	"    ) AS DD " +
				"    ON ID.CONCEPT_ID = DD.CONCEPT_ID " +
				"    WHERE ID.EFFECTIVE_TIME <= '" + effectiveTime + "' " +
				"    AND ID.TYPE_ID = '" + SNOMEDCTUtils.DescriptionType.FullySpecifiedName + "' " +
				"    GROUP BY ID.DESCRIPTION_ID " +
				"  ) AS GD " +
				"  ON D.DESCRIPTION_ID = GD.DESCRIPTION_ID " +
				"  AND D.EFFECTIVE_TIME = GD.MAX_ETIME " +
				"  AND D.ACTIVE = 1 " +
				"  AND D.LANGUAGE_CODE = '" + LANG + "' " +
				"  GROUP BY D.CONCEPT_ID, D.TERM " +
				") AS D " +
				"ON C.CONCEPT_ID = D.CONCEPT_ID " +
				"LEFT JOIN ( ";
		qry += closureQry;
		qry +=  ") AS T " +
				"ON C.CONCEPT_ID = T.CONCEPT_ID ";
		qry += getModuleJoinQuery(effectiveTime);
		qry += getDefinitionStatusJoinQuery(effectiveTime);
		return qry;
	}


	/**
	 *
	 * @param conceptId
	 * @param ids
	 * @param effectiveTime
	 * @param offset
	 * @param limit
	 * @param inclSelf
	 * @return
	 */
	private String getAncestorListQuery(String conceptId, List<String> ids, String effectiveTime, int offset, int limit, boolean inclSelf) {
		if (CollectionUtils.isEmpty(ids)) {
			return "";
		}
		
		String qry;
		String orInClause = "";
		int idsSize = ids.size();
		
		orInClause = "CONCEPT_ID IN ( '" + ids.get(0) + "'";
		
		for (int i = 1; i < idsSize; i++) {
			orInClause += ", '" + ids.get(i) + "'";
		}
		orInClause += ") ";
		
		if (inclSelf) {
			orInClause += "OR CONCEPT_ID = '" + conceptId + "' ";
		}
		
		qry = "SELECT C.*, D.TERM, MODULE.TERM AS MODULE_NAME, DEF.TERM AS DEF_NAME, TC.CHILDREN_COUNT, TC.DESCENDANT_COUNT " +
			  "FROM ( " +
			  "  SELECT C1.* " +
			  "  FROM CONCEPT AS C1 " +
			  "  INNER JOIN ( " +
			  "    SELECT CONCEPT_ID, MAX(EFFECTIVE_TIME) AS MAX_ETIME " +
			  "    FROM CONCEPT " +
			  "    WHERE ( ";
		
		qry += orInClause;
				
		qry +=  "    ) " +
				"    AND EFFECTIVE_TIME <= '" + effectiveTime + "' " +
				"    GROUP BY CONCEPT_ID " +
				"  ) AS C2 " +
				"  ON C1.CONCEPT_ID = C2.CONCEPT_ID " +
				"  AND C1.EFFECTIVE_TIME = C2.MAX_ETIME " +
				"  AND C1.ACTIVE = 1 " +
				") AS C " +
				"INNER JOIN ( " +
				"  SELECT CONCEPT_ID, TERM " +
				"  FROM DESCRIPTION AS D " +
				"  INNER JOIN ( " +
				"	SELECT DESCRIPTION_ID, MAX(EFFECTIVE_TIME) AS MAX_ETIME " +
				"    FROM DESCRIPTION " +
				"    WHERE ( ";
		
		qry += orInClause;
		
		qry +=  "    ) " +
				"    AND EFFECTIVE_TIME <= '" + effectiveTime + "' " +
				"    AND TYPE_ID = '" + SNOMEDCTUtils.DescriptionType.FullySpecifiedName + "' " +
				"    GROUP BY DESCRIPTION_ID " +
				"  ) AS GD " +
				"  ON D.DESCRIPTION_ID = GD.DESCRIPTION_ID " +
				"  AND D.EFFECTIVE_TIME = GD.MAX_ETIME " +
				"  AND D.ACTIVE = 1 " +
				"  AND D.LANGUAGE_CODE = '" + LANG + "' " +
				"  GROUP BY D.CONCEPT_ID, D.TERM" + // PostgreSQL: all SELECT columns must appear in GROUP BY
				") AS D " +
				"ON C.CONCEPT_ID = D.CONCEPT_ID " +
				"LEFT JOIN ( " +
				"  SELECT DISTINCT(CONCEPT_ID), CHILDREN_COUNT, DESCENDANT_COUNT " +
				"  FROM TC " +
				"    WHERE ( ";
		qry += orInClause;
		qry +=  "    ) " +
				") AS TC " +
				"ON C.CONCEPT_ID = TC.CONCEPT_ID ";
				
		qry += getModuleJoinQuery(effectiveTime);
		qry += getDefinitionStatusJoinQuery(effectiveTime);
		
		// Self를 포함할 경우 Self를 가장 상위로 보여지게 정렬
		if (inclSelf) {
			qry += "ORDER BY FIELD(C.CONCEPT_ID, '" + conceptId + "') DESC, D.TERM ";
		}
		
		qry += "LIMIT " + limit + " " +
			   "OFFSET " + offset;
		
		return qry;
	}
	
	
	/**
	 * 모듈 조인 쿼리
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	// SNOMED CT에서 사용되는 MODULE_ID 목록 (고정값 — 전체 테이블 스캔 회피)
	private static final String MODULE_IDS =
		"'900000000000207008','900000000000012004','715152001','715515008'," +
		"'718292005','721230008','784009001','816211006','827022005'," +
		"'1157359004','1237620007','1303956008'";

	private String getModuleJoinQuery(String effectiveTime) {
		String query;

		query = "LEFT JOIN ( " +
				"  SELECT CONCEPT_ID, TERM " +
				"  FROM DESCRIPTION AS D " +
				"  INNER JOIN ( " +
				"    SELECT DESCRIPTION_ID, MAX(EFFECTIVE_TIME) AS MAX_ETIME " +
				"    FROM DESCRIPTION AS ID " +
				"    WHERE ID.CONCEPT_ID IN (" + MODULE_IDS + ") " +
				"    AND ID.EFFECTIVE_TIME <= '" + effectiveTime + "' " +
				"    AND ID.TYPE_ID = '" + SNOMEDCTUtils.DescriptionType.FullySpecifiedName + "' " +
				"    GROUP BY ID.DESCRIPTION_ID " +
				"  ) AS GD " +
				"  ON D.DESCRIPTION_ID = GD.DESCRIPTION_ID " +
				"  AND D.EFFECTIVE_TIME = GD.MAX_ETIME " +
				"  AND D.ACTIVE = 1 " +
				"  AND D.LANGUAGE_CODE = '" + LANG + "' " +
				") AS MODULE " +
				"ON C.MODULE_ID = MODULE.CONCEPT_ID ";

		return query;
	}
	
	
	/**
	 * 정의상태 조인 쿼리
	 * 
	 * @param conceptId
	 * @param effectiveTime
	 * @return
	 */
	// SNOMED CT 정의상태 ID (고정값 2개 — 전체 테이블 스캔 회피)
	private static final String DEFINITION_STATUS_IDS =
		"'900000000000073002','900000000000074008'";

	private String getDefinitionStatusJoinQuery(String effectiveTime) {
		String query;

		query = "LEFT JOIN ( " +
				"  SELECT CONCEPT_ID, TERM " +
				"  FROM DESCRIPTION AS D " +
				"  INNER JOIN ( " +
				"    SELECT ID.DESCRIPTION_ID, MAX(EFFECTIVE_TIME) AS MAX_ETIME " +
				"    FROM DESCRIPTION AS ID " +
				"    WHERE ID.CONCEPT_ID IN (" + DEFINITION_STATUS_IDS + ") " +
				"    AND ID.EFFECTIVE_TIME <= '" + effectiveTime + "' " +
				"    AND ID.TYPE_ID = '" + SNOMEDCTUtils.DescriptionType.FullySpecifiedName + "' " +
				"    GROUP BY ID.DESCRIPTION_ID " +
				"  ) AS GD " +
				"  ON D.DESCRIPTION_ID = GD.DESCRIPTION_ID " +
				"  AND D.EFFECTIVE_TIME = GD.MAX_ETIME " +
				"  AND D.ACTIVE = 1 " +
				"  AND D.LANGUAGE_CODE = '" + LANG + "' " +
				") AS DEF " +
				"ON C.DEFINITION_STATUS_ID = DEF.CONCEPT_ID ";

		return query;
	}

}
