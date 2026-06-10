package co.infoclinic.term.loinc.repository.custom;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaContext;

import co.infoclinic.term.loinc.model.entity.LPMap;

/**
 * Entity의 Complex Query를 처리하는 Repository
 */
public class LPMapRepositoryImpl implements LPMapRepositoryCustom {
	
	private static final String FROM_CLAUSE = "FROM loinc.LP_MAP";

	/** JPA Entity Manager */
	private final EntityManager em;
	
	/** Constructor DI */
	@Autowired
	public LPMapRepositoryImpl(JpaContext context) {
		this.em = context.getEntityManagerByManagedType(LPMap.class);
	}
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.repository.custom.EntityRepositoryCustom#findListByLPMap(java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<LPMap> findListByCode(String code, int offset, int limit) {
		
		// 엔티티 목록
		List<LPMap> entities;
		// 쿼리 객체
		Query q;
		// 쿼리 문
		String qry = "SELECT * " + 
					 FROM_CLAUSE + " " +
					 "WHERE " + "PART_NUMBER" + " = '" + code + "' " +
					 //"ORDER BY " + col + " " +
					 "LIMIT " + limit + " " +
					 "OFFSET " + offset + " ";
		
		// Native Query 생성
		q = em.createNativeQuery(qry, LPMap.class);
		
		// 쿼리 수행 후 결과 반환
		entities = q.getResultList();
		
		// 반환
		return entities;
	}


	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.repository.custom.LPMapRepositoryCustom#findCountByLPMap(java.lang.String)
	 */
	@Override
	public int findCountByValue(String value) {
		int count = 0;
		
		// 쿼리 객체
		Query q;
		// 쿼리 문
		String qry = "SELECT COUNT(*) " + 
					 FROM_CLAUSE + " " +
					 "WHERE " + "PART_NUMBER" + " = '" + value + "' ";
		
		// Native Query 생성
		q = em.createNativeQuery(qry);
		
		// 쿼리 수행 후 결과 반환
		Object result = (Object) q.getSingleResult();
		count = Integer.valueOf(String.valueOf(result));
		
		return count;
	}
	
}
