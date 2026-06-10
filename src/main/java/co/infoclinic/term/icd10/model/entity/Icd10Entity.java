package co.infoclinic.term.icd10.model.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ICD10 Entity
 * 
 * @author dongwon
 *
 */
@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Icd10Entity {
  
  @EmbeddedId
  private Icd10EntityId id;
  
  @Column(nullable = true)
  private String parent;
  
  @NotNull
  @Column(nullable = false)
  private String type;
  
  @Column(nullable = true)
  private String name;
  
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "icd10Entity")
  private List<Icd10Property> icd10Properties;
  
  @OneToOne(fetch = FetchType.LAZY, mappedBy = "icd10Entity")
  private Icd10Attribute icd10Attribute;
}
