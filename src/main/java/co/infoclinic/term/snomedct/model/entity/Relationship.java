package co.infoclinic.term.snomedct.model.entity;

import javax.persistence.Column;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class Relationship extends Component {

	
	/** The relationship id. */
	@NotNull
	@Column(nullable = false)
	private String relationshipId;
	
	/** The source id. */
	@NotNull
	@Column(nullable = false)
	private String sourceId;
	
	/** The destination id. */
	@NotNull
	@Column(nullable = false)
	private String destinationId;
	
	/** The relationship group. */
	@NotNull
	@Column(nullable = false)
	private String relationshipGroup;
	
	/** The type id. */
	@NotNull
	@Column(nullable = false)
	private String typeId;
	
	/** The characteristic type id. */
	@NotNull
	@Column(nullable = false)
	private String characteristicTypeId;
	
	/** The modifier id. */
	@NotNull
	@Column(nullable = false)
	private String modifierId;
	
	@Override
	protected void onLoad() {
		super.setComponentId(getRelationshipId());
	}
}
