package co.infoclinic.term.fhir.controller;

import ca.uhn.fhir.parser.IParser;
import co.infoclinic.term.fhir.api.FhirApi;
import co.infoclinic.term.fhir.model.entity.FhirResource;
import co.infoclinic.term.fhir.service.FhirCodeSystemService;
import co.infoclinic.term.fhir.service.FhirResourceService;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class FhirCodeSystemController {

    private static final String FHIR_JSON = "application/fhir+json";

    @Autowired
    private FhirCodeSystemService svc;

    // ── CRUD ──────────────────────────────────────────────

    @RequestMapping(value = FhirApi.CODE_SYSTEM_ID, method = RequestMethod.GET, produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> read(@PathVariable String id) {
        Optional<String> json = svc.findById(id);
        if (!json.isPresent()) return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(json.get());
    }

    @RequestMapping(value = FhirApi.CODE_SYSTEM, method = RequestMethod.GET, produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public String search(@RequestParam(required = false) String name,
                         @RequestParam(required = false) String url) {
        List<FhirResource> results = svc.search(name, url);
        return buildBundle(results);
    }

    @RequestMapping(value = FhirApi.CODE_SYSTEM, method = RequestMethod.POST,
            consumes = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE},
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> create(@RequestBody String body) {
        String id = svc.save(body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/fhir/CodeSystem/" + id)
                .body("{\"id\":\"" + id + "\"}");
    }

    @RequestMapping(value = FhirApi.CODE_SYSTEM_ID, method = RequestMethod.PUT,
            consumes = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE},
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> update(@PathVariable String id, @RequestBody String body) {
        boolean existed = svc.findById(id).isPresent();
        svc.save(body);
        return new ResponseEntity<String>(existed ? HttpStatus.OK : HttpStatus.CREATED);
    }

    @RequestMapping(value = FhirApi.CODE_SYSTEM_ID, method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable String id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── $lookup ───────────────────────────────────────────

    @RequestMapping(value = FhirApi.CODE_SYSTEM_LOOKUP, method = RequestMethod.GET, produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public String lookup(@RequestParam String system,
                         @RequestParam String code,
                         @RequestParam(required = false) String displayLanguage) {
        Parameters result = svc.lookup(system, code, displayLanguage);
        return encode(result);
    }

    @RequestMapping(value = FhirApi.CODE_SYSTEM_LOOKUP, method = RequestMethod.POST,
            consumes = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE},
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public String lookupPost(@RequestBody String body) {
        IParser parser = FhirResourceService.FHIR_CTX.newJsonParser();
        Parameters params = (Parameters) parser.parseResource(body);
        String system = getParamString(params, "system");
        String code   = getParamString(params, "code");
        String lang   = getParamString(params, "displayLanguage");
        return encode(svc.lookup(system, code, lang));
    }

    // ── $validate-code ────────────────────────────────────

    @RequestMapping(value = FhirApi.CODE_SYSTEM_VALIDATE, method = RequestMethod.GET, produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public String validateCode(@RequestParam String system,
                               @RequestParam String code,
                               @RequestParam(required = false) String display) {
        return encode(svc.validateCode(system, code, display));
    }

    // ── $subsumes ─────────────────────────────────────────

    @RequestMapping(value = FhirApi.CODE_SYSTEM_SUBSUMES, method = RequestMethod.GET, produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public String subsumes(@RequestParam String system,
                           @RequestParam String codeA,
                           @RequestParam String codeB) {
        return encode(svc.subsumes(system, codeA, codeB));
    }

    // ── 유틸 ──────────────────────────────────────────────

    private String encode(Resource resource) {
        return FhirResourceService.FHIR_CTX.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
    }

    private String buildBundle(List<FhirResource> resources) {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTotal(resources.size());
        IParser parser = FhirResourceService.FHIR_CTX.newJsonParser();
        for (FhirResource r : resources) {
            Bundle.BundleEntryComponent entry = bundle.addEntry();
            entry.setFullUrl("/stom/fhir/CodeSystem/" + r.getId());
            Resource res;
            if (r.getContent() != null) {
                res = (Resource) parser.parseResource(r.getContent());
            } else {
                CodeSystem cs = new CodeSystem();
                cs.setId(r.getId());
                cs.setUrl(r.getUrl());
                cs.setName(r.getName());
                cs.setTitle(r.getTitle());
                if (r.getStatus() != null) cs.setStatus(Enumerations.PublicationStatus.fromCode(r.getStatus()));
                res = cs;
            }
            entry.setResource(res);
        }
        return FhirResourceService.FHIR_CTX.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }

    private String getParamString(Parameters params, String name) {
        return params.getParameter().stream()
                .filter(p -> name.equals(p.getName()) && p.getValue() instanceof PrimitiveType)
                .map(p -> ((PrimitiveType<?>) p.getValue()).getValueAsString())
                .findFirst().orElse(null);
    }
}
