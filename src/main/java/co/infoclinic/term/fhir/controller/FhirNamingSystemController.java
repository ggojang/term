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

@RestController
public class FhirNamingSystemController {

    private static final String FHIR_JSON = "application/fhir+json";
    private static final String RESOURCE_TYPE = "NamingSystem";

    @Autowired
    private FhirResourceService svc;

    @RequestMapping(value = FhirApi.NAMING_SYSTEM_ID, method = RequestMethod.GET,
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> read(@PathVariable String id) {
        Optional<String> json = svc.findById(RESOURCE_TYPE, id);
        if (!json.isPresent()) return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(json.get());
    }

    @RequestMapping(value = FhirApi.NAMING_SYSTEM, method = RequestMethod.GET,
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public String search(@RequestParam(required = false) String name) {
        List<FhirResource> results = svc.search(RESOURCE_TYPE, name);
        return buildBundle(results);
    }

    @RequestMapping(value = FhirApi.NAMING_SYSTEM, method = RequestMethod.POST,
            consumes = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE},
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> create(@RequestBody String body) {
        String id = svc.save(RESOURCE_TYPE, body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", FhirApi.BASE + "/NamingSystem/" + id)
                .body("{\"id\":\"" + id + "\"}");
    }

    @RequestMapping(value = FhirApi.NAMING_SYSTEM_ID, method = RequestMethod.PUT,
            consumes = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE},
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> update(@PathVariable String id, @RequestBody String body) {
        boolean existed = svc.exists(RESOURCE_TYPE, id);
        svc.save(RESOURCE_TYPE, body);
        return new ResponseEntity<String>(existed ? HttpStatus.OK : HttpStatus.CREATED);
    }

    @RequestMapping(value = FhirApi.NAMING_SYSTEM_ID, method = RequestMethod.DELETE)
    public ResponseEntity<String> delete(@PathVariable String id) {
        svc.delete(RESOURCE_TYPE, id);
        return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
    }

    private String buildBundle(List<FhirResource> resources) {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTotal(resources.size());
        IParser parser = FhirResourceService.FHIR_CTX.newJsonParser();
        for (FhirResource r : resources) {
            Bundle.BundleEntryComponent entry = bundle.addEntry();
            entry.setFullUrl("/stom/fhir/NamingSystem/" + r.getId());
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
