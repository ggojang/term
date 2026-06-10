package co.infoclinic.term.snomedct.model.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class Component {

	@PrePersist
	  protected void onCreate() {
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	    String effectiveTime = sdf.format(new Date());
	    this.effectiveTime = effectiveTime;
	  }

	  @PostLoad
	  protected abstract void onLoad();

	  @Id
	  @GeneratedValue
	  @Column(name = "SEQ")
	  private Long id;

	  @Transient
	  private String componentId;


	  /** The effective time. */
	  @NotNull
	  @Column(
	      nullable = false,
	      columnDefinition="CHAR(8)"
	  )
	  private String effectiveTime;

	  /** The active. */
	  @NotNull
	  @Column(
	      nullable = false, 
	      columnDefinition = "TINYINT",
	      length = 1
	  )
	  private boolean active;

	  /** The module id. */
	  @NotNull
	  @Column(nullable = false)
	  private String moduleId;
}
