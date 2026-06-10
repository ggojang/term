package co.infoclinic.term.snomedct.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Scheme
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scheme {

	/** ID: {name}-{version} */
	@Id
	@Column
	private String id;
	
	/** Name: SNOMEDCT-{Abbr of ReleaseEdition} */
	@Column
	private String name;
	
	/** Edition: SNOMEDCT Edition Name */
	@Column
	private String edition;
	
	/** Version: SNOMEDCT v + EffectiveTime */
	@Column
	private String version;
	
	/** Authority: SNOMED */
	@Column
	private String authority;
	
	/** Date: Updated Date */
	@Column
	private String date;
}
