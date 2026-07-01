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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "V-03. FHIR ValueSet")
@RestController
public class FhirValueSetController {

    private static final String FHIR_JSON = "application/fhir+json";

    @Autowired
    private FhirValueSetService svc;

    @ApiOperation(value = "ValueSet 단건 조회/수정/삭제 [GET]")
    @RequestMapping(value = FhirApi.VALUE_SET_ID, method = RequestMethod.GET, produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> read(@PathVariable String id) {
        Optional<String> json = svc.findById(id);
        if (!json.isPresent()) return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(json.get());
    }

    @ApiOperation(value = "ValueSet 목록 검색/생성 [GET]")
    @RequestMapping(value = FhirApi.VALUE_SET, method = RequestMethod.GET, produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public String search(@RequestParam(required = false) String name,
                         @RequestParam(required = false) String url,
                         @RequestParam(required = false) String ig) {
        List<FhirResource> results = (ig != null && !ig.isEmpty())
            ? svc.searchByIg(ig, name)
            : svc.search(name, url);
        return buildBundle(results);
    }

    @ApiOperation(value = "ValueSet 목록 검색/생성 [POST]")
    @RequestMapping(value = FhirApi.VALUE_SET, method = RequestMethod.POST,
            consumes = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE},
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> create(@RequestBody String body) {
        String id = svc.save(body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/fhir/ValueSet/" + id)
                .body("{\"id\":\"" + id + "\"}");
    }

    @ApiOperation(value = "ValueSet 단건 조회/수정/삭제 [PUT]")
    @RequestMapping(value = FhirApi.VALUE_SET_ID, method = RequestMethod.PUT,
            consumes = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE},
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> update(@PathVariable String id, @RequestBody String body) {
        boolean existed = svc.findById(id).isPresent();
        svc.save(body);
        return new ResponseEntity<String>(existed ? HttpStatus.OK : HttpStatus.CREATED);
    }

    @ApiOperation(value = "ValueSet 단건 조회/수정/삭제 [DELETE]")
    @RequestMapping(value = FhirApi.VALUE_SET_ID, method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable String id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── $expand ───────────────────────────────────────────

    @ApiOperation(value = "ValueSet $expand (by id) [GET]")
    @RequestMapping(value = FhirApi.VALUE_SET_EXPAND_ID, method = RequestMethod.GET, produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public String expandById(@PathVariable String id,
                             @RequestParam(required = false) String filter,
                             @RequestParam(required = false) Integer offset,
                             @RequestParam(required = false) Integer count,
                             @RequestParam(name = "system-version", required = false) String systemVersion) {
        ValueSet result = svc.expand(id, filter, offset, count, systemVersion);
        return encode(result);
    }

    @ApiOperation(value = "ValueSet $expand [GET]")
    @RequestMapping(value = FhirApi.VALUE_SET_EXPAND, method = RequestMethod.GET, produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public String expand(@RequestParam(required = false) String url,
                         @RequestParam(required = false) String filter,
                         @RequestParam(required = false) Integer offset,
                         @RequestParam(required = false) Integer count,
                         @RequestParam(name = "system-version", required = false) String systemVersion) {
        ValueSet result = svc.expand(url, filter, offset, count, systemVersion);
        return encode(result);
    }

    // ── $validate-code ────────────────────────────────────

    @ApiOperation(value = "ValueSet $validate-code [GET]")
    @RequestMapping(value = FhirApi.VALUE_SET_VALIDATE, method = RequestMethod.GET,
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public String validateCode(@RequestParam(required = false) String url,
                               @RequestParam(required = false) String valueSetUrl,
                               @RequestParam(required = false) String system,
                               @RequestParam(required = false) String code,
                               @RequestParam(required = false) String display) {
        String idOrUrl = url != null ? url : valueSetUrl;
        Parameters result = svc.validateCode(idOrUrl, system, code, display);
        return encode(result);
    }

    @ApiOperation(value = "ValueSet $validate-code [POST]")
    @RequestMapping(value = FhirApi.VALUE_SET_VALIDATE, method = RequestMethod.POST,
            consumes = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE},
            produces = {FHIR_JSON, MediaType.APPLICATION_JSON_VALUE})
    public String validateCodePost(@RequestBody String body) {
        IParser parser = FhirResourceService.FHIR_CTX.newJsonParser();
        Parameters params = (Parameters) parser.parseResource(body);
        String url = getParamStr(params, "url");
        String valueSetUrl = getParamStr(params, "valueSetUrl");
        String system = getParamStr(params, "system");
        String code = getParamStr(params, "code");
        String display = getParamStr(params, "display");
        String idOrUrl = url != null ? url : valueSetUrl;
        Parameters result = svc.validateCode(idOrUrl, system, code, display);
        return encode(result);
    }

    private String getParamStr(Parameters params, String name) {
        return params.getParameter().stream()
                .filter(p -> name.equals(p.getName()) && p.getValue() instanceof org.hl7.fhir.r4.model.PrimitiveType)
                .map(p -> ((org.hl7.fhir.r4.model.PrimitiveType<?>) p.getValue()).getValueAsString())
                .findFirst().orElse(null);
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
