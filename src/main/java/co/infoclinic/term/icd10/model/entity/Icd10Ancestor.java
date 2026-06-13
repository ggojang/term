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
@Table(schema = "icd10")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Icd10Ancestor {

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

	  @Column
	  private String koreanLabel;

	  @Column
	  private Boolean isKcdExt;

}