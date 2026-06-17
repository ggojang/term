package co.infoclinic.term.fhir.service;

import ca.uhn.fhir.parser.IParser;
import co.infoclinic.term.fhir.model.entity.FhirResource;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class FhirValueSetService {

    private static final Logger log = LoggerFactory.getLogger(FhirValueSetService.class);

    @Autowired
    private FhirResourceService resourceSvc;

    @Autowired
    private LinguisticVariantRepository lvRepo;

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    public String save(String json) {
        return resourceSvc.save("ValueSet", json);
    }

    public Optional<String> findById(String id) {
        return resourceSvc.findById("ValueSet", id);
    }

    public Optional<String> findByUrl(String url) {
        return resourceSvc.findByUrl("ValueSet", url);
    }

    public List<FhirResource> search(String name, String url) {
        if (url != null && !url.isEmpty()) {
            return resourceSvc.findByUrl("ValueSet", url)
                    .map(json -> { FhirResource r = new FhirResource(); r.setContent(json); return Collections.singletonList(r); })
                    .orElse(Collections.<FhirResource>emptyList());
        }
        return resourceSvc.search("ValueSet", name);
    }

    public void delete(String id) {
        resourceSvc.delete("ValueSet", id);
    }

    /**
     * $expand: ValueSet을 펼쳐서 포함되는 코드 목록 반환
     */
    @SuppressWarnings("unchecked")
    public ValueSet expand(String idOrUrl, String filter, Integer offset, Integer count) {
        String json = null;
        if (idOrUrl != null) {
            json = resourceSvc.findById("ValueSet", idOrUrl)
                    .orElseGet(() -> resourceSvc.findByUrl("ValueSet", idOrUrl).orElse(null));
        }
        if (json == null) return buildOutcomeValueSet("ValueSet not found: " + idOrUrl);

        IParser parser = FhirResourceService.FHIR_CTX.newJsonParser();
        ValueSet vs = (ValueSet) parser.parseResource(json);
        ValueSet.ValueSetExpansionComponent expansion = new ValueSet.ValueSetExpansionComponent();
        expansion.setIdentifier(vs.getUrl());
        expansion.setTimestamp(new java.util.Date());

        if (vs.hasCompose()) {
            for (ValueSet.ConceptSetComponent include : vs.getCompose().getInclude()) {
                String system = include.getSystem();
                expandInclude(system, include, filter, offset, count, expansion);
            }
        }

        vs.setExpansion(expansion);
        return vs;
    }

    @SuppressWarnings("unchecked")
    private void expandInclude(String system, ValueSet.ConceptSetComponent include,
                               String filter, Integer offset, Integer count,
                               ValueSet.ValueSetExpansionComponent expansion) {

        // 직접 열거된 코드
        if (include.hasConcept()) {
            for (ValueSet.ConceptReferenceComponent c : include.getConcept()) {
                String code = c.getCode();
                String display = c.getDisplay();

                // LOINC display 누락 시 DB에서 영문 조회
                if ((display == null || display.isEmpty())
                        && FhirCodeSystemService.URL_LOINC.equals(system)) {
                    display = lookupLoincDisplay(code);
                }

                boolean matchesFilter = filter == null
                        || code.contains(filter)
                        || (display != null && display.toLowerCase().contains(filter.toLowerCase()))
                        || c.getDesignation().stream().anyMatch(d ->
                                d.getValue() != null && d.getValue().toLowerCase().contains(filter.toLowerCase()));

                if (matchesFilter) {
                    ValueSet.ValueSetExpansionContainsComponent contains = expansion.addContains()
                            .setSystem(system)
                            .setCode(code)
                            .setDisplay(display);
                    // ValueSet 정의에 있는 designation 먼저
                    for (ValueSet.ConceptReferenceDesignationComponent des : c.getDesignation()) {
                        ValueSet.ConceptReferenceDesignationComponent d = contains.addDesignation();
                        if (des.hasLanguage()) d.setLanguage(des.getLanguage());
                        if (des.hasUse())      d.setUse(des.getUse());
                        if (des.hasValue())    d.setValue(des.getValue());
                    }
                    // LOINC: Linguistic Variant 다국어 designation 추가
                    if (FhirCodeSystemService.URL_LOINC.equals(system)) {
                        addLoincKoreanDesignation(code, contains);
                    }
                }
            }
            return;
        }

        // SNOMED CT 계층 포함
        if (FhirCodeSystemService.URL_SNOMED.equals(system) && include.hasFilter()) {
            for (ValueSet.ConceptSetFilterComponent f : include.getFilter()) {
                if ("is-a".equals(f.getProperty())) {
                    expandSnomedHierarchy(f.getValue(), filter, offset, count, expansion, system);
                }
            }
            return;
        }

        // LOINC ValueSet (class 기반)
        if (FhirCodeSystemService.URL_LOINC.equals(system) && include.hasFilter()) {
            for (ValueSet.ConceptSetFilterComponent f : include.getFilter()) {
                if ("class".equals(f.getProperty())) {
                    expandLoincByClass(f.getValue(), filter, offset, count, expansion, system);
                }
            }
            return;
        }

        // 저장된 CodeSystem에서 전체 코드 가져오기
        expandFromStoredCodeSystem(system, filter, expansion);
    }

    @SuppressWarnings("unchecked")
    private void expandSnomedHierarchy(String rootCode, String filter, Integer offset, Integer count,
                                       ValueSet.ValueSetExpansionComponent expansion, String system) {
        String sql = "SELECT c.id, d.term FROM term.concept c " +
                "JOIN term.description d ON d.concept_id = c.id " +
                "AND d.type_id = '900000000000003001' AND d.active = true " +
                "WHERE c.id = :root OR EXISTS (" +
                "  SELECT 1 FROM term.transitive_closure tc " +
                "  WHERE tc.sub_type_id = c.id AND tc.super_type_id = :root AND tc.active = true" +
                ") AND c.active = true";
        if (filter != null) sql += " AND d.term ILIKE :filter";
        sql += " ORDER BY d.term";
        if (count != null) sql += " LIMIT " + count;
        if (offset != null) sql += " OFFSET " + offset;

        Query q = em.createNativeQuery(sql);
        q.setParameter("root", rootCode);
        if (filter != null) q.setParameter("filter", "%" + filter + "%");

        List<Object[]> rows = q.getResultList();
        expansion.setTotal(rows.size());
        for (Object[] row : rows) {
            expansion.addContains()
                    .setSystem(system)
                    .setCode(String.valueOf(row[0]))
                    .setDisplay((String) row[1]);
        }
    }

    @SuppressWarnings("unchecked")
    private void expandLoincByClass(String className, String filter, Integer offset, Integer count,
                                    ValueSet.ValueSetExpansionComponent expansion, String system) {
        String sql = "SELECT loinc_num, long_common_name FROM loinc.loinc WHERE class = :cls";
        if (filter != null) sql += " AND (loinc_num ILIKE :filter OR long_common_name ILIKE :filter)";
        sql += " ORDER BY loinc_num";
        if (count != null) sql += " LIMIT " + count;
        if (offset != null) sql += " OFFSET " + offset;

        Query q = em.createNativeQuery(sql);
        q.setParameter("cls", className);
        if (filter != null) q.setParameter("filter", "%" + filter + "%");

        List<Object[]> rows = q.getResultList();
        expansion.setTotal(rows.size());
        for (Object[] row : rows) {
            String loincCode = (String) row[0];
            ValueSet.ValueSetExpansionContainsComponent contains = expansion.addContains()
                    .setSystem(system)
                    .setCode(loincCode)
                    .setDisplay((String) row[1]);
            addLoincKoreanDesignation(loincCode, contains);
        }
    }

    private void expandFromStoredCodeSystem(String system, String filter,
                                            ValueSet.ValueSetExpansionComponent expansion) {
        Optional<String> csJson = resourceSvc.findByUrl("CodeSystem", system);
        if (!csJson.isPresent()) return;

        IParser parser = FhirResourceService.FHIR_CTX.newJsonParser();
        CodeSystem cs = (CodeSystem) parser.parseResource(csJson.get());
        addConcepts(cs.getConcept(), system, filter, expansion);
    }

    private void addConcepts(List<CodeSystem.ConceptDefinitionComponent> concepts,
                             String system, String filter,
                             ValueSet.ValueSetExpansionComponent expansion) {
        for (CodeSystem.ConceptDefinitionComponent c : concepts) {
            boolean matchesFilter = filter == null
                    || c.getCode().contains(filter)
                    || (c.getDisplay() != null && c.getDisplay().toLowerCase().contains(filter.toLowerCase()))
                    || c.getDesignation().stream().anyMatch(d ->
                            d.getValue() != null && d.getValue().toLowerCase().contains(filter.toLowerCase()));

            if (matchesFilter) {
                ValueSet.ValueSetExpansionContainsComponent contains = expansion.addContains()
                        .setSystem(system)
                        .setCode(c.getCode())
                        .setDisplay(c.getDisplay());

                for (CodeSystem.ConceptDefinitionDesignationComponent des : c.getDesignation()) {
                    ValueSet.ConceptReferenceDesignationComponent d = contains.addDesignation();
                    if (des.hasLanguage()) d.setLanguage(des.getLanguage());
                    if (des.hasUse())     d.setUse(des.getUse());
                    if (des.hasValue())   d.setValue(des.getValue());
                }
            }
            if (c.hasConcept()) addConcepts(c.getConcept(), system, filter, expansion);
        }
    }

    @SuppressWarnings("unchecked")
    private String lookupLoincDisplay(String code) {
        try {
            List<Object[]> rows = em.createNativeQuery(
                "SELECT long_common_name, component FROM loinc.loinc WHERE code = :code LIMIT 1")
                .setParameter("code", code)
                .getResultList();
            if (rows.isEmpty()) return null;
            Object[] row = rows.get(0);
            String lcn = row[0] != null ? (String) row[0] : null;
            String comp = row[1] != null ? (String) row[1] : null;
            return lcn != null ? lcn : comp;
        } catch (Exception e) {
            return null;
        }
    }

    private void addLoincKoreanDesignation(String code, ValueSet.ValueSetExpansionContainsComponent contains) {
        List<LinguisticVariant> variants = lvRepo.findListByCode(code);
        for (LinguisticVariant lv : variants) {
            String display = resolveLoincDisplay(lv);
            if (display == null || display.isEmpty()) continue;
            String lang = lv.getIsoLang() + (lv.getIsoCountry() != null ? "-" + lv.getIsoCountry() : "");
            contains.addDesignation().setLanguage(lang).setValue(display);
        }
    }

    private String resolveLoincDisplay(LinguisticVariant lv) {
        if (lv.getLongCommonName() != null && !lv.getLongCommonName().isEmpty())
            return lv.getLongCommonName();
        StringBuilder sb = new StringBuilder();
        if (lv.getComponent()  != null) sb.append(lv.getComponent());
        if (lv.getProperty()   != null && !lv.getProperty().isEmpty())   sb.append(" [").append(lv.getProperty()).append("]");
        if (lv.getTimeAspect() != null && !lv.getTimeAspect().isEmpty()) sb.append(":").append(lv.getTimeAspect());
        if (lv.getSystem()     != null && !lv.getSystem().isEmpty())     sb.append(":").append(lv.getSystem());
        if (lv.getScaleType()  != null && !lv.getScaleType().isEmpty())  sb.append(":").append(lv.getScaleType());
        if (lv.getMethodType() != null && !lv.getMethodType().isEmpty()) sb.append(":").append(lv.getMethodType());
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * ValueSet/$validate-code
     * url 또는 idOrUrl로 ValueSet을 찾아 code가 포함되어 있는지 확인
     */
    @Autowired
    private FhirCodeSystemService csSvc;

    public Parameters validateCode(String idOrUrl, String system, String code, String display) {
        Parameters out = new Parameters();

        if (code == null || code.isEmpty()) {
            out.addParameter().setName("result").setValue(new BooleanType(false));
            out.addParameter().setName("message").setValue(new StringType("code parameter is required"));
            return out;
        }

        // ValueSet URL/ID 없이 system만 있으면 CodeSystem validate-code로 위임
        if (idOrUrl == null) {
            if (system != null) {
                return csSvc.validateCode(system, code, display);
            }
            out.addParameter().setName("result").setValue(new BooleanType(false));
            out.addParameter().setName("message").setValue(new StringType("url or system parameter is required"));
            return out;
        }

        // ValueSet 조회
        String json = resourceSvc.findById("ValueSet", idOrUrl)
                .orElseGet(() -> resourceSvc.findByUrl("ValueSet", idOrUrl).orElse(null));
        if (json == null) {
            out.addParameter().setName("result").setValue(new BooleanType(false));
            out.addParameter().setName("message").setValue(new StringType("ValueSet not found: " + idOrUrl));
            return out;
        }

        // $expand 후 코드 포함 여부 확인
        ValueSet expanded = expand(idOrUrl, null, null, null);
        if (expanded.hasExpansion()) {
            for (ValueSet.ValueSetExpansionContainsComponent c : expanded.getExpansion().getContains()) {
                if (code.equals(c.getCode()) && (system == null || system.equals(c.getSystem()))) {
                    out.addParameter().setName("result").setValue(new BooleanType(true));
                    if (c.getDisplay() != null) {
                        out.addParameter().setName("display").setValue(new StringType(c.getDisplay()));
                    }
                    return out;
                }
            }
        }

        out.addParameter().setName("result").setValue(new BooleanType(false));
        out.addParameter().setName("message").setValue(
                new StringType("Code " + code + " not found in ValueSet " + idOrUrl));
        return out;
    }

    private ValueSet buildOutcomeValueSet(String message) {
        ValueSet vs = new ValueSet();
        vs.setStatus(Enumerations.PublicationStatus.UNKNOWN);
        vs.setDescription(message);
        return vs;
    }
}
