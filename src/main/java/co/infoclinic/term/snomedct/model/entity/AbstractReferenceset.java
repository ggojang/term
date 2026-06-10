package co.infoclinic.term.snomedct.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonInclude(Include.NON_NULL)
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractReferenceset extends Component {
	
	@PrePersist
	protected void onCreate() {
		super.onCreate();
		// Set Uuid
		if (this.referencesetId == null) {
		  UUID uuid = UUID.randomUUID();
	        this.referencesetId = uuid.toString();
		}
	}
	
	@NotNull
	@Column(nullable = false)
	private String referencesetId;
	
	@NotNull
	@Column(nullable = false)
	private String refsetId;
	
	@NotNull
	@Column(nullable = false)
	private String referencedComponentId;
	
	@Column
	private String field1;
	
	@Column
	private String field2;
	
	@Column
	private String field3;
	
	@Column
	private String field4;
	
	@Column
	private String field5;
	
	@Column
	private String field6;
	
	@Column
	private String field7;

	@Override
	protected void onLoad() {
		super.setComponentId(getReferencesetId());
	}
	
}
