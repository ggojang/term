package co.infoclinic.term.common.strategy;

import java.util.Locale;

import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.internal.util.StringHelper;

/**
 * 클래스, 속성명을 규칙을 사용하여 DB 엔티티의 요소를 변경한다.
 * 
 * @author 최동원 dongwon.choi@infoclinic.co
 * @since 2016.05.09. 오후 12:12
 *
 */
public class UpperCasedNamingStrategy extends ImprovedNamingStrategy {

  private static final long serialVersionUID = 1L;

  /**
   * 클래스 명을 underscore(_)와 대문자로 구분된 문자열로 변환한다. (InferredRelationship => INFERRED_RELATIONSHIP)
   * 
   * @param className 클래스 명
   * @return 테이블 명
   */
  @Override
  public String classToTableName(String className) {
    return upper(addUnderscores(StringHelper.unqualify(className)));
  }

  /**
   * 속성 명을 underscore(_)와 대문자로 구분된 문자열로 변환한다. (effectiveTime => EFFECTIVE_TIME)
   * 
   * @param propertyName 속성 명
   * @return 속성 명
   */
  @Override
  public String propertyToColumnName(String propertyName) {
    //return upper(super.propertyToColumnName(propertyName));
    return upper(addUnderscores(StringHelper.unqualify(propertyName)));
  }

  /**
   * 테이블 명을 underscore(_)와 대문자로 구분된 문자열로 변환한다. (InferredRelationship => INFERRED_RELATIONSHIP)
   * 
   * @param tableName 테이블 명
   * @return 테이블 명
   */
  @Override
  public String tableName(String tableName) {
    return upper(addUnderscores(StringHelper.unqualify(tableName)));
  }

  /**
   * 컬럼 명을 underscore(_)와 대문자로 구분된 문자열로 변환한다. (effectiveTime => EFFECTIVE_TIME)
   * 
   * @param columnName 컬럼 명
   * @return 컬럼 명
   */
  @Override
  public String columnName(String columnName) {
    //return upper(super.columnName(columnName));
	return upper(addUnderscores(columnName));  
  }

  protected static String addUnderscores(String name) {
    StringBuilder buf = new StringBuilder(name.replace('.', '_'));
    for (int i = 1; i < buf.length() - 1; i++) {
      if ((Character.isLowerCase(buf.charAt(i - 1)) || Character.isDigit(buf.charAt(i - 1)))
          && Character.isUpperCase(buf.charAt(i)) && Character.isLowerCase(buf.charAt(i + 1))) {
        buf.insert(i++, '_');
      }
    }
    return buf.toString().toLowerCase(Locale.ROOT);
  }

  private String upper(String value) {
    return value == null ? null : value.toUpperCase();
  }
}
