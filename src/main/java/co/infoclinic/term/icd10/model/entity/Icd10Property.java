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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Icd10Property {

  @Id
  @GeneratedValue
  private int seq;
  
  @NotNull
  @Column(nullable = false)
  private String id;
  
  @NotNull
  @Column(nullable = false)
  private String entity;
  
  @NotNull
  @Column(nullable = false)
  private String property;
  
  @NotNull
  @Column(nullable = false)
  private String name;
  
  @Column(nullable = true)
  private String language;
  
  @NotNull
  @Column(nullable = false)
  private String type;
  
  @NotNull
  @Column(nullable = false)
  private String value;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumns({
    @JoinColumn(name = "id", referencedColumnName="id", insertable = false, updatable = false),
    @JoinColumn(name = "entity", referencedColumnName="entity", insertable = false, updatable = false)
  })
  private Icd10Entity icd10Entity;
}
