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
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ICD10 Class
 * 
 * @author Yu
 *
 */
@Entity
@Table(schema = "icd10")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Icd10Class {
  
  @Id
  @Column//(length = 10)
  private String code;
  
  @Column//(length = 4)
  private String version;
  
  @Column//(length = 10)
  private String classKind;
  
  @Column//(length = 10)
  private String usageKind;
  
  @Column//(length = 10)
  private String superClass;
  
  @Column
  private String label;

  @Column
  private String ref;

  @Column//(length = 4)
  private int childrenCount;

  @Column(length = 4)
  private int descendantCount;
  
  @Column//(length = 255)
  private String path;
  
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "icd10Class")
  private List<Icd10Rubric> icd10Rubric;
  
  //@OneToOne(fetch = FetchType.LAZY, mappedBy = "icd10Class")
  //private List<Icd10Meta> icd10Meta;
}
