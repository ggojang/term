package co.infoclinic.term.fhir.service;

import ca.uhn.fhir.parser.IParser;
import co.infoclinic.term.fhir.model.entity.FhirResource;
import co.infoclinic.term.icd10.model.entity.Icd10Class;
import co.infoclinic.term.icd10.repository.Icd10ClassRepository;
import co.infoclinic.term.loinc.model.entity.LinguisticVariant;
import co.infoclinic.term.loinc.repository.LinguisticVariantRepository;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

/**
 * FHIR CodeSystem 서비스
 * - fhir.resource 테이블 저장/조회
 * - SNOMED CT / LOINC / KCD-9 DB 브리징 ($lookup, $validate-code)
 */
@Service
public class FhirCodeSystemService {

    private static final Logger log = LoggerFactory.getLogger(FhirCodeSystemService.class);

    // Well-known canonical URLs
    public static final String URL_SNOMED = "http://snomed.info/sct";
    public static final String URL_LOINC  = "http://loinc.org";
    public static final String URL_KCD9   = "http://koicd.kr/fhir/kcd9";

    @Autowired
    private FhirResourceService resourceSvc;

    @Autowired
    private Icd10ClassRepository icd10Repo;

    @Autowired
    private LinguisticVariantRepository lvRepo;

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    // ── CRUD ──────────────────────────────────────────────

    public String save(String json) {
        return resourceSvc.save("CodeSystem", json);
    }

    public Optional<String> findById(String id) {
        // 내장 코드시스템 처리
        if ("snomed".equals(id)) return Optional.of(buildSnomedStub());
        if ("loinc".equals(id))  return Optional.of(buildLoincStub());
        if ("kcd9".equals(id))   return Optional.of(buildKcd9Stub());
        return resourceSvc.findById("CodeSystem", id);
    }

    public Optional<String> findByUrl(String url) {
        if (URL_SNOMED.equals(url)) return Optional.of(buildSnomedStub());
        if (URL_LOINC.equals(url))  return Optional.of(buildLoincStub());
        if (URL_KCD9.equals(url))   return Optional.of(buildKcd9Stub());
        return resourceSvc.findByUrl("CodeSystem", url);
    }

    public List<FhirResource> search(String name, String url) {
        if (url != null && !url.isEmpty()) {
            return resourceSvc.findByUrl("CodeSystem", url)
                    .map(json -> {
                        FhirResource r = new FhirResource();
                        r.setResourceType("CodeSystem"); r.setUrl(url); r.setContent(json);
                        return Collections.singletonList(r);
                    }).orElse(Collections.emptyList());
        }
        return resourceSvc.search("CodeSystem", name);
    }

    public void delete(String id) {
        resourceSvc.delete("CodeSystem", id);
    }

    // ── $lookup ───────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Parameters lookup(String system, String code, String displayLanguage) {
        Parameters out = new Parameters();

