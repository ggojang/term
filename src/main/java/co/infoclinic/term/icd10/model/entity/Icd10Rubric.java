package co.infoclinic.term.icd10.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

//import org.hibernate.annotations.Proxy;

//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "icd10")
//@JsonIgnoreProperties(ignoreUnknown = false)
//@Proxy(lazy = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Icd10Rubric {

  @Id
  @GeneratedValue
  private int seq;
  
  @NotNull
  @Column
  private String code;
  
  @NotNull
  @Column
  private String version;
  
  @NotNull
  @Column
  private String id;
  
  @NotNull
  @Column
  private String kind;
  
  @Column
  private String modifierCode;

  @Column
  private String usageKind;
  
  @Column
  private String lang;
  
  @Column
  private String fragmentType;

  @Column
  private String paraType;

  @Column
  private String label;

  @Column
  private String ref;
  
  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumns({
    @JoinColumn(name = "code", referencedColumnName="code", insertable = false, updatable = false) //,
//    @JoinColumn(name = "version", referencedColumnName="version", insertable = false, updatable = false)
//  })
  private Icd10Class icd10Class;
  
}
