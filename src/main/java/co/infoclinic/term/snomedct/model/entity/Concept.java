package co.infoclinic.term.snomedct.model.entity;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table
@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
@SqlResultSetMapping(
	name="ConceptWithHierarchy",
	entities={
		@EntityResult(
			entityClass=Concept.class
			/*,
			fields={
				@FieldResult(name="id", column="SEQ"),
				@FieldResult(name="active", column="ACTIVE"),
				@FieldResult(name="effectiveTime", column="EFFECTIVE_TIME"),
				@FieldResult(name="moduleId", column="MODULE_ID"),
				@FieldResult(name="definitionStatusId", column="DEFINITION_STATUS_ID"),
				@FieldResult(name="conceptId", column="CONCEPT_ID"),
			}*/
		)	
	},
	columns={
		@ColumnResult(name="TERM"),
		@ColumnResult(name="MODULE_NAME"),
		@ColumnResult(name="DEF_NAME"),
		@ColumnResult(name="CHILDREN_COUNT"),
		@ColumnResult(name="DESCENDANT_COUNT")
	}
)
public class Concept extends Component {

	/** The definition status id */
	  @NotNull
	  @Column(nullable = false)
	  private String definitionStatusId;
	  
	  /** The concept id */
	  @NotNull
	  @Column(nullable = false)
	  private String conceptId;

	  @Transient
	  private int childrenCount;
	  
	  @Transient
	  private int descendantCount;
	  
	  @Override
	  protected void onLoad() {
	      super.setComponentId(getConceptId());
	  }
}