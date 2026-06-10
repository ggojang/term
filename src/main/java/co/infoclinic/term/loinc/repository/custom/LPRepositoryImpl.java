package co.infoclinic.term.loinc.repository.custom;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaContext;

import co.infoclinic.term.loinc.model.entity.LP;

/**
 * Entity의 Complex Query를 처리하는 Repository
 */
public class LPRepositoryImpl implements LPRepositoryCustom {
	
	private static final String FROM_CLAUSE = "FROM loinc.LP";

	/** JPA Entity Manager */
	private final EntityManager em;
	
	/** Constructor DI */
	@Autowired
	public LPRepositoryImpl(JpaContext context) {
		this.em = context.getEntityManagerByManagedType(LP.class);
	}
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.repository.custom.EntityRepositoryCustom#findListByLP(java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<LP> findListByCode(String code, int offset, int limit) {
		
		// 엔티티 목록
		List<LP> entities;
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
		q = em.createNativeQuery(qry, LP.class);
		
		// 쿼리 수행 후 결과 반환
		entities = q.getResultList();
		
		// 반환
		return entities;
	}


	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.repository.custom.LPRepositoryCustom#findCountByLP(java.lang.String)
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
