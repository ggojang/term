package co.infoclinic.term.fhir.service;

import ca.uhn.fhir.parser.IParser;
import co.infoclinic.term.fhir.model.entity.FhirResource;
import co.infoclinic.term.loinc.model.entity.LinguisticVariant;
import co.infoclinic.term.loinc.repository.LinguisticVariantRepository;
import co.infoclinic.term.snomedct.controller.ConstraintController;
import co.infoclinic.term.snomedct.model.dto.ConceptViewDTO;
import co.infoclinic.term.snomedct.service.SchemeService;
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
import java.util.stream.Collectors;

@Service
public class FhirValueSetService {

    private static final Logger log = LoggerFactory.getLogger(FhirValueSetService.class);

    @Autowired
    private FhirResourceService resourceSvc;

    @Autowired
    private LinguisticVariantRepository lvRepo;

    @Autowired
    private ConstraintController constraintCtrl;

    @Autowired
    private SchemeService schemeSvc;

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

    public List<FhirResource> searchByIg(String igId, String name) {
        return resourceSvc.searchByIg("ValueSet", igId, name);
    }

    public void delete(String id) {
        resourceSvc.delete("ValueSet", id);
    }

    /**
     * $expand: ValueSet을 펼쳐서 포함되는 코드 목록 반환
     */
    @SuppressWarnings("unchecked")
    public ValueSet expand(String idOrUrl, String filter, Integer offset, Integer count) {
        // SNOMED CT implicit ValueSet (ECL / refset)
        if (idOrUrl != null && idOrUrl.startsWith(FhirCodeSystemService.URL_SNOMED)) {
            return expandSnomedImplicit(idOrUrl, filter, offset, count);
        }

        // LOINC implicit ValueSet
        if (idOrUrl != null && idOrUrl.startsWith(FhirCodeSystemService.URL_LOINC + "/vs")) {
            return expandLoincImplicit(idOrUrl, filter, offset, count);
        }

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
                // include.valueSet 참조 처리 (다른 ValueSet 포함)
                if (!include.hasSystem() && include.hasValueSet()) {
                    for (CanonicalType refUrl : include.getValueSet()) {
                        expandReferencedValueSet(refUrl.getValue(), filter, offset, count, expansion);
                    }
                    continue;
                }
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

        // SNOMED CT 전체 조회 (concept/filter 없이 system=http://snomed.info/sct 만 선언된 경우)
        if (FhirCodeSystemService.URL_SNOMED.equals(system)) {
            expandSnomedAll(filter, offset, count, expansion, system);
            return;
        }

        // LOINC 전체 조회 (concept/filter 없이 system=http://loinc.org 만 선언된 경우)
        if (FhirCodeSystemService.URL_LOINC.equals(system)) {
            expandLoincAll(filter, offset, count, expansion, system);
            return;
        }

        // KCD-8/9 전체 조회
        if (FhirCodeSystemService.URL_KCD9.equals(system) || FhirCodeSystemService.URL_KCD8.equals(system)) {
            expandKcdAll(filter, offset, count, expansion, system);
            return;
        }

        // ATC 전체 조회
        if (FhirCodeSystemService.URL_ATC.equals(system)) {
            expandAtcAll(filter, offset, count, expansion, system);
            return;
        }

        // KPIS KD코드 전체 조회
        if (FhirCodeSystemService.URL_KPIS_KDCODE.equals(system)) {
            expandKdcodeAll(filter, offset, count, expansion, system);
            return;
        }

        // HIRA EDI Procedure 전체 조회
        if (FhirCodeSystemService.URL_HIRA_PROCEDURE.equals(system)) {
            expandHiraEdiProcedureAll(filter, offset, count, expansion, system);
            return;
        }

        // HIRA EDI Medication 전체 조회
        if (FhirCodeSystemService.URL_HIRA_MEDICATION.equals(system)) {
            expandHiraEdiMedicationAll(filter, offset, count, expansion, system);
            return;
        }

        // HIRA EDI Material 전체 조회
        if (FhirCodeSystemService.URL_HIRA_MATERIAL.equals(system)) {
            expandHiraEdiMaterialAll(filter, offset, count, expansion, system);
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
    private void expandSnomedAll(String filter, Integer offset, Integer count,
                                 ValueSet.ValueSetExpansionComponent expansion, String system) {
        int from  = offset != null ? offset : 0;
        int limit = count  != null ? count  : 100;

        String where = filter != null
                ? " WHERE d.term ILIKE :f OR c.concept_id ILIKE :f" : "";
        String sql = "SELECT c.concept_id, d.term FROM term.concept c" +
                " JOIN term.description d ON d.concept_id = c.concept_id" +
                " AND d.type_id = '900000000000003001' AND d.active = 1" +
                " WHERE c.active = 1" +
                (filter != null ? " AND (d.term ILIKE :f OR c.concept_id ILIKE :f)" : "") +
                " ORDER BY c.concept_id LIMIT " + limit + " OFFSET " + from;
        Query q = em.createNativeQuery(sql);
        if (filter != null) q.setParameter("f", "%" + filter + "%");

        String cntSql = "SELECT COUNT(*) FROM term.concept c" +
                " JOIN term.description d ON d.concept_id = c.concept_id" +
                " AND d.type_id = '900000000000003001' AND d.active = 1" +
                " WHERE c.active = 1" +
                (filter != null ? " AND (d.term ILIKE :f OR c.concept_id ILIKE :f)" : "");
        Query cq = em.createNativeQuery(cntSql);
        if (filter != null) cq.setParameter("f", "%" + filter + "%");
        expansion.setTotal(((Number) cq.getSingleResult()).intValue());
        if (offset != null) expansion.setOffset(offset);

        for (Object[] r : (List<Object[]>) q.getResultList()) {
            expansion.addContains()
                    .setSystem(system)
                    .setCode(String.valueOf(r[0]))
                    .setDisplay(r[1] != null ? (String) r[1] : "");
        }
    }

    @SuppressWarnings("unchecked")
    private void expandLoincAll(String filter, Integer offset, Integer count,
                                ValueSet.ValueSetExpansionComponent expansion, String system) {
        int from  = offset != null ? offset : 0;
        int limit = count  != null ? count  : 100;

        String where = filter != null ? " WHERE code ILIKE :f OR long_common_name ILIKE :f" : "";
        String sql = "SELECT code, long_common_name FROM loinc.loinc" + where
                + " ORDER BY code LIMIT " + limit + " OFFSET " + from;
        Query q = em.createNativeQuery(sql);
        if (filter != null) q.setParameter("f", "%" + filter + "%");

        String cntSql = "SELECT COUNT(*) FROM loinc.loinc" + where;
        Query cq = em.createNativeQuery(cntSql);
        if (filter != null) cq.setParameter("f", "%" + filter + "%");
        expansion.setTotal(((Number) cq.getSingleResult()).intValue());
        if (offset != null) expansion.setOffset(offset);

        for (Object[] r : (List<Object[]>) q.getResultList()) {
            String loincCode = (String) r[0];
            ValueSet.ValueSetExpansionContainsComponent contains = expansion.addContains()
                    .setSystem(system).setCode(loincCode)
                    .setDisplay(r[1] != null ? (String) r[1] : "");
            addLoincKoreanDesignation(loincCode, contains);
        }
    }

    @SuppressWarnings("unchecked")
    private void expandKcdAll(String filter, Integer offset, Integer count,
                              ValueSet.ValueSetExpansionComponent expansion, String system) {
        int from  = offset != null ? offset : 0;
        int limit = count  != null ? count  : 100;

        String where = filter != null
                ? " WHERE code ILIKE :f OR korean_label ILIKE :f OR label ILIKE :f" : "";
        String sql = "SELECT code, korean_label, label FROM icd10.icd10_class" + where
                + " ORDER BY code LIMIT " + limit + " OFFSET " + from;
        Query q = em.createNativeQuery(sql);
        if (filter != null) q.setParameter("f", "%" + filter + "%");

        String cntSql = "SELECT COUNT(*) FROM icd10.icd10_class" + where;
        Query cq = em.createNativeQuery(cntSql);
        if (filter != null) cq.setParameter("f", "%" + filter + "%");
        expansion.setTotal(((Number) cq.getSingleResult()).intValue());
        if (offset != null) expansion.setOffset(offset);

        for (Object[] r : (List<Object[]>) q.getResultList()) {
            String kcdCode = (String) r[0];
            String display = r[1] != null ? (String) r[1] : (r[2] != null ? (String) r[2] : "");
            expansion.addContains().setSystem(system).setCode(kcdCode).setDisplay(display);
        }
    }

    @SuppressWarnings("unchecked")
    private void expandAtcAll(String filter, Integer offset, Integer count,
                              ValueSet.ValueSetExpansionComponent expansion, String system) {
        int from  = offset != null ? offset : 0;
        int limit = count  != null ? count  : 100;
        String where = filter != null ? " WHERE atc_code ILIKE :f OR atc_name ILIKE :f OR atc_hname ILIKE :f" : "";
        String sql = "SELECT atc_code, atc_hname, atc_name FROM term.hira_atc_master" + where
                + " ORDER BY atc_code LIMIT " + limit + " OFFSET " + from;
        Query q = em.createNativeQuery(sql);
        if (filter != null) q.setParameter("f", "%" + filter + "%");
        String cntSql = "SELECT COUNT(*) FROM term.hira_atc_master" + where;
        Query cq = em.createNativeQuery(cntSql);
        if (filter != null) cq.setParameter("f", "%" + filter + "%");
        expansion.setTotal(((Number) cq.getSingleResult()).intValue());
        if (offset != null) expansion.setOffset(offset);
        for (Object[] r : (List<Object[]>) q.getResultList()) {
            String display = r[1] != null ? (String) r[1] : (r[2] != null ? (String) r[2] : "");
            expansion.addContains().setSystem(system).setCode((String) r[0]).setDisplay(display);
        }
    }

    @SuppressWarnings("unchecked")
    private void expandKdcodeAll(String filter, Integer offset, Integer count,
                                 ValueSet.ValueSetExpansionComponent expansion, String system) {
        int from  = offset != null ? offset : 0;
        int limit = count  != null ? count  : 100;
        String where = filter != null ? " WHERE 표준코드 ILIKE :f OR 표준코드명칭 ILIKE :f" : "";
        String sql = "SELECT DISTINCT ON (표준코드) 표준코드, 표준코드명칭 FROM term.kdcode" + where
                + " ORDER BY 표준코드, 적용개시일자 DESC LIMIT " + limit + " OFFSET " + from;
        Query q = em.createNativeQuery(sql);
        if (filter != null) q.setParameter("f", "%" + filter + "%");
        String cntSql = "SELECT COUNT(DISTINCT 표준코드) FROM term.kdcode" + where;
        Query cq = em.createNativeQuery(cntSql);
        if (filter != null) cq.setParameter("f", "%" + filter + "%");
        expansion.setTotal(((Number) cq.getSingleResult()).intValue());
        if (offset != null) expansion.setOffset(offset);
        for (Object[] r : (List<Object[]>) q.getResultList()) {
            expansion.addContains().setSystem(system).setCode((String) r[0])
                    .setDisplay(r[1] != null ? (String) r[1] : "");
        }
    }

    @SuppressWarnings("unchecked")
    private void expandHiraEdiProcedureAll(String filter, Integer offset, Integer count,
                                           ValueSet.ValueSetExpansionComponent expansion, String system) {
        int from  = offset != null ? offset : 0;
        int limit = count  != null ? count  : 100;
        String where = filter != null ? " WHERE 수가코드 ILIKE :f OR 한글명 ILIKE :f" : "";
        String sql = "SELECT 수가코드, 한글명 FROM term.hira_행위_code" + where
                + " ORDER BY 수가코드 LIMIT " + limit + " OFFSET " + from;
        Query q = em.createNativeQuery(sql);
        if (filter != null) q.setParameter("f", "%" + filter + "%");
        String cntSql = "SELECT COUNT(*) FROM term.hira_행위_code" + where;
        Query cq = em.createNativeQuery(cntSql);
        if (filter != null) cq.setParameter("f", "%" + filter + "%");
        expansion.setTotal(((Number) cq.getSingleResult()).intValue());
        if (offset != null) expansion.setOffset(offset);
        for (Object[] r : (List<Object[]>) q.getResultList()) {
            expansion.addContains().setSystem(system).setCode((String) r[0])
                    .setDisplay(r[1] != null ? (String) r[1] : "");
        }
    }

    @SuppressWarnings("unchecked")
    private void expandHiraEdiMedicationAll(String filter, Integer offset, Integer count,
                                            ValueSet.ValueSetExpansionComponent expansion, String system) {
        int from  = offset != null ? offset : 0;
        int limit = count  != null ? count  : 100;
        String where = filter != null ? " WHERE 표준코드 ILIKE :f OR 표준코드명칭 ILIKE :f" : "";
        String sql = "SELECT DISTINCT ON (표준코드) 표준코드, 표준코드명칭 FROM term.kdcode" + where
                + " ORDER BY 표준코드, 적용개시일자 DESC LIMIT " + limit + " OFFSET " + from;
        Query q = em.createNativeQuery(sql);
        if (filter != null) q.setParameter("f", "%" + filter + "%");
        String cntSql = "SELECT COUNT(DISTINCT 표준코드) FROM term.kdcode" + where;
        Query cq = em.createNativeQuery(cntSql);
        if (filter != null) cq.setParameter("f", "%" + filter + "%");
        expansion.setTotal(((Number) cq.getSingleResult()).intValue());
        if (offset != null) expansion.setOffset(offset);
        for (Object[] r : (List<Object[]>) q.getResultList()) {
            expansion.addContains().setSystem(system).setCode((String) r[0])
                    .setDisplay(r[1] != null ? (String) r[1] : "");
        }
    }

    @SuppressWarnings("unchecked")
    private void expandHiraEdiMaterialAll(String filter, Integer offset, Integer count,
                                          ValueSet.ValueSetExpansionComponent expansion, String system) {
        int from  = offset != null ? offset : 0;
        int limit = count  != null ? count  : 100;
        String where = filter != null ? " WHERE 코드 ILIKE :f OR 품명 ILIKE :f" : "";
        String sql = "SELECT 코드, 품명 FROM term.hira_치료재료_code" + where
                + " ORDER BY 코드 LIMIT " + limit + " OFFSET " + from;
        Query q = em.createNativeQuery(sql);
        if (filter != null) q.setParameter("f", "%" + filter + "%");
        String cntSql = "SELECT COUNT(*) FROM term.hira_치료재료_code" + where;
        Query cq = em.createNativeQuery(cntSql);
        if (filter != null) cq.setParameter("f", "%" + filter + "%");
        expansion.setTotal(((Number) cq.getSingleResult()).intValue());
        if (offset != null) expansion.setOffset(offset);
        for (Object[] r : (List<Object[]>) q.getResultList()) {
            expansion.addContains().setSystem(system).setCode((String) r[0])
                    .setDisplay(r[1] != null ? (String) r[1] : "");
        }
    }

    private void expandReferencedValueSet(String refUrl, String filter, Integer offset, Integer count,
                                          ValueSet.ValueSetExpansionComponent expansion) {
        String json = resourceSvc.findByUrl("ValueSet", refUrl)
                .orElseGet(() -> {
                    // URL에서 ID 추출 시도
                    String id = refUrl.contains("/") ? refUrl.substring(refUrl.lastIndexOf('/') + 1) : refUrl;
                    return resourceSvc.findById("ValueSet", id).orElse(null);
                });
        if (json == null) {
            log.warn("Referenced ValueSet not found: {}", refUrl);
            return;
        }
        IParser parser = FhirResourceService.FHIR_CTX.newJsonParser();
        ValueSet refVs = (ValueSet) parser.parseResource(json);
        if (!refVs.hasCompose()) return;
        for (ValueSet.ConceptSetComponent include : refVs.getCompose().getInclude()) {
            if (!include.hasSystem() && include.hasValueSet()) {
                for (CanonicalType nested : include.getValueSet()) {
                    expandReferencedValueSet(nested.getValue(), filter, offset, count, expansion);
                }
                continue;
            }
            expandInclude(include.getSystem(), include, filter, offset, count, expansion);
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

        // SNOMED CT implicit ValueSet은 DB 조회 없이 바로 expand
        boolean isImplicit = idOrUrl.startsWith(FhirCodeSystemService.URL_SNOMED);
        if (!isImplicit) {
            String json = resourceSvc.findById("ValueSet", idOrUrl)
                    .orElseGet(() -> resourceSvc.findByUrl("ValueSet", idOrUrl).orElse(null));
            if (json == null) {
                out.addParameter().setName("result").setValue(new BooleanType(false));
                out.addParameter().setName("message").setValue(new StringType("ValueSet not found: " + idOrUrl));
                return out;
            }
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

    /**
     * LOINC implicit ValueSet 확장
     * 지원 URL 패턴:
     *   http://loinc.org/vs                    — LOINC 전체 (count 필수)
     *   http://loinc.org/vs/LL2827-8           — Answer List (LL로 시작)
     *   http://loinc.org/vs/8480-6             — 특정 LOINC 코드의 Answer List
     *   http://loinc.org/vs/loinc-{CLASS}      — CLASS 기반 (예: loinc-CHEM)
     */
    @SuppressWarnings("unchecked")
    private ValueSet expandLoincImplicit(String url, String filter, Integer offset, Integer count) {
        String path = url.substring((FhirCodeSystemService.URL_LOINC + "/vs").length());
        // path: "" | "/LL2827-8" | "/8480-6" | "/loinc-CHEM"
        if (path.startsWith("/")) path = path.substring(1);

        ValueSet vs = new ValueSet();
        vs.setUrl(url);
        vs.setStatus(Enumerations.PublicationStatus.ACTIVE);

        ValueSet.ValueSetExpansionComponent expansion = new ValueSet.ValueSetExpansionComponent();
        expansion.setIdentifier(url);
        expansion.setTimestamp(new java.util.Date());
        if (offset != null) expansion.setOffset(offset);

        int from  = offset != null ? offset : 0;
        int limit = count  != null ? count  : 100;

        if (path.isEmpty()) {
            // 전체 LOINC
            vs.setTitle("All LOINC codes");
            String sql = "SELECT code, long_common_name FROM loinc.loinc"
                    + (filter != null ? " WHERE code ILIKE :f OR long_common_name ILIKE :f OR display_name ILIKE :f" : "")
                    + " ORDER BY code LIMIT " + limit + " OFFSET " + from;
            Query q = em.createNativeQuery(sql);
            if (filter != null) q.setParameter("f", "%" + filter + "%");
            List<Object[]> rows = q.getResultList();

            String cntSql = "SELECT COUNT(*) FROM loinc.loinc"
                    + (filter != null ? " WHERE code ILIKE :f OR long_common_name ILIKE :f OR display_name ILIKE :f" : "");
            Query cq = em.createNativeQuery(cntSql);
            if (filter != null) cq.setParameter("f", "%" + filter + "%");
            expansion.setTotal(((Number) cq.getSingleResult()).intValue());

            for (Object[] r : rows) {
                expansion.addContains()
                        .setSystem(FhirCodeSystemService.URL_LOINC)
                        .setCode((String) r[0])
                        .setDisplay(r[1] != null ? (String) r[1] : "");
            }

        } else if (path.toUpperCase().startsWith("LL")) {
            // Answer List: LL2827-8
            vs.setTitle("LOINC Answer List: " + path);
            String sql = "SELECT answer_string_id, display_text FROM loinc.la"
                    + " WHERE answer_list_id = :lid"
                    + (filter != null ? " AND (display_text ILIKE :f OR answer_string_id ILIKE :f)" : "")
                    + " ORDER BY answer_string_id LIMIT " + limit + " OFFSET " + from;
            Query q = em.createNativeQuery(sql);
            q.setParameter("lid", path);
            if (filter != null) q.setParameter("f", "%" + filter + "%");
            List<Object[]> rows = q.getResultList();

            String cntSql = "SELECT COUNT(*) FROM loinc.la WHERE answer_list_id = :lid"
                    + (filter != null ? " AND (display_text ILIKE :f OR answer_string_id ILIKE :f)" : "");
            Query cq = em.createNativeQuery(cntSql);
            cq.setParameter("lid", path);
            if (filter != null) cq.setParameter("f", "%" + filter + "%");
            expansion.setTotal(((Number) cq.getSingleResult()).intValue());

            for (Object[] r : rows) {
                expansion.addContains()
                        .setSystem(FhirCodeSystemService.URL_LOINC)
                        .setCode(r[0] != null ? (String) r[0] : "")
                        .setDisplay(r[1] != null ? (String) r[1] : "");
            }

        } else if (path.startsWith("loinc-")) {
            // CLASS 기반: loinc-CHEM
            String className = path.substring("loinc-".length());
            vs.setTitle("LOINC CLASS: " + className);
            String sql = "SELECT code, long_common_name FROM loinc.loinc"
                    + " WHERE class_name = :cls"
                    + (filter != null ? " AND (code ILIKE :f OR long_common_name ILIKE :f)" : "")
                    + " ORDER BY code LIMIT " + limit + " OFFSET " + from;
            Query q = em.createNativeQuery(sql);
            q.setParameter("cls", className);
            if (filter != null) q.setParameter("f", "%" + filter + "%");
            List<Object[]> rows = q.getResultList();

            String cntSql = "SELECT COUNT(*) FROM loinc.loinc WHERE class_name = :cls"
                    + (filter != null ? " AND (code ILIKE :f OR long_common_name ILIKE :f)" : "");
            Query cq = em.createNativeQuery(cntSql);
            cq.setParameter("cls", className);
            if (filter != null) cq.setParameter("f", "%" + filter + "%");
            expansion.setTotal(((Number) cq.getSingleResult()).intValue());

            for (Object[] r : rows) {
                expansion.addContains()
                        .setSystem(FhirCodeSystemService.URL_LOINC)
                        .setCode((String) r[0])
                        .setDisplay(r[1] != null ? (String) r[1] : "");
            }

        } else {
            // 특정 LOINC 코드의 Answer List (la_link 경유)
            String loincCode = path;
            vs.setTitle("LOINC Answer List for: " + loincCode);
            String sql = "SELECT a.answer_string_id, a.display_text"
                    + " FROM loinc.la_link l JOIN loinc.la a ON a.answer_list_id = l.answer_list_id"
                    + " WHERE l.loinc_number = :code"
                    + (filter != null ? " AND (a.display_text ILIKE :f OR a.answer_string_id ILIKE :f)" : "")
                    + " ORDER BY a.answer_list_id, a.sequence_number LIMIT " + limit + " OFFSET " + from;
            Query q = em.createNativeQuery(sql);
            q.setParameter("code", loincCode);
            if (filter != null) q.setParameter("f", "%" + filter + "%");
            List<Object[]> rows = q.getResultList();

            String cntSql = "SELECT COUNT(*) FROM loinc.la_link l JOIN loinc.la a ON a.answer_list_id = l.answer_list_id"
                    + " WHERE l.loinc_number = :code"
                    + (filter != null ? " AND (a.display_text ILIKE :f OR a.answer_string_id ILIKE :f)" : "");
            Query cq = em.createNativeQuery(cntSql);
            cq.setParameter("code", loincCode);
            if (filter != null) cq.setParameter("f", "%" + filter + "%");
            expansion.setTotal(((Number) cq.getSingleResult()).intValue());

            for (Object[] r : rows) {
                expansion.addContains()
                        .setSystem(FhirCodeSystemService.URL_LOINC)
                        .setCode(r[0] != null ? (String) r[0] : "")
                        .setDisplay(r[1] != null ? (String) r[1] : "");
            }
        }

        vs.setExpansion(expansion);
        return vs;
    }

    /**
     * SNOMED CT implicit ValueSet 확장
     * 지원 URL 패턴:
     *   http://snomed.info/sct?fhir_vs=ecl/<<73211009
     *   http://snomed.info/sct?fhir_vs=refset/450976002
     *   http://snomed.info/sct?fhir_vs  (전체)
     */
    private ValueSet expandSnomedImplicit(String url, String filter, Integer offset, Integer count) {
        String effectiveTime = schemeSvc.getEffectiveTime(schemeSvc.getLatestVersion());

        // fhir_vs 파라미터 추출
        String fhirVs = null;
        int qIdx = url.indexOf('?');
        if (qIdx >= 0) {
            String query = url.substring(qIdx + 1);
            for (String param : query.split("&")) {
                if (param.startsWith("fhir_vs=")) {
                    fhirVs = param.substring("fhir_vs=".length());
                    break;
                } else if (param.equals("fhir_vs")) {
                    fhirVs = "";
                    break;
                }
            }
        }

        List<ConceptViewDTO> concepts;
        String vsTitle;

        if (fhirVs == null || fhirVs.isEmpty()) {
            // 전체 — 너무 크므로 비지원
            return buildOutcomeValueSet("fhir_vs parameter required for SNOMED CT implicit ValueSet");
        } else if (fhirVs.startsWith("ecl/")) {
            String ecl = fhirVs.substring("ecl/".length());
            vsTitle = "SNOMED CT ECL: " + ecl;
            try {
                concepts = constraintCtrl.evaluateECLPublic(ecl, effectiveTime);
            } catch (Exception e) {
                return buildOutcomeValueSet("ECL error: " + e.getMessage());
            }
        } else if (fhirVs.startsWith("refset/")) {
            String refsetId = fhirVs.substring("refset/".length());
            vsTitle = "SNOMED CT RefSet: " + refsetId;
            try {
                concepts = constraintCtrl.evaluateECLPublic("^" + refsetId, effectiveTime);
            } catch (Exception e) {
                return buildOutcomeValueSet("RefSet error: " + e.getMessage());
            }
        } else {
            return buildOutcomeValueSet("Unsupported fhir_vs pattern: " + fhirVs);
        }

        // filter 적용
        if (filter != null && !filter.isEmpty()) {
            String lf = filter.toLowerCase();
            concepts = concepts.stream()
                    .filter(c -> (c.getConceptId() != null && c.getConceptId().contains(filter))
                            || (c.getTerm() != null && c.getTerm().toLowerCase().contains(lf)))
                    .collect(Collectors.toList());
        }

        int total = concepts.size();

        // offset / count 적용
        int from = offset != null ? offset : 0;
        int to = count != null ? Math.min(from + count, total) : total;
        if (from < total) concepts = concepts.subList(from, to);
        else concepts = Collections.emptyList();

        ValueSet vs = new ValueSet();
        vs.setUrl(url);
        vs.setTitle(vsTitle);
        vs.setStatus(Enumerations.PublicationStatus.ACTIVE);

        ValueSet.ValueSetExpansionComponent expansion = new ValueSet.ValueSetExpansionComponent();
        expansion.setIdentifier(url);
        expansion.setTimestamp(new java.util.Date());
        expansion.setTotal(total);
        if (offset != null) expansion.setOffset(offset);

        for (ConceptViewDTO c : concepts) {
            expansion.addContains()
                    .setSystem(FhirCodeSystemService.URL_SNOMED)
                    .setCode(c.getConceptId())
                    .setDisplay(c.getTerm());
        }

        vs.setExpansion(expansion);
        return vs;
    }

    private ValueSet buildOutcomeValueSet(String message) {
        ValueSet vs = new ValueSet();
        vs.setStatus(Enumerations.PublicationStatus.UNKNOWN);
        vs.setDescription(message);
        return vs;
    }
}
