package co.infoclinic.term.fhir.controller;

import ca.uhn.fhir.parser.IParser;
import co.infoclinic.term.fhir.api.FhirApi;
import co.infoclinic.term.fhir.service.FhirResourceService;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.infoclinic.term.fhir.model.entity.FhirPackage;
import co.infoclinic.term.fhir.repository.FhirPackageRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * IG NPM 패키지(.tgz) 업로드
 * POST /fhir/$install-package  (multipart file: package)
 *
 * CodeSystem / ValueSet / ConceptMap 리소스만 추출하여 fhir.resource 테이블에 저장
 */
@Api(tags = "V-06. FHIR Package")
@RestController
public class FhirPackageController {

    private static final Logger log = LoggerFactory.getLogger(FhirPackageController.class);

    private static final Set<String> TERMINOLOGY_TYPES = new HashSet<>(Arrays.asList("CodeSystem", "ValueSet", "ConceptMap", "NamingSystem"));

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private FhirResourceService resourceSvc;

    @Autowired
    private FhirPackageRepository packageRepo;

    @ApiOperation(value = "IG 패키지 설치 (tgz 업로드) [POST]")
    @RequestMapping(value = FhirApi.INSTALL_PACKAGE, method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = {MediaType.APPLICATION_JSON_VALUE, "application/fhir+json"})
    public String installPackage(@RequestParam("package") MultipartFile file) {

        List<String> saved   = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        List<String> errors  = new ArrayList<>();

        IParser parser = FhirResourceService.FHIR_CTX.newJsonParser();

        // 1차 패스: package.json 파싱으로 IG 메타 추출
        String igId = null;
        String igName = null;
        String igVersion = null;
        String igDesc = null;

        try (InputStream raw = file.getInputStream();
             BufferedInputStream buf = new BufferedInputStream(raw);
             GzipCompressorInputStream gz = new GzipCompressorInputStream(buf);
             TarArchiveInputStream tar = new TarArchiveInputStream(gz)) {

            TarArchiveEntry entry;
            while ((entry = tar.getNextTarEntry()) != null) {
                if (entry.isDirectory()) continue;
                String entryName = entry.getName();
                if (entryName.equals("package/package.json") || entryName.equals("package.json")) {
                    byte[] bytes = readEntry(tar);
                    try {
                        JsonNode node = MAPPER.readTree(bytes);
                        igName    = node.path("name").asText(null);
                        igVersion = node.path("version").asText(null);
                        igDesc    = node.path("description").asText(null);
                        if (igName != null) {
                            igId = igName + "#" + (igVersion != null ? igVersion : "unknown");
                        }
                    } catch (Exception e) {
                        log.warn("package.json parse failed: {}", e.getMessage());
                    }
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("First-pass failed: {}", e.getMessage());
        }

        // IG 메타 저장
        if (igId != null) {
            FhirPackage pkg = new FhirPackage(igId, igName, igVersion, igDesc, LocalDateTime.now());
            packageRepo.save(pkg);
            log.info("Package registered: {}", igId);
        }

        // 2차 패스: 리소스 추출 및 저장
        try (InputStream raw = file.getInputStream();
             BufferedInputStream buf = new BufferedInputStream(raw);
             GzipCompressorInputStream gz = new GzipCompressorInputStream(buf);
             TarArchiveInputStream tar = new TarArchiveInputStream(gz)) {

            TarArchiveEntry entry;
            while ((entry = tar.getNextTarEntry()) != null) {
                if (entry.isDirectory()) continue;

                String name = entry.getName();
                if (!name.endsWith(".json")) continue;
                if (name.endsWith("package.json") || name.contains(".index.json")) continue;

                byte[] bytes = readEntry(tar);
                String json = new String(bytes, StandardCharsets.UTF_8);

                try {
                    String resourceType = extractResourceType(json);
                    if (resourceType == null || !TERMINOLOGY_TYPES.contains(resourceType)) {
                        skipped.add(name + " (" + resourceType + ")");
                        continue;
                    }

                    String id = (igId != null)
                        ? resourceSvc.saveWithIg(resourceType, json, igId)
                        : resourceSvc.save(resourceType, json);
                    saved.add(resourceType + "/" + id + " ← " + name);
                    log.info("Installed {} from {}", resourceType + "/" + id, name);

                } catch (Exception e) {
                    errors.add(name + ": " + e.getMessage());
                    log.warn("Failed to parse {}: {}", name, e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Package install failed", e);
            return buildOutcome(false, "Package install failed: " + e.getMessage(), saved, skipped, errors);
        }

        return buildOutcome(true,
                "Installed " + saved.size() + " resource(s) [" + (igId != null ? igId : "no package.json") + "]. Skipped " + skipped.size() + ".",
                saved, skipped, errors);
    }

    /** 설치된 IG 패키지 목록 */
    @ApiOperation(value = "설치된 IG 패키지 목록 [GET]")
    @RequestMapping(value = "/fhir/Package", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> listPackages() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : packageRepo.findAllSummary()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",          str(row[0]));
            m.put("name",        str(row[1]));
            m.put("version",     str(row[2]));
            m.put("description", str(row[3]));
            m.put("installedAt", str(row[4]));
            // 리소스 타입별 건수
            Map<String, Long> counts = new LinkedHashMap<>();
            for (Object[] cnt : packageRepo.countByResourceType(str(row[0]))) {
                counts.put(str(cnt[0]), ((Number) cnt[1]).longValue());
            }
            m.put("counts", counts);
            result.add(m);
        }
        return result;
    }

    private String str(Object o) { return o == null ? null : o.toString(); }

    /**
     * JSON 단일 리소스 업로드 (Bundle 또는 단건)
     */
    @ApiOperation(value = "IG 패키지 설치 (tgz 업로드) [POST]")
    @RequestMapping(value = FhirApi.INSTALL_PACKAGE, method = RequestMethod.POST,
            consumes = {MediaType.APPLICATION_JSON_VALUE, "application/fhir+json"},
            produces = {MediaType.APPLICATION_JSON_VALUE, "application/fhir+json"})
    public String installJson(@RequestBody String body) {
        List<String> saved  = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        IParser parser = FhirResourceService.FHIR_CTX.newJsonParser();

        try {
            String resourceType = extractResourceType(body);
            if ("Bundle".equals(resourceType)) {
                Bundle bundle = (Bundle) parser.parseResource(body);
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    Resource res = entry.getResource();
                    if (res == null) continue;
                    String type = res.getResourceType().name();
                    if (!TERMINOLOGY_TYPES.contains(type)) continue;
                    try {
                        String id = resourceSvc.save(type, parser.encodeResourceToString(res));
                        saved.add(type + "/" + id);
                    } catch (Exception e) {
                        errors.add(type + ": " + e.getMessage());
                    }
                }
            } else if (TERMINOLOGY_TYPES.contains(resourceType)) {
                String id = resourceSvc.save(resourceType, body);
                saved.add(resourceType + "/" + id);
            } else {
                return buildOutcome(false, "Only CodeSystem/ValueSet/ConceptMap supported", saved, Collections.<String>emptyList(), errors);
            }
        } catch (Exception e) {
            return buildOutcome(false, "Parse error: " + e.getMessage(), saved, Collections.<String>emptyList(), errors);
        }

        return buildOutcome(true, "Installed " + saved.size() + " resource(s).", saved, Collections.<String>emptyList(), errors);
    }

    // ── 내부 유틸 ─────────────────────────────────────────

    private byte[] readEntry(TarArchiveInputStream tar) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int len;
        while ((len = tar.read(buf)) != -1) out.write(buf, 0, len);
        return out.toByteArray();
    }

    private String extractResourceType(String json) {
        int idx = json.indexOf("\"resourceType\"");
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx);
        if (colon < 0) return null;
        int q1 = json.indexOf('"', colon);
        if (q1 < 0) return null;
        int q2 = json.indexOf('"', q1 + 1);
        if (q2 < 0) return null;
        return json.substring(q1 + 1, q2);
    }

    private String buildOutcome(boolean success, String message,
                                List<String> saved, List<String> skipped, List<String> errors) {
        Parameters out = new Parameters();
        out.addParameter().setName("result").setValue(new BooleanType(success));
        out.addParameter().setName("message").setValue(new StringType(message));
        for (String s : saved)   out.addParameter().setName("saved").setValue(new StringType(s));
        for (String s : skipped) out.addParameter().setName("skipped").setValue(new StringType(s));
        for (String s : errors)  out.addParameter().setName("error").setValue(new StringType(s));
        return FhirResourceService.FHIR_CTX.newJsonParser().setPrettyPrint(true).encodeResourceToString(out);
    }
}
