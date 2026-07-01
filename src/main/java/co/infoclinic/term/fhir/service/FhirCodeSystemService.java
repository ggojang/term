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
    public static final String URL_SNOMED           = "http://snomed.info/sct";
    public static final String URL_LOINC            = "http://loinc.org";
    public static final String URL_LOINC_LP         = "http://loinc.org/lpf";
    public static final String URL_LOINC_LG         = "http://loinc.org/lg";
    public static final String URL_KCD9             = "http://www.hl7korea.or.kr/CodeSystem/kostat-kcd-9";
    public static final String URL_KCD8             = "http://www.hl7korea.or.kr/CodeSystem/kostat-kcd-8";
    public static final String URL_ATC               = "http://www.whocc.no/atc";
    public static final String URL_KPIS_KDCODE       = "http://www.hl7korea.or.kr/CodeSystem/kpis-kdcode";
    public static final String URL_HIRA_PROCEDURE   = "http://www.hl7korea.or.kr/CodeSystem/hira-edi-procedure";
    public static final String URL_HIRA_MEDICATION  = "http://www.hl7korea.or.kr/CodeSystem/hira-edi-medication";
    public static final String URL_HIRA_MATERIAL    = "http://www.hl7korea.or.kr/CodeSystem/hira-edi-material";

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
        if ("snomed".equals(id))      return Optional.of(buildSnomedStub());
        if ("loinc".equals(id))       return Optional.of(buildLoincStub());
        if ("loinc-lp".equals(id))    return Optional.of(buildLoincLpStub());
        if ("loinc-lg".equals(id))    return Optional.of(buildLoincLgStub());
        if ("kcd9".equals(id))        return Optional.of(buildKcd9Stub());
        if ("kcd8".equals(id))        return Optional.of(buildKcd8Stub());
        if ("atc".equals(id))         return Optional.of(buildAtcStub());
        if ("kpis-kdcode".equals(id)) return Optional.of(buildKpisKdcodeStub());
        return resourceSvc.findById("CodeSystem", id);
    }

    public Optional<String> findByUrl(String url) {
        if (URL_SNOMED.equals(url))      return Optional.of(buildSnomedStub());
        if (URL_LOINC.equals(url))       return Optional.of(buildLoincStub());
        if (URL_LOINC_LP.equals(url))    return Optional.of(buildLoincLpStub());
        if (URL_LOINC_LG.equals(url))    return Optional.of(buildLoincLgStub());
        if (URL_KCD9.equals(url))        return Optional.of(buildKcd9Stub());
        if (URL_KCD8.equals(url))        return Optional.of(buildKcd8Stub());
        if (URL_ATC.equals(url))         return Optional.of(buildAtcStub());
        if (URL_KPIS_KDCODE.equals(url)) return Optional.of(buildKpisKdcodeStub());
        return resourceSvc.findByUrl("CodeSystem", url);
    }

    public List<FhirResource> search(String name, String url) {
        if (url != null && !url.isEmpty()) {
            // stub 포함하여 조회 (findByUrl은 stub을 우선 처리)
            return findByUrl(url)
                    .map(json -> {
                        FhirResource r = new FhirResource();
                        r.setResourceType("CodeSystem"); r.setUrl(url); r.setContent(json);
                        return Collections.<FhirResource>singletonList(r);
                    }).orElse(Collections.emptyList());
        }
        // 전체 목록: DB 결과 + well-known stub 추가
        List<FhirResource> results = new ArrayList<>(resourceSvc.search("CodeSystem", name));
        String nameLower = (name != null) ? name.toLowerCase() : "";
        for (String[] stub : WELL_KNOWN_STUBS) {
            String stubId = stub[0], stubUrl = stub[1];
            // name 검색어가 있으면 stub id/url에 포함되는 경우만 추가
            if (!nameLower.isEmpty() && !stubId.contains(nameLower) && !stubUrl.toLowerCase().contains(nameLower)) {
                continue;
            }
            boolean already = results.stream().anyMatch(r -> stubUrl.equals(r.getUrl()) || stubId.equals(r.getId()));
            if (!already) {
                FhirResource r = new FhirResource();
                r.setId(stubId); r.setResourceType("CodeSystem"); r.setUrl(stubUrl);
                r.setContent(findById(stubId).orElse(null));
                results.add(0, r);
            }
        }
        return results;
    }

    private static final String[][] WELL_KNOWN_STUBS = {
        {"snomed",      URL_SNOMED},
        {"loinc",       URL_LOINC},
        {"loinc-lp",    URL_LOINC_LP},
        {"loinc-lg",    URL_LOINC_LG},
        {"kcd9",        URL_KCD9},
        {"kcd8",        URL_KCD8},
        {"atc",         URL_ATC},
        {"kpis-kdcode", URL_KPIS_KDCODE},
    };

    public List<FhirResource> searchByIg(String igId, String name) {
        return resourceSvc.searchByIg("CodeSystem", igId, name);
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
            addLoincDesignations(code, out);
            return out;
        }

        if (URL_LOINC_LP.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT part_name, part_display_name, part_type_name FROM loinc.lp WHERE part_number = :code LIMIT 1");
            q.setParameter("code", code);
            List<Object[]> rows = q.getResultList();
            if (rows.isEmpty()) return outcomeNotFound(out, system, code);
            Object[] row = rows.get(0);
            String display = row[1] != null ? (String) row[1] : (String) row[0];
            out.addParameter().setName("name").setValue(new StringType("LOINC Parts"));
            out.addParameter().setName("display").setValue(new StringType(display != null ? display : ""));
            out.addParameter().setName("system").setValue(new UriType(system));
            out.addParameter().setName("code").setValue(new CodeType(code));
            if (row[2] != null) {
                Parameters.ParametersParameterComponent prop = out.addParameter().setName("property");
                prop.addPart().setName("code").setValue(new CodeType("partType"));
                prop.addPart().setName("value").setValue(new StringType((String) row[2]));
            }
            return out;
        }

        if (URL_LOINC_LG.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT lg, status FROM loinc.lg WHERE lg_id = :code LIMIT 1");
            q.setParameter("code", code);
            List<Object[]> rows = q.getResultList();
            if (rows.isEmpty()) return outcomeNotFound(out, system, code);
            Object[] row = rows.get(0);
            out.addParameter().setName("name").setValue(new StringType("LOINC Groups"));
            out.addParameter().setName("display").setValue(new StringType(row[0] != null ? (String) row[0] : ""));
            out.addParameter().setName("system").setValue(new UriType(system));
            out.addParameter().setName("code").setValue(new CodeType(code));
            return out;
        }

        if (URL_KCD9.equals(system) || URL_KCD8.equals(system)) {
            String name = URL_KCD9.equals(system) ? "KCD-9" : "KCD-8";
            Icd10Class icd10 = icd10Repo.findByCode(code);
            if (icd10 == null) return outcomeNotFound(out, system, code);
            String display = icd10.getKoreanLabel() != null ? icd10.getKoreanLabel() : icd10.getLabel();
            out.addParameter().setName("name").setValue(new StringType(name));
            out.addParameter().setName("display").setValue(new StringType(display != null ? display : ""));
            out.addParameter().setName("system").setValue(new UriType(system));
            out.addParameter().setName("code").setValue(new CodeType(code));
            return out;
        }

        if (URL_KPIS_KDCODE.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT 표준코드명칭, 급여비급여구분, 상한가, 적용개시일자 FROM term.kdcode WHERE 표준코드 = :code ORDER BY 적용개시일자 DESC LIMIT 1");
            q.setParameter("code", code);
            List<Object[]> rows = q.getResultList();
            if (rows.isEmpty()) return outcomeNotFound(out, system, code);
            Object[] row = rows.get(0);
            String display = row[0] != null ? (String) row[0] : "";
            out.addParameter().setName("name").setValue(new StringType("KPIS KD코드 (표준코드목록)"));
            out.addParameter().setName("display").setValue(new StringType(display));
            out.addParameter().setName("system").setValue(new UriType(system));
            out.addParameter().setName("code").setValue(new CodeType(code));
            if (row[1] != null) {
                Parameters.ParametersParameterComponent des = out.addParameter().setName("designation");
                des.addPart().setName("language").setValue(new CodeType("ko"));
                des.addPart().setName("value").setValue(new StringType(
                    display + " [급여구분:" + row[1] + (row[2] != null ? ", 상한가:" + row[2] : "") + "]"));
            }
            return out;
        }

        if (URL_ATC.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT atc_name, atc_hname FROM term.hira_atc_master WHERE atc_code = :code LIMIT 1");
            q.setParameter("code", code);
            List<Object[]> rows = q.getResultList();
            if (rows.isEmpty()) return outcomeNotFound(out, system, code);
            Object[] row = rows.get(0);
            String display = row[0] != null ? (String) row[0] : "";
            out.addParameter().setName("name").setValue(new StringType("ATC"));
            out.addParameter().setName("display").setValue(new StringType(display));
            out.addParameter().setName("system").setValue(new UriType(system));
            out.addParameter().setName("code").setValue(new CodeType(code));
            if (row[1] != null && !((String) row[1]).isEmpty()) {
                Parameters.ParametersParameterComponent des = out.addParameter().setName("designation");
                des.addPart().setName("language").setValue(new CodeType("ko"));
                des.addPart().setName("value").setValue(new StringType((String) row[1]));
            }
            return out;
        }

        if (URL_HIRA_PROCEDURE.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT 한글명, 영문명 FROM term.hira_행위_code WHERE 수가코드 = :code LIMIT 1");
            q.setParameter("code", code);
            List<Object[]> rows = q.getResultList();
            if (rows.isEmpty()) return outcomeNotFound(out, system, code);
            Object[] row = rows.get(0);
            String display = row[0] != null ? (String) row[0] : "";
            out.addParameter().setName("name").setValue(new StringType("HIRA EDI Procedure"));
            out.addParameter().setName("display").setValue(new StringType(display));
            out.addParameter().setName("system").setValue(new UriType(system));
            out.addParameter().setName("code").setValue(new CodeType(code));
            if (row[1] != null && !((String) row[1]).isEmpty()) {
                Parameters.ParametersParameterComponent des = out.addParameter().setName("designation");
                des.addPart().setName("language").setValue(new CodeType("en"));
                des.addPart().setName("value").setValue(new StringType((String) row[1]));
            }
            return out;
        }

        if (URL_HIRA_MEDICATION.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT 제품명 FROM term.hira_약제_code WHERE 제품코드 = :code LIMIT 1");
            q.setParameter("code", code);
            List<String> rows = q.getResultList();
            if (rows.isEmpty()) return outcomeNotFound(out, system, code);
            out.addParameter().setName("name").setValue(new StringType("HIRA EDI Medication"));
            out.addParameter().setName("display").setValue(new StringType(rows.get(0) != null ? rows.get(0) : ""));
            out.addParameter().setName("system").setValue(new UriType(system));
            out.addParameter().setName("code").setValue(new CodeType(code));
            return out;
        }

        if (URL_HIRA_MATERIAL.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT 품명 FROM term.hira_치료재료_code WHERE 코드 = :code LIMIT 1");
            q.setParameter("code", code);
            List<String> rows = q.getResultList();
            if (rows.isEmpty()) return outcomeNotFound(out, system, code);
            out.addParameter().setName("name").setValue(new StringType("HIRA EDI Material"));
            out.addParameter().setName("display").setValue(new StringType(rows.get(0) != null ? rows.get(0) : ""));
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
        } else if (URL_LOINC_LP.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT COALESCE(part_display_name, part_name) FROM loinc.lp WHERE part_number = :code LIMIT 1");
            q.setParameter("code", code);
            List<String> rows = q.getResultList();
            valid = !rows.isEmpty();
            if (valid) actualDisplay = rows.get(0);
        } else if (URL_LOINC_LG.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT lg FROM loinc.lg WHERE lg_id = :code LIMIT 1");
            q.setParameter("code", code);
            List<String> rows = q.getResultList();
            valid = !rows.isEmpty();
            if (valid) actualDisplay = rows.get(0);
        } else if (URL_KCD9.equals(system) || URL_KCD8.equals(system)) {
            Icd10Class icd10 = icd10Repo.findByCode(code);
            valid = (icd10 != null);
            if (valid) actualDisplay = icd10.getKoreanLabel() != null ? icd10.getKoreanLabel() : icd10.getLabel();
        } else if (URL_KPIS_KDCODE.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT 표준코드명칭 FROM term.kdcode WHERE 표준코드 = :code ORDER BY 적용개시일자 DESC LIMIT 1");
            q.setParameter("code", code);
            List<String> rows = q.getResultList();
            valid = !rows.isEmpty();
            if (valid) actualDisplay = rows.get(0);
        } else if (URL_ATC.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT atc_name FROM term.hira_atc_master WHERE atc_code = :code LIMIT 1");
            q.setParameter("code", code);
            List<String> rows = q.getResultList();
            valid = !rows.isEmpty();
            if (valid) actualDisplay = rows.get(0);
        } else if (URL_HIRA_PROCEDURE.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT 한글명 FROM term.hira_행위_code WHERE 수가코드 = :code LIMIT 1");
            q.setParameter("code", code);
            List<String> rows = q.getResultList();
            valid = !rows.isEmpty();
            if (valid) actualDisplay = rows.get(0);
        } else if (URL_HIRA_MEDICATION.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT 제품명 FROM term.hira_약제_code WHERE 제품코드 = :code LIMIT 1");
            q.setParameter("code", code);
            List<String> rows = q.getResultList();
            valid = !rows.isEmpty();
            if (valid) actualDisplay = rows.get(0);
        } else if (URL_HIRA_MATERIAL.equals(system)) {
            Query q = em.createNativeQuery(
                "SELECT 품명 FROM term.hira_치료재료_code WHERE 코드 = :code LIMIT 1");
            q.setParameter("code", code);
            List<String> rows = q.getResultList();
            valid = !rows.isEmpty();
            if (valid) actualDisplay = rows.get(0);
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

    private String buildLoincLpStub() {
        CodeSystem cs = new CodeSystem();
        cs.setId("loinc-lp");
        cs.setUrl(URL_LOINC_LP);
        cs.setName("LOINCParts");
        cs.setTitle("LOINC Parts (LP)");
        cs.setStatus(Enumerations.PublicationStatus.ACTIVE);
        cs.setContent(CodeSystem.CodeSystemContentMode.NOTPRESENT);
        cs.setPublisher("Regenstrief Institute, Inc.");
        cs.setDescription("LOINC Part codes (LP) representing components, properties, time aspects, systems, scale types, and method types.");
        return FhirResourceService.FHIR_CTX.newJsonParser().setPrettyPrint(true).encodeResourceToString(cs);
    }

    private String buildLoincLgStub() {
        CodeSystem cs = new CodeSystem();
        cs.setId("loinc-lg");
        cs.setUrl(URL_LOINC_LG);
        cs.setName("LOINCGroups");
        cs.setTitle("LOINC Groups (LG)");
        cs.setStatus(Enumerations.PublicationStatus.ACTIVE);
        cs.setContent(CodeSystem.CodeSystemContentMode.NOTPRESENT);
        cs.setPublisher("Regenstrief Institute, Inc.");
        cs.setDescription("LOINC Group codes (LG) representing clinically meaningful groupings of LOINC terms.");
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

    private String buildKpisKdcodeStub() {
        CodeSystem cs = new CodeSystem();
        cs.setId("kpis-kdcode");
        cs.setUrl(URL_KPIS_KDCODE);
        cs.setName("KPIS_KDCode");
        cs.setTitle("KPIS KD코드 (약제코드)");
        cs.setStatus(Enumerations.PublicationStatus.ACTIVE);
        cs.setContent(CodeSystem.CodeSystemContentMode.NOTPRESENT);
        cs.setPublisher("HIRA");
        return FhirResourceService.FHIR_CTX.newJsonParser().setPrettyPrint(true).encodeResourceToString(cs);
    }

    private String buildAtcStub() {
        CodeSystem cs = new CodeSystem();
        cs.setId("atc");
        cs.setUrl(URL_ATC);
        cs.setName("ATC");
        cs.setTitle("Anatomical Therapeutic Chemical Classification System");
        cs.setStatus(Enumerations.PublicationStatus.ACTIVE);
        cs.setContent(CodeSystem.CodeSystemContentMode.NOTPRESENT);
        cs.setPublisher("WHO Collaborating Centre for Drug Statistics Methodology");
        return FhirResourceService.FHIR_CTX.newJsonParser().setPrettyPrint(true).encodeResourceToString(cs);
    }

    private String buildKcd8Stub() {
        CodeSystem cs = new CodeSystem();
        cs.setId("kcd8");
        cs.setUrl(URL_KCD8);
        cs.setName("KCD8");
        cs.setTitle("한국표준질병사인분류 (KCD-8)");
        cs.setStatus(Enumerations.PublicationStatus.ACTIVE);
        cs.setContent(CodeSystem.CodeSystemContentMode.NOTPRESENT);
        cs.setPublisher("통계청");
        return FhirResourceService.FHIR_CTX.newJsonParser().setPrettyPrint(true).encodeResourceToString(cs);
    }
}
