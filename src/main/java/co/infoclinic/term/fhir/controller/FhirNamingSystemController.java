package co.infoclinic.term.fhir.controller;

import ca.uhn.fhir.parser.IParser;
import co.infoclinic.term.fhir.api.FhirApi;
import co.infoclinic.term.fhir.model.entity.FhirResource;
import co.infoclinic.term.fhir.service.FhirResourceService;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "V-05. FHIR NamingSystem")
@RestController
public class FhirNamingSystemController {

    private static final String FHIR_JSON = "application/fhir+json";
    private static final String RESOURCE_TYPE = "NamingSystem";

    @Autowired
    private FhirResourceService svc;

    @ApiOperation(value = "NamingSystem 단건 조회/수정/삭제 [GET]")
    @RequestMapping(value = FhirApi.NAMING_SYSTEM_ID, method = RequestMethod.GET,
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> read(@PathVariable String id) {
        Optional<String> json = svc.findById(RESOURCE_TYPE, id);
        if (!json.isPresent()) return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(json.get());
    }

    @ApiOperation(value = "NamingSystem 목록 검색/생성 [GET]")
    @RequestMapping(value = FhirApi.NAMING_SYSTEM, method = RequestMethod.GET,
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public String search(@RequestParam(required = false) String name,
                         @RequestParam(required = false) String ig) {
        List<FhirResource> results = (ig != null && !ig.isEmpty())
            ? svc.searchByIg(RESOURCE_TYPE, ig, name)
            : svc.search(RESOURCE_TYPE, name);
        return buildBundle(results);
    }

    @ApiOperation(value = "NamingSystem 목록 검색/생성 [POST]")
    @RequestMapping(value = FhirApi.NAMING_SYSTEM, method = RequestMethod.POST,
            consumes = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE},
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> create(@RequestBody String body) {
        String id = svc.save(RESOURCE_TYPE, body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", FhirApi.BASE + "/NamingSystem/" + id)
                .body("{\"id\":\"" + id + "\"}");
    }

    @ApiOperation(value = "NamingSystem 단건 조회/수정/삭제 [PUT]")
    @RequestMapping(value = FhirApi.NAMING_SYSTEM_ID, method = RequestMethod.PUT,
            consumes = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE},
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> update(@PathVariable String id, @RequestBody String body) {
        boolean existed = svc.exists(RESOURCE_TYPE, id);
        svc.save(RESOURCE_TYPE, body);
        return new ResponseEntity<String>(existed ? HttpStatus.OK : HttpStatus.CREATED);
    }

    @ApiOperation(value = "NamingSystem 단건 조회/수정/삭제 [DELETE]")
    @RequestMapping(value = FhirApi.NAMING_SYSTEM_ID, method = RequestMethod.DELETE)
    public ResponseEntity<String> delete(@PathVariable String id) {
        svc.delete(RESOURCE_TYPE, id);
        return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
    }

    // ── $preferred-id ─────────────────────────────────────────────────────────
    // GET /NamingSystem/$preferred-id?id={value}&type={oid|uri|uuid|other}
    @ApiOperation(value = "NamingSystem $preferred-id [GET]")
    @RequestMapping(value = FhirApi.NAMING_SYSTEM + "/$preferred-id", method = RequestMethod.GET,
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> preferredId(@RequestParam String id,
                                              @RequestParam String type) {
        IParser parser = FhirResourceService.FHIR_CTX.newJsonParser();
        List<FhirResource> all = svc.search(RESOURCE_TYPE, null);
        for (FhirResource r : all) {
            if (r.getContent() == null) continue;
            NamingSystem ns = (NamingSystem) parser.parseResource(r.getContent());
            boolean hasId = ns.getUniqueId().stream().anyMatch(u ->
                    id.equals(u.getValue()) && (type == null || type.equals(u.getType() != null ? u.getType().toCode() : "")));
            if (!hasId) continue;
            // preferred identifier 반환
            NamingSystem.NamingSystemUniqueIdComponent preferred = ns.getUniqueId().stream()
                    .filter(u -> u.hasPreferred() && u.getPreferred())
                    .findFirst()
                    .orElse(ns.getUniqueId().isEmpty() ? null : ns.getUniqueId().get(0));
            if (preferred != null) {
                Parameters out = new Parameters();
                out.addParameter().setName("result").setValue(new StringType(preferred.getValue()));
                return ResponseEntity.ok(
                        FhirResourceService.FHIR_CTX.newJsonParser().setPrettyPrint(true).encodeResourceToString(out));
            }
        }
        Parameters notFound = new Parameters();
        notFound.addParameter().setName("result").setValue(new StringType(""));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(FhirResourceService.FHIR_CTX.newJsonParser().setPrettyPrint(true).encodeResourceToString(notFound));
    }

    private String buildBundle(List<FhirResource> resources) {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTotal(resources.size());
        IParser parser = FhirResourceService.FHIR_CTX.newJsonParser();
        for (FhirResource r : resources) {
            Bundle.BundleEntryComponent entry = bundle.addEntry();
            entry.setFullUrl(FhirApi.BASE + "/NamingSystem/" + r.getId());
            Resource res;
            if (r.getContent() != null) {
                res = (Resource) parser.parseResource(r.getContent());
            } else {
                NamingSystem ns = new NamingSystem();
                ns.setId(r.getId());
                ns.setName(r.getName() != null ? r.getName() : "");
                if (r.getStatus() != null) ns.setStatus(Enumerations.PublicationStatus.fromCode(r.getStatus()));
                res = ns;
            }
            entry.setResource(res);
        }
        return FhirResourceService.FHIR_CTX.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }
}
