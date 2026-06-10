package co.infoclinic.term.loinc.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "loinc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceOrganization {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "SEQ")
	private Long id;
	
	@Column(length = 255)
	private String copyrightId;
	
	@Column(length = 255)
	private String name;
	
	@Column(columnDefinition = "text")
	private String copyright;
	
	@Column(columnDefinition = "text")
	private String termsOfUse;
	
	@Column(length = 255)
	private String url;
}
