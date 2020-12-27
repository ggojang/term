package co.infoclinic.term.snomedct.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Description extends Component {

	/** The language code. */
	@NotNull
	@Column(nullable = false)
	private String languageCode;
	
	/** The type id. */
	@NotNull
	@Column(nullable = false)
	private String typeId;
	
	/** The description id. */
	@Column(nullable = false)
	private String descriptionId;
	
	/** The concept id. */
	@NotNull
	@Column(nullable = false)
	private String conceptId;
	
	/** The term. */
	@NotNull
	@Column(nullable = false)
	private String term;
	
	/** The case significance id status id */
	@NotNull
	@Column(nullable = false)
	private String caseSignificanceId;
	
	@Override
	protected void onLoad() {
	  super.setComponentId(getDescriptionId());
	}
}
