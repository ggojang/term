package co.infoclinic.term.fhir.controller;

import ca.uhn.fhir.parser.IParser;
import co.infoclinic.term.fhir.api.FhirApi;
import co.infoclinic.term.fhir.model.entity.FhirResource;
import co.infoclinic.term.fhir.service.FhirConceptMapService;
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

@Api(tags = "V-04. FHIR ConceptMap")
@RestController
public class FhirConceptMapController {

    private static final String FHIR_JSON = "application/fhir+json";

    @Autowired
    private FhirConceptMapService svc;

    @ApiOperation(value = "ConceptMap 단건 조회/수정/삭제 [GET]")
    @RequestMapping(value = FhirApi.CONCEPT_MAP_ID, method = RequestMethod.GET, produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> read(@PathVariable String id) {
        Optional<String> json = svc.findById(id);
        if (!json.isPresent()) return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(json.get());
    }

    @ApiOperation(value = "ConceptMap 목록 검색/생성 [GET]")
    @RequestMapping(value = FhirApi.CONCEPT_MAP, method = RequestMethod.GET, produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public String search(@RequestParam(required = false) String name,
                         @RequestParam(required = false) String url,
                         @RequestParam(required = false) String ig) {
        List<FhirResource> results = (ig != null && !ig.isEmpty())
            ? svc.searchByIg(ig, name)
            : svc.search(name, url);
        return buildBundle(results);
    }

    @ApiOperation(value = "ConceptMap 목록 검색/생성 [POST]")
    @RequestMapping(value = FhirApi.CONCEPT_MAP, method = RequestMethod.POST,
            consumes = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE},
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> create(@RequestBody String body) {
        String id = svc.save(body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/fhir/ConceptMap/" + id)
                .body("{\"id\":\"" + id + "\"}");
    }

    @ApiOperation(value = "ConceptMap 단건 조회/수정/삭제 [PUT]")
    @RequestMapping(value = FhirApi.CONCEPT_MAP_ID, method = RequestMethod.PUT,
            consumes = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE},
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> update(@PathVariable String id, @RequestBody String body) {
        boolean existed = svc.findById(id).isPresent();
        svc.save(body);
        return new ResponseEntity<String>(existed ? HttpStatus.OK : HttpStatus.CREATED);
    }

    @ApiOperation(value = "ConceptMap 단건 조회/수정/삭제 [DELETE]")
    @RequestMapping(value = FhirApi.CONCEPT_MAP_ID, method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable String id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── $translate ────────────────────────────────────────

    @ApiOperation(value = "ConceptMap $translate 매핑 변환 [GET]")
    @RequestMapping(value = FhirApi.CONCEPT_MAP_TRANSLATE, method = RequestMethod.GET, produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public String translate(@RequestParam String system,
                            @RequestParam String code,
                            @RequestParam(required = false) String targetSystem,
                            @RequestParam(required = false) String url) {
        Parameters result = svc.translate(system, code, targetSystem, url);
        return encode(result);
    }

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
            entry.setFullUrl("/stom/fhir/ConceptMap/" + r.getId());
            Resource res;
            if (r.getContent() != null) {
                res = (Resource) parser.parseResource(r.getContent());
            } else {
                ConceptMap cm = new ConceptMap();
                cm.setId(r.getId());
                cm.setUrl(r.getUrl());
                cm.setName(r.getName());
                cm.setTitle(r.getTitle());
                if (r.getStatus() != null) cm.setStatus(Enumerations.PublicationStatus.fromCode(r.getStatus()));
                res = cm;
            }
            entry.setResource(res);
        }
        return FhirResourceService.FHIR_CTX.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }
}
