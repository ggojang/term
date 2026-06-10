package co.infoclinic.term.loinc.repository.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaContext;

import co.infoclinic.term.common.utils.LOINCUtils;
import co.infoclinic.term.loinc.model.entity.Hierarchy;

/**
 * Hierarchy의 Complex Query를 처리하는 Repository
 */
public class HierarchyRepositoryImpl implements HierarchyRepositoryCustom {

	/** JPA Entity Manager */
	private final EntityManager em;
	
	/** Constructor DI */
	@Autowired
	public HierarchyRepositoryImpl(JpaContext context) {
		this.em = context.getEntityManagerByManagedType(Hierarchy.class);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.repository.custom.HierarchyRepositoryCustom#findAncestorListByCode(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Hierarchy> findAncestorListByCodeAndInclSelf(String code, boolean inclSelf) {
		if (StringUtils.isEmpty(code) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return new ArrayList<Hierarchy>();
		}
		
		// 엔티티 목록
		List<Hierarchy> entities= null;
		// 검색할 모든 경로 목록
		Map<String, Set<String>> pathCodesMap = new HashMap<String, Set<String>>();
		// 쿼리 객체
		Query q;
		// 쿼리 문
		String qry = null;
		
		
		// paths 분할, 중복 제거
		List<Hierarchy> hierarchies = findByCode(code);
		if (!CollectionUtils.isEmpty(hierarchies)) {
			for (Hierarchy hierarchy : hierarchies) {
				String path = hierarchy.getPath();
				splitPathCodes(path, pathCodesMap);
			}
			
			// Ancestor Query
			String ancestorQry = "";
			if (!MapUtils.isEmpty(pathCodesMap) ) {
				Set<String> codes;
				Iterator<String> itr = pathCodesMap.keySet().iterator();
				while (itr.hasNext()) {
					String path = itr.next();
					
					codes = pathCodesMap.get(path);
					if (!CollectionUtils.isEmpty(codes)) {
						for (String cd : codes) {
							// (CODE = 'LP14738-6' AND PATH = 'PARTS~LP32744-2')
							ancestorQry += getCodePathConditionWithBrace(cd, path);
						}
						
						if (itr.hasNext()) {
							ancestorQry += " OR ";
						}
					}
				}
			}
				
			// Self Query
			String selfQry = "";
			if (inclSelf) {
				if (!StringUtils.isEmpty(ancestorQry)) {
					selfQry += " OR ";
				}
				selfQry += " CODE = '" + code + "' ";
			}
			
			// Assemble Query
			if (!StringUtils.isEmpty(ancestorQry) || !StringUtils.isEmpty(selfQry)) {
				// Query
				//	e.g.
				//  SELECT *
				//	FROM loinc.HIERARCHY
				//	WHERE (CODE = 'LP14738-6' AND PATH = 'PARTS~LP32744-2') OR (CODE = 'LP32744-2' AND PATH = 'PARTS')
				
				qry = "SELECT * " +
					  "FROM loinc.HIERARCHY " +
					  "WHERE ";
				
				qry += ancestorQry + selfQry;
			}
			
		}
		
		if (!StringUtils.isEmpty(qry)) {
			q = em.createNativeQuery(qry, Hierarchy.class);
			entities = q.getResultList();
		}
		
		return entities != null ? entities:new ArrayList<Hierarchy>();
	}
	
	
	// ----------------------------------------
	// Private Methods
	// ----------------------------------------

	/**
	 * 특정 코드를 가지는 엔티티 검색
	 * 
	 * @param code
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Hierarchy> findByCode(String code) {
		// Path List
		
		List<Hierarchy> paths = new ArrayList<Hierarchy>();
		//List<Hierarchy> paths;
		
		// Query Object
		Query q;
		// Query String
		String qry;
		
		qry = "SELECT * " +
			  "FROM loinc.HIERARCHY " +
			  //"WHERE CODE = :cd";
			  "WHERE CODE = '" + code + "' ";
		
		q = em.createNativeQuery(qry, Hierarchy.class);
		//q.setParameter("cd", code);
		
		paths = q.getResultList();
		
		return paths;
		//return paths != null ? paths : new ArrayList<Hierarchy>();
	}
	
	
	/**
	 * 코드와 경로의 쿼리 조건문 반환
	 * 
	 * @param code
	 * @param path
	 * @return
	 */
	private String getCodePathConditionWithBrace(String code, String path) {
		String condition = "";
		
		if ("".equals(path)) {
			condition = " ( CODE = '" + code + "') ";
		} else {
			condition = " ( CODE = '" + code + "' AND PATH = '" + path + "') ";
		}
		
		return condition;
	}
	
	
	/**
	 * 부모 경로와 코드를 분할
	 * 
	 * <pre>
	 *   만약 경로가 A-B-C-D일 경우,
	 *   1) A-B-C, D
	 *   2) A-B,   C
	 *   3) A,     B
	 *   
	 *  (PATH = 'PARTS~LP32744-2' AND CODE = 'LP14738-6') OR
	 *  (PATH = 'PARTS'           AND CODE = 'LP32744-2')
	 * </pre>
	 * 
	 * @param path
	 * @param pathCodes
	 * @return
	 */
	private Map<String, Set<String>> splitPathCodes(String path, Map<String, Set<String>> pathCodes) {
		if (StringUtils.isEmpty(path)) {
			return pathCodes;
		}
		
		Set<String> codes = null;
		
		// 경로에 구분자(~)가 없다면 path가 루트
		if (!path.contains("~")) {
			codes = new HashSet<String>();
			codes.add(path);
			pathCodes.put("", codes);
			
			return pathCodes;
		}
				
		// next path
		String parentPath = path.substring(0, path.lastIndexOf("~"));
		String code = path.substring(path.lastIndexOf("~") + 1);
		boolean isContain = pathCodes.containsKey(parentPath);
		if (isContain) {
			codes = pathCodes.get(parentPath);
		} else {
			codes = new HashSet<String>();
			pathCodes.put(parentPath, codes);
		}
		
		codes.add(code);
		
		return splitPathCodes(parentPath, pathCodes);
	}
}
