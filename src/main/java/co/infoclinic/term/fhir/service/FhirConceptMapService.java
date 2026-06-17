package co.infoclinic.term.fhir.service;

import ca.uhn.fhir.parser.IParser;
import co.infoclinic.term.fhir.model.entity.FhirResource;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class FhirConceptMapService {

    @Autowired
    private FhirResourceService resourceSvc;

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    public String save(String json) {
        return resourceSvc.save("ConceptMap", json);
    }

    public Optional<String> findById(String id) {
        return resourceSvc.findById("ConceptMap", id);
    }

    public Optional<String> findByUrl(String url) {
        return resourceSvc.findByUrl("ConceptMap", url);
    }

    public List<FhirResource> search(String name, String url) {
        if (url != null && !url.isEmpty()) {
            return resourceSvc.findByUrl("ConceptMap", url)
                    .map(json -> { FhirResource r = new FhirResource(); r.setContent(json); return Collections.singletonList(r); })
                    .orElse(Collections.<FhirResource>emptyList());
        }
        return resourceSvc.search("ConceptMap", name);
    }

    public void delete(String id) {
        resourceSvc.delete("ConceptMap", id);
    }

    /**
     * $translate: 소스 코드를 타겟 코드시스템으로 변환
     */
    @SuppressWarnings("unchecked")
    public Parameters translate(String system, String code, String targetSystem, String conceptMapUrl) {
        Parameters out = new Parameters();

        // SNOMED → ICD-10 매핑 (term.map 테이블 활용)
        if (FhirCodeSystemService.URL_SNOMED.equals(system) && targetSystem != null &&
                targetSystem.contains("icd")) {
            return translateSnomedToIcd(code, targetSystem, out);
        }

        // 저장된 ConceptMap에서 조회
        return translateFromStoredConceptMap(system, code, targetSystem, conceptMapUrl, out);
    }

    @SuppressWarnings("unchecked")
    private Parameters translateSnomedToIcd(String code, String targetSystem, Parameters out) {
        // term.map 테이블 (SNOMED to ICD-10 Extended Map)
        String sql = "SELECT map_target, map_advice, map_rule, map_group " +
                "FROM term.map " +
                "WHERE referenced_component_id = :code AND active = true " +
                "ORDER BY map_group, map_priority";
        Query q = em.createNativeQuery(sql);
        q.setParameter("code", code);
        List<Object[]> rows = q.getResultList();

        if (rows.isEmpty()) {
            out.addParameter().setName("result").setValue(new BooleanType(false));
            out.addParameter().setName("message").setValue(new StringType("No mapping found for code: " + code));
            return out;
        }

        out.addParameter().setName("result").setValue(new BooleanType(true));
        for (Object[] row : rows) {
            Parameters.ParametersParameterComponent match = out.addParameter();
            match.setName("match");
            match.addPart().setName("equivalence").setValue(new CodeType("equivalent"));
            match.addPart().setName("concept")
                    .setValue(new Coding().setSystem(targetSystem).setCode((String) row[0]));
            if (row[1] != null) {
                match.addPart().setName("comment").setValue(new StringType((String) row[1]));
            }
        }
        return out;
    }

    private Parameters translateFromStoredConceptMap(String system, String code,
                                                     String targetSystem, String conceptMapUrl,
                                                     Parameters out) {
        Optional<String> cmJson = conceptMapUrl != null
                ? resourceSvc.findByUrl("ConceptMap", conceptMapUrl)
                : Optional.empty();

        if (!cmJson.isPresent()) {
            // URL 없으면 system 기반으로 매칭되는 ConceptMap 검색
            List<FhirResource> maps = resourceSvc.findAll("ConceptMap");
            for (FhirResource r : maps) {
                IParser parser = FhirResourceService.FHIR_CTX.newJsonParser();
                ConceptMap cm = (ConceptMap) parser.parseResource(r.getContent());
                if (system.equals(cm.getSourceUriType().asStringValue()) ||
                        system.equals(cm.getSourceCanonicalType().asStringValue())) {
                    cmJson = Optional.of(r.getContent());
                    break;
                }
            }
        }

        if (!cmJson.isPresent()) {
            out.addParameter().setName("result").setValue(new BooleanType(false));
            out.addParameter().setName("message").setValue(new StringType("No ConceptMap found"));
            return out;
        }

        IParser parser = FhirResourceService.FHIR_CTX.newJsonParser();
        ConceptMap cm = (ConceptMap) parser.parseResource(cmJson.get());

        for (ConceptMap.ConceptMapGroupComponent group : cm.getGroup()) {
            if (!system.equals(group.getSource())) continue;
            for (ConceptMap.SourceElementComponent element : group.getElement()) {
                if (!code.equals(element.getCode())) continue;
                out.addParameter().setName("result").setValue(new BooleanType(true));
                for (ConceptMap.TargetElementComponent target : element.getTarget()) {
                    Parameters.ParametersParameterComponent match = out.addParameter();
                    match.setName("match");
                    match.addPart().setName("equivalence")
                            .setValue(new CodeType(target.getEquivalence().toCode()));
                    match.addPart().setName("concept")
                            .setValue(new Coding().setSystem(group.getTarget()).setCode(target.getCode())
                                    .setDisplay(target.getDisplay()));
                }
                return out;
            }
        }

        out.addParameter().setName("result").setValue(new BooleanType(false));
        out.addParameter().setName("message").setValue(new StringType("No match for code: " + code));
        return out;
    }
}
