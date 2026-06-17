package co.infoclinic.term.fhir.controller;

import ca.uhn.fhir.parser.IParser;
import co.infoclinic.term.fhir.api.FhirApi;
import co.infoclinic.term.fhir.model.entity.FhirResource;
import co.infoclinic.term.fhir.service.FhirResourceService;
import co.infoclinic.term.fhir.service.FhirValueSetService;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class FhirValueSetController {

    private static final String FHIR_JSON = "application/fhir+json";

    @Autowired
    private FhirValueSetService svc;

    @RequestMapping(value = FhirApi.VALUE_SET_ID, method = RequestMethod.GET, produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> read(@PathVariable String id) {
        Optional<String> json = svc.findById(id);
        if (!json.isPresent()) return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(json.get());
    }

    @RequestMapping(value = FhirApi.VALUE_SET, method = RequestMethod.GET, produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public String search(@RequestParam(required = false) String name,
                         @RequestParam(required = false) String url) {
        List<FhirResource> results = svc.search(name, url);
        return buildBundle(results);
    }

    @RequestMapping(value = FhirApi.VALUE_SET, method = RequestMethod.POST,
            consumes = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE},
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> create(@RequestBody String body) {
        String id = svc.save(body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/fhir/ValueSet/" + id)
                .body("{\"id\":\"" + id + "\"}");
    }

    @RequestMapping(value = FhirApi.VALUE_SET_ID, method = RequestMethod.PUT,
            consumes = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE},
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> update(@PathVariable String id, @RequestBody String body) {
        boolean existed = svc.findById(id).isPresent();
        svc.save(body);
        return new ResponseEntity<String>(existed ? HttpStatus.OK : HttpStatus.CREATED);
    }

    @RequestMapping(value = FhirApi.VALUE_SET_ID, method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable String id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── $expand ───────────────────────────────────────────

    @RequestMapping(value = FhirApi.VALUE_SET_EXPAND_ID, method = RequestMethod.GET, produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public String expandById(@PathVariable String id,
                             @RequestParam(required = false) String filter,
                             @RequestParam(required = false) Integer offset,
                             @RequestParam(required = false) Integer count) {
        ValueSet result = svc.expand(id, filter, offset, count);
        return encode(result);
    }

    @RequestMapping(value = FhirApi.VALUE_SET_EXPAND, method = RequestMethod.GET, produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public String expand(@RequestParam(required = false) String url,
                         @RequestParam(required = false) String filter,
                         @RequestParam(required = false) Integer offset,
                         @RequestParam(required = false) Integer count) {
        ValueSet result = svc.expand(url, filter, offset, count);
        return encode(result);
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
            entry.setFullUrl("/stom/fhir/ValueSet/" + r.getId());
            Resource res;
            if (r.getContent() != null) {
                res = (Resource) parser.parseResource(r.getContent());
            } else {
                ValueSet vs = new ValueSet();
                vs.setId(r.getId());
                vs.setUrl(r.getUrl());
                vs.setName(r.getName());
                vs.setTitle(r.getTitle());
                if (r.getStatus() != null) vs.setStatus(Enumerations.PublicationStatus.fromCode(r.getStatus()));
                res = vs;
            }
            entry.setResource(res);
        }
        return FhirResourceService.FHIR_CTX.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }
}
