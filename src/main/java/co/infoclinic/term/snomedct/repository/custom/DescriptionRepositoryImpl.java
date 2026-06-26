package co.infoclinic.term.snomedct.repository.custom;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaContext;

import co.infoclinic.term.snomedct.model.entity.Description;

/**
 * Description Custom Repository
 */
public class DescriptionRepositoryImpl implements DescriptionRepositoryCustom {

	/** Entity Manager */
	private final EntityManager em;
	
	@Autowired
	public DescriptionRepositoryImpl(JpaContext context) {
		this.em = context.getEntityManagerByManagedType(Description.class);
	}
	
	
	
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------

	/// ----------------------------------------
	/// 조회
	/// ----------------------------------------
	
	
	/**
	 * 경로를 부모로 갖는 Concept의 fsn 목록을 반환
	 * <pre>
	 * SELECT D.*
	 * FROM (
	 *   SELECT DSC.CONCEPT_ID, DSC.DESCRIPTION_ID, MAX(DSC.EFFECTIVE_TIME) AS MAX_EFFECTIVE_TIME
	 *   FROM (
	 *     SELECT CONCEPT_ID
	 *     FROM term.TC 
	 *     WHERE 
	 *     PATH LIKE '138875005~105590001~115669006~33463005~32457005~87612001%'
	 *     OR PATH LIKE '138875005~105590001~115668003~256906008~87612001%'
	 *     OR PATH LIKE '138875005~105590001~91720002~32457005~87612001%'
	 *     GROUP BY CONCEPT_ID
	 *   ) AS CNPT
	 *   LEFT JOIN DESCRIPTION_TP AS DSC
	 *   ON DSC.CONCEPT_ID = CNPT.CONCEPT_ID
	 *   AND DSC.TYPE_ID = '900000000000003001' -- FSN
	 *   GROUP BY CONCEPT_ID, DESCRIPTION_ID
	 * ) AS MBR
	 * LEFT JOIN DESCRIPTION_TP AS D
	 * ON MBR.CONCEPT_ID = D.CONCEPT_ID
	 * AND MBR.MAX_EFFECTIVE_TIME = D.EFFECTIVE_TIME
	 * AND MBR.DESCRIPTION_ID = D.DESCRIPTION_ID
	 * AND D.ACTIVE = 1;
	 * </pre>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Description> findByPaths(List<String> paths) {
		// result
		List<Description> rslts = new ArrayList<Description>();

		// query object
		Query q;
		// query string
		String query;
		// path list size
		int pathsSize = paths.size();
		
		// if path list size is 0 then return empty list
		if (pathsSize == 0) {
			return rslts;
		}
		
		query = " SELECT D.* " +
				" FROM ( " +
				" SELECT DSC.CONCEPT_ID, DSC.DESCRIPTION_ID, MAX(DSC.EFFECTIVE_TIME) AS MAX_EFFECTIVE_TIME " +
				" FROM ( " +
				" SELECT CONCEPT_ID " +
				" FROM term.TC " +
				" WHERE " + getPathLikeStmt(paths.get(0));
		
		for (int i = 1; i < pathsSize; i++) {
			query += " OR " + getPathLikeStmt(paths.get(i));
		}
		
		query += " GROUP BY CONCEPT_ID " +
				 " ) AS CNPT " +
				 " LEFT JOIN DESCRIPTION_TP AS DSC " +
				 " ON DSC.CONCEPT_ID = CNPT.CONCEPT_ID " +
				 " AND DSC.TYPE_ID = '900000000000003001' -- FSN " +
				 " GROUP BY CONCEPT_ID, DESCRIPTION_ID " +
				 " ) AS MBR " +
				 " LEFT JOIN DESCRIPTION_TP AS D " +
				 " ON MBR.CONCEPT_ID = D.CONCEPT_ID " +
				 " AND MBR.MAX_EFFECTIVE_TIME = D.EFFECTIVE_TIME " +
				 " AND MBR.DESCRIPTION_ID = D.DESCRIPTION_ID " +
				 " AND D.ACTIVE = 1;";	
	
		// create native query
		q = em.createNativeQuery(query);
		// get result list
		rslts = q.getResultList();
		
		return rslts;
	}
	
	
	
	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------

	/**
	 * Path Column에 LIKE clause를 사용하는 문장 반환 
	 * @param path
	 * @return
	 */
	private String getPathLikeStmt(String path) {
		return "PATH LIKE '" + path + "%' ";
	}
}
