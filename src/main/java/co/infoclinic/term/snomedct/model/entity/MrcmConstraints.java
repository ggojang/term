package co.infoclinic.term.snomedct.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MrcmConstraints {

	@Id
	@GeneratedValue
	@Column(name = "SEQ")
	private Long id;
	
	@NotNull
	@Column(nullable = false)
	private String attributeId;
	
	@NotNull
	@Column(nullable = false)
	private String attributeName;
	
	@NotNull
	@Column(nullable = false)
	private String sourceId;
	
	@NotNull
	@Column(nullable = false)
	private String sourceName;
	
	@NotNull
	@Column(nullable = false)
	private String valueId;
	
	@NotNull
	@Column(nullable = false)
	private String valueName;
}
