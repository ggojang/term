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

        String url, version, name, title, status, content;

        // id가 없을 때 URL로 기존 row를 먼저 조회해 id를 재사용 (중복 방지)
        if (id == null || id.isEmpty()) {
            String candidateUrl = null;
            if (resource instanceof org.hl7.fhir.r4.model.MetadataResource) {
                candidateUrl = ((org.hl7.fhir.r4.model.MetadataResource) resource).getUrl();
            }
            if (candidateUrl != null && !candidateUrl.isEmpty()) {
                Optional<String> existing = repo.findContentByResourceTypeAndUrl(resourceType, candidateUrl);
                if (existing.isPresent()) {
                    Resource existingRes = (Resource) parser.parseResource(existing.get());
                    id = existingRes.getIdElement().getIdPart();
                }
            }
            if (id == null || id.isEmpty()) {
                id = UUID.randomUUID().toString();
            }
            resource.setId(id);
        }

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

    /** IG 패키지 설치 시 ig_id를 함께 저장 */
    public String saveWithIg(String resourceType, String json, String igId) {
        IParser parser = FHIR_CTX.newJsonParser();
        Resource resource = (Resource) parser.parseResource(json);

        String id = resource.getIdElement().getIdPart();
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
            resource.setId(id);
        }

        String url, version, name, title, status;
        String content = parser.encodeResourceToString(resource);

        if (resource instanceof org.hl7.fhir.r4.model.NamingSystem) {
            org.hl7.fhir.r4.model.NamingSystem ns = (org.hl7.fhir.r4.model.NamingSystem) resource;
            url = ns.getUniqueId().stream()
                    .filter(u -> u.getType() == org.hl7.fhir.r4.model.NamingSystem.NamingSystemIdentifierType.URI)
                    .map(org.hl7.fhir.r4.model.NamingSystem.NamingSystemUniqueIdComponent::getValue)
                    .findFirst().orElse(null);
            version = null; name = ns.getName(); title = ns.hasTitle() ? ns.getTitle() : null;
            status = ns.getStatus() != null ? ns.getStatus().toCode() : null;
        } else {
            org.hl7.fhir.r4.model.MetadataResource meta = (org.hl7.fhir.r4.model.MetadataResource) resource;
            url = meta.getUrl(); version = meta.getVersion(); name = meta.getName();
            title = meta.getTitle(); status = meta.getStatus() != null ? meta.getStatus().toCode() : null;
        }

        repo.upsertWithIg(resourceType, id, url, version, name, title, status, igId, content);
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

    public List<FhirResource> searchByIg(String resourceType, String igId, String name) {
        List<Object[]> rows = (name != null && !name.isEmpty())
            ? repo.searchSummaryByResourceTypeAndIg(resourceType, igId, "%" + name + "%")
            : repo.findSummaryByResourceTypeAndIg(resourceType, igId);
        return toFhirResourceList(rows);
    }

    public List<FhirResource> search(String resourceType, String name) {
        List<Object[]> rows = (name != null && !name.isEmpty())
            ? repo.searchSummaryByResourceType(resourceType, "%" + name + "%")
            : repo.findSummaryByResourceType(resourceType);
        return toFhirResourceList(rows);
    }

    private List<FhirResource> toFhirResourceList(List<Object[]> rows) {
        return rows.stream()
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

    public void delete(String resourceType, String id) {
        repo.deleteByResourceTypeAndId(resourceType, id);
    }

    public boolean exists(String resourceType, String id) {
        return repo.countByResourceTypeAndId(resourceType, id) > 0;
    }

    private String str(Object o) { return o == null ? null : o.toString(); }
}
