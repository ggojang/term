package co.infoclinic.term.snomedct.model.entity;

import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Inferred Relationship Entity
 */
@Entity
@Table
@Data
@EqualsAndHashCode(callSuper=true)
@SqlResultSetMapping(
	name="InferredRelationshipWithName",
	entities={
		@EntityResult(
			entityClass=InferredRelationship.class
		)	
	},
	columns={
		@ColumnResult(name="CHARACTERISTIC_TYPE_NAME"),
		@ColumnResult(name="MODULE_NAME"),
		@ColumnResult(name="MODIFIER_NAME")
	}
)
public class InferredRelationship extends Relationship {

}
