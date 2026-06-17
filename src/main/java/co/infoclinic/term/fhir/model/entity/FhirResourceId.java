package co.infoclinic.term.fhir.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FhirResourceId implements Serializable {
    private String resourceType;
    private String id;
}
