package co.infoclinic.term.snomedct.model.entity;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table
@Data
@EqualsAndHashCode(callSuper=true)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="TYPE")
@DiscriminatorValue("null") // DiscriminatorColumn으로 지정한 컬럼의 값이 null인 경우. 보통 base class에 사용한다.
public class UserReferenceset extends AbstractReferenceset {
  
}
