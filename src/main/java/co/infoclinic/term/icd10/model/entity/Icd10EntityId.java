package co.infoclinic.term.icd10.model.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Icd10EntityId implements Serializable {
  
  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  private String id;
  
  @Column(nullable = false)
  private String entity;
}
