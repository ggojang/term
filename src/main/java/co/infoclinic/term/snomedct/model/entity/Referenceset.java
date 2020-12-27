package co.infoclinic.term.snomedct.model.entity;

import javax.persistence.Entity;
import javax.persistence.Table;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table
@Data
@EqualsAndHashCode(callSuper=true)
public class Referenceset extends AbstractReferenceset {
}
