package co.infoclinic.term.fhir.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import co.infoclinic.term.fhir.model.entity.FhirResource;
import co.infoclinic.term.fhir.repository.FhirResourceRepository;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FhirResourceService {

    public static final FhirContext FHIR_CTX = FhirContext.forR4();

    @Autowired
    private FhirResourceRepository repo;

    public String save(String resourceType, String json) {
        IParser parser = FHIR_CTX.newJsonParser();
        Resource resource = (Resource) parser.parseResource(json);

        String id = resource.getIdElement().getIdPart();
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
            resource.setId(id);
        }

        String url, version, name, title, status, content;
        content = parser.encodeResourceToString(resource);

        if (resource instanceof org.hl7.fhir.r4.model.NamingSystem) {
            org.hl7.fhir.r4.model.NamingSystem ns = (org.hl7.fhir.r4.model.NamingSystem) resource;
            // NamingSystem의 URI uniqueId를 url로 사용
            url = ns.getUniqueId().stream()
                    .filter(u -> u.getType() == org.hl7.fhir.r4.model.NamingSystem.NamingSystemIdentifierType.URI)
                    .map(org.hl7.fhir.r4.model.NamingSystem.NamingSystemUniqueIdComponent::getValue)
                    .findFirst().orElse(null);
            version = null;
            name    = ns.getName();
            title   = ns.hasTitle() ? ns.getTitle() : null;
            status  = ns.getStatus() != null ? ns.getStatus().toCode() : null;
        } else {
            org.hl7.fhir.r4.model.MetadataResource meta = (org.hl7.fhir.r4.model.MetadataResource) resource;
            url     = meta.getUrl();
            version = meta.getVersion();
            name    = meta.getName();
            title   = meta.getTitle();
            status  = meta.getStatus() != null ? meta.getStatus().toCode() : null;
        }

        repo.upsert(resourceType, id, url, version, name, title, status, content);
        return id;
    }

    public Optional<String> findById(String resourceType, String id) {
        return repo.findContentByResourceTypeAndId(resourceType, id);
    }

    public Optional<String> findByUrl(String resourceType, String url) {
        return repo.findContentByResourceTypeAndUrl(resourceType, url);
    }

    /**
     * Search by name or url (returns full content). No filter = returns summary stubs only.
     */
    public List<String> searchContent(String resourceType, String name, String url) {
        if (url != null && !url.isEmpty()) {
            return repo.searchContentByResourceTypeAndUrl(resourceType, url);
        }
        if (name != null && !name.isEmpty()) {
            return repo.searchContentByResourceTypeAndName(resourceType, name);
        }
        // No filter: return lightweight summary objects instead of full content
        return Collections.emptyList();
    }

    /** Returns summary rows [id, url, name, title, status] for list-without-filter use case. */
    public List<Object[]> findSummary(String resourceType) {
        return repo.findSummaryByResourceType(resourceType);
    }

    public List<FhirResource> findAll(String resourceType) {
        return repo.findSummaryByResourceType(resourceType).stream()
                .map(row -> {
                    FhirResource r = new FhirResource();
                    r.setId(str(row[0]));
                    r.setUrl(str(row[1]));
                    r.setName(str(row[2]));
                    r.setTitle(str(row[3]));
                    r.setStatus(str(row[4]));
                    return r;
                })
                .collect(Collectors.toList());
    }

    public List<FhirResource> search(String resourceType, String name) {
        if (name != null && !name.isEmpty()) {
            return repo.searchContentByResourceTypeAndName(resourceType, name).stream()
                    .map(c -> { FhirResource r = new FhirResource(); r.setContent(c); return r; })
                    .collect(Collectors.toList());
        }
        return findAll(resourceType);
    }

    public void delete(String resourceType, String id) {
        repo.findByResourceTypeAndId(resourceType, id).ifPresent(repo::delete);
    }

    public boolean exists(String resourceType, String id) {
        return repo.findByResourceTypeAndId(resourceType, id).isPresent();
    }

    private String str(Object o) { return o == null ? null : o.toString(); }
}