        if (URL_SNOMED.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT d.term, c.effective_time FROM term.description d " +
                "JOIN term.concept c ON c.concept_id = d.concept_id " +
                "WHERE d.concept_id = :code AND d.type_id = '900000000000003001' " +
                "AND d.active = 1 LIMIT 1");
            q.setParameter("code", code);
            List<Object[]> rows = q.getResultList();
            if (rows.isEmpty()) return outcomeNotFound(out, system, code);
            Object[] row = rows.get(0);
            out.addParameter().setName("name").setValue(new StringType("SNOMED CT"));
            out.addParameter().setName("display").setValue(new StringType((String) row[0]));
            out.addParameter().setName("system").setValue(new UriType(system));
            out.addParameter().setName("code").setValue(new CodeType(code));
            return out;
        }

        if (URL_LOINC.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT long_common_name, component FROM loinc.loinc WHERE code = :code LIMIT 1");
            q.setParameter("code", code);
            List<Object[]> rows = q.getResultList();
            if (rows.isEmpty()) return outcomeNotFound(out, system, code);
            Object[] row = rows.get(0);
            String display = row[0] != null ? (String) row[0] : (String) row[1];
            out.addParameter().setName("name").setValue(new StringType("LOINC"));
            out.addParameter().setName("display").setValue(new StringType(display != null ? display : ""));
            out.addParameter().setName("system").setValue(new UriType(system));
            out.addParameter().setName("code").setValue(new CodeType(code));
            // Korean Linguistic Variant designation
            addLoincDesignations(code, out);
            return out;
        }

        if (URL_KCD9.equals(system)) {
            Icd10Class icd10 = icd10Repo.findByCode(code);
            if (icd10 == null) return outcomeNotFound(out, system, code);
            String display = icd10.getKoreanLabel() != null ? icd10.getKoreanLabel() : icd10.getLabel();
            out.addParameter().setName("name").setValue(new StringType("KCD-9"));
            out.addParameter().setName("display").setValue(new StringType(display != null ? display : ""));
            out.addParameter().setName("system").setValue(new UriType(system));
            out.addParameter().setName("code").setValue(new CodeType(code));
            return out;
        }

        // fhir.resource 저장된 CodeSystem에서 조회
        return lookupFromStoredCodeSystem(system, code, out);
    }

    // ── $validate-code ────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Parameters validateCode(String system, String code, String display) {
        Parameters out = new Parameters();
        boolean valid = false;
        String actualDisplay = null;

        if (URL_SNOMED.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT d.term FROM term.description d " +
                "WHERE d.concept_id = :code AND d.type_id = '900000000000003001' AND d.active = 1 LIMIT 1");
            q.setParameter("code", code);
            List<String> rows = q.getResultList();
            valid = !rows.isEmpty();
            if (valid) actualDisplay = rows.get(0);
        } else if (URL_LOINC.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT long_common_name FROM loinc.loinc WHERE code = :code LIMIT 1");
            q.setParameter("code", code);
            List<String> rows = q.getResultList();
            valid = !rows.isEmpty();
            if (valid) actualDisplay = rows.get(0);
        } else if (URL_KCD9.equals(system)) {
            Icd10Class icd10 = icd10Repo.findByCode(code);
            valid = (icd10 != null);
            if (valid) actualDisplay = icd10.getKoreanLabel() != null ? icd10.getKoreanLabel() : icd10.getLabel();
        } else {
            return validateFromStoredCodeSystem(system, code, display, out);
        }

        out.addParameter().setName("result").setValue(new BooleanType(valid));
        if (valid && actualDisplay != null) {
            out.addParameter().setName("display").setValue(new StringType(actualDisplay));
        }
        if (!valid) {
            out.addParameter().setName("message").setValue(
                new StringType("Code " + code + " not found in " + system));
        }
        return out;
    }

    // ── $subsumes (SNOMED CT 전용) ────────────────────────

    @SuppressWarnings("unchecked")
    public Parameters subsumes(String system, String codeA, String codeB) {
        Parameters out = new Parameters();
        if (!URL_SNOMED.equals(system)) {
            out.addParameter().setName("outcome").setValue(new CodeType("not-subsumed"));
            return out;
        }

        // codeA subsumes codeB? (B's path contains A)
        Query q = em.createNativeQuery(
            "SELECT COUNT(*) FROM term.tc WHERE concept_id = :b AND path LIKE '%' || :a || '%'");
        q.setParameter("a", codeA);
        q.setParameter("b", codeB);
        Number aSubsumesB = (Number) q.getSingleResult();

        // codeB subsumes codeA? (A's path contains B)
        Query q2 = em.createNativeQuery(
            "SELECT COUNT(*) FROM term.tc WHERE concept_id = :a AND path LIKE '%' || :b || '%'");
        q2.setParameter("a", codeA);
        q2.setParameter("b", codeB);
        Number bSubsumesA = (Number) q2.getSingleResult();

        String outcome;
        if (codeA.equals(codeB))          outcome = "equivalent";
        else if (aSubsumesB.intValue() > 0 && bSubsumesA.intValue() > 0) outcome = "equivalent";
        else if (aSubsumesB.intValue() > 0) outcome = "subsumes";
        else if (bSubsumesA.intValue() > 0) outcome = "subsumed-by";
        else                              outcome = "not-subsumed";

        out.addParameter().setName("outcome").setValue(new CodeType(outcome));
        return out;
    }

    // ── 내부 유틸 ─────────────────────────────────────────

    private Parameters lookupFromStoredCodeSystem(String system, String code, Parameters out) {
        Optional<String> csJson = resourceSvc.findByUrl("CodeSystem", system);
        if (!csJson.isPresent()) return outcomeNotFound(out, system, code);

        IParser parser = FhirResourceService.FHIR_CTX.newJsonParser();
        CodeSystem cs = (CodeSystem) parser.parseResource(csJson.get());
        Optional<CodeSystem.ConceptDefinitionComponent> concept = findConcept(cs.getConcept(), code);
        if (!concept.isPresent()) return outcomeNotFound(out, system, code);

        out.addParameter().setName("name").setValue(new StringType(cs.getName()));
        out.addParameter().setName("display").setValue(new StringType(concept.get().getDisplay()));
        out.addParameter().setName("system").setValue(new UriType(system));
        out.addParameter().setName("code").setValue(new CodeType(code));
        // designation
        for (CodeSystem.ConceptDefinitionDesignationComponent des : concept.get().getDesignation()) {
            Parameters.ParametersParameterComponent p = out.addParameter().setName("designation");
            if (des.hasLanguage()) p.addPart().setName("language").setValue(new CodeType(des.getLanguage()));
            if (des.hasValue())    p.addPart().setName("value").setValue(new StringType(des.getValue()));
        }
        return out;
    }

    private Parameters validateFromStoredCodeSystem(String system, String code, String display, Parameters out) {
        Optional<String> csJson = resourceSvc.findByUrl("CodeSystem", system);
        if (!csJson.isPresent()) {
            out.addParameter().setName("result").setValue(new BooleanType(false));
            out.addParameter().setName("message").setValue(new StringType("CodeSystem not found: " + system));
            return out;
        }
        IParser parser = FhirResourceService.FHIR_CTX.newJsonParser();
        CodeSystem cs = (CodeSystem) parser.parseResource(csJson.get());
        Optional<CodeSystem.ConceptDefinitionComponent> concept = findConcept(cs.getConcept(), code);
        boolean valid = concept.isPresent();
        out.addParameter().setName("result").setValue(new BooleanType(valid));
        if (valid) {
            out.addParameter().setName("display").setValue(new StringType(concept.get().getDisplay()));
        }
        return out;
    }

    private Optional<CodeSystem.ConceptDefinitionComponent> findConcept(
            List<CodeSystem.ConceptDefinitionComponent> concepts, String code) {
        for (CodeSystem.ConceptDefinitionComponent c : concepts) {
            if (code.equals(c.getCode())) return Optional.of(c);
            Optional<CodeSystem.ConceptDefinitionComponent> found = findConcept(c.getConcept(), code);
            if (found.isPresent()) return found;
        }
        return Optional.empty();
    }

    private void addLoincDesignations(String code, Parameters out) {
        List<LinguisticVariant> variants = lvRepo.findListByCode(code);
        for (LinguisticVariant lv : variants) {
            String display = resolveLoincDisplay(lv);
            if (display == null || display.isEmpty()) continue;
            String lang = lv.getIsoLang() + (lv.getIsoCountry() != null ? "-" + lv.getIsoCountry() : "");
            Parameters.ParametersParameterComponent p = out.addParameter().setName("designation");
            p.addPart().setName("language").setValue(new CodeType(lang));
            p.addPart().setName("value").setValue(new StringType(display));
        }
    }

    private String resolveLoincDisplay(LinguisticVariant lv) {
        if (lv.getLongCommonName() != null && !lv.getLongCommonName().isEmpty())
            return lv.getLongCommonName();
        // longCommonName이 없으면 파트 조합 (특히 한국어)
        StringBuilder sb = new StringBuilder();
        if (lv.getComponent()   != null) sb.append(lv.getComponent());
        if (lv.getProperty()    != null && !lv.getProperty().isEmpty())    sb.append(" [").append(lv.getProperty()).append("]");
        if (lv.getTimeAspect()  != null && !lv.getTimeAspect().isEmpty())  sb.append(":").append(lv.getTimeAspect());
        if (lv.getSystem()      != null && !lv.getSystem().isEmpty())      sb.append(":").append(lv.getSystem());
        if (lv.getScaleType()   != null && !lv.getScaleType().isEmpty())   sb.append(":").append(lv.getScaleType());
        if (lv.getMethodType()  != null && !lv.getMethodType().isEmpty())  sb.append(":").append(lv.getMethodType());
        return sb.length() > 0 ? sb.toString() : null;
    }

    private Parameters outcomeNotFound(Parameters out, String system, String code) {
        out.addParameter().setName("result").setValue(new BooleanType(false));
        out.addParameter().setName("message").setValue(
            new StringType("Code '" + code + "' not found in system '" + system + "'"));
        return out;
    }

    private String buildSnomedStub() {
        CodeSystem cs = new CodeSystem();
        cs.setId("snomed");
        cs.setUrl(URL_SNOMED);
        cs.setName("SNOMEDCT");
        cs.setTitle("SNOMED Clinical Terms");
        cs.setStatus(Enumerations.PublicationStatus.ACTIVE);
        cs.setContent(CodeSystem.CodeSystemContentMode.NOTPRESENT);
        cs.setPublisher("SNOMED International");
        cs.setDescription("Systematized Nomenclature of Medicine Clinical Terms");
        return FhirResourceService.FHIR_CTX.newJsonParser().setPrettyPrint(true).encodeResourceToString(cs);
    }

    private String buildLoincStub() {
        CodeSystem cs = new CodeSystem();
        cs.setId("loinc");
        cs.setUrl(URL_LOINC);
        cs.setName("LOINC");
        cs.setTitle("Logical Observation Identifiers, Names and Codes");
        cs.setStatus(Enumerations.PublicationStatus.ACTIVE);
        cs.setContent(CodeSystem.CodeSystemContentMode.NOTPRESENT);
        cs.setPublisher("Regenstrief Institute, Inc.");
        return FhirResourceService.FHIR_CTX.newJsonParser().setPrettyPrint(true).encodeResourceToString(cs);
    }

    private String buildKcd9Stub() {
        CodeSystem cs = new CodeSystem();
        cs.setId("kcd9");
        cs.setUrl(URL_KCD9);
        cs.setName("KCD9");
        cs.setTitle("한국표준질병사인분류 (KCD-9)");
        cs.setStatus(Enumerations.PublicationStatus.ACTIVE);
        cs.setContent(CodeSystem.CodeSystemContentMode.NOTPRESENT);
        cs.setPublisher("통계청");
        return FhirResourceService.FHIR_CTX.newJsonParser().setPrettyPrint(true).encodeResourceToString(cs);
    }
}
