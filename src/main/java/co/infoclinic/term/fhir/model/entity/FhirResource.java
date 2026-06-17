package co.infoclinic.term.fhir.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(schema = "fhir", name = "resource")
@IdClass(FhirResourceId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FhirResource {

    @Id
    @Column(name = "resource_type", length = 64)
    private String resourceType;

    @Id
    @Column(name = "id", length = 128)
    private String id;

    @Column(name = "url", length = 512)
    private String url;

    @Column(name = "version", length = 64)
    private String version;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "title", length = 512)
    private String title;

    @Column(name = "status", length = 32)
    private String status;

    @Type(type = "org.hibernate.type.StringType")
    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
