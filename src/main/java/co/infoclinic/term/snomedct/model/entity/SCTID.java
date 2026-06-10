package co.infoclinic.term.snomedct.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SCTID {

	@Id
	@GeneratedValue
	@Column(name = "SEQ")
	private Long id;
	
	@Column(name = "CPNT_TYPE")
	private int componentType;
}
