package co.infoclinic.term.loinc.repository.custom;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaContext;

import co.infoclinic.term.loinc.model.entity.Loinc;
import co.infoclinic.term.loinc.utils.PartEnum;

/**
 * Entity의 Complex Query를 처리하는 Repository
 */
public class LoincRepositoryImpl implements LoincRepositoryCustom {
	
	private static final String FROM_CLAUSE = "FROM loinc.LOINC";

	/** JPA Entity Manager */
	private final EntityManager em;
	
	/** Constructor DI */
	@Autowired
	public LoincRepositoryImpl(JpaContext context) {
		this.em = context.getEntityManagerByManagedType(Loinc.class);
	}
	
	
	
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.repository.custom.EntityRepositoryCustom#findListByPartAndValue(java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Loinc> findListByPartAndValue(PartEnum part, String value, int offset, int limit) {
		
		// 엔티티 목록
		List<Loinc> entities;
		// 쿼리 객체
		Query q;
		// 파트 컬럼 이름
		String col = part.getColName();
		// 쿼리 문
		String qry = "SELECT * " + 
					 FROM_CLAUSE + " " +
					 "WHERE " + col + " = '" + value + "' " +
					 //"ORDER BY " + col + " " +
					 "LIMIT " + limit + " " +
					 "OFFSET " + offset + " ";
		
		// Native Query 생성
		q = em.createNativeQuery(qry, Loinc.class);
		
		// 쿼리 수행 후 결과 반환
		entities = q.getResultList();
		
		// 반환
		return entities;
	}


	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.loinc.repository.custom.LoincRepositoryCustom#findCountByPartAndValue(co.infoclinic.term.loinc.utils.PartEnum, java.lang.String)
	 */
	@Override
	public int findCountByPartAndValue(PartEnum part, String value) {
		int count = 0;
		
		// 쿼리 객체
		Query q;
		// 파트 컬럼 이름
		String col = part.getColName();
		// 쿼리 문
		String qry = "SELECT COUNT(*) " + 
					 FROM_CLAUSE + " " +
					 "WHERE " + col + " = '" + value + "' ";
		
		// Native Query 생성
		q = em.createNativeQuery(qry);
		
		// 쿼리 수행 후 결과 반환
		Object result = (Object) q.getSingleResult();
		count = Integer.valueOf(String.valueOf(result));
		
		return count;
	}
	
}
