package co.infoclinic.term.fhir.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "package", schema = "fhir")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FhirPackage {
    @Id
    @Column(length = 200)
    private String id;           // {name}#{version}

    @Column(length = 200, nullable = false)
    private String name;

    @Column(length = 50)
    private String version;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "installed_at")
    private LocalDateTime installedAt;
}
