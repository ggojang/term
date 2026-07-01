package co.infoclinic.term.fhir.config;

import co.infoclinic.term.fhir.service.FhirResourceService;
import co.infoclinic.term.fhir.service.FhirValueSetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 서버 기동 시 FHIR ValueSet 초기 등록 (이미 존재하면 upsert로 덮어씀)
 */
@Component
public class FhirValueSetInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(FhirValueSetInitializer.class);

    @Autowired
    private FhirValueSetService valueSetSvc;

    private static final String[][] VALUE_SETS = {
        {
            "http://www.hl7korea.or.kr/ValueSet/kcd-8",
            "KCD8", "KCD-8 (한국표준질병사인분류 8차)",
            "http://www.hl7korea.or.kr/CodeSystem/kostat-kcd-8"
        },
        {
            "http://www.hl7korea.or.kr/ValueSet/kcd-9",
            "KCD9", "KCD-9 (한국표준질병사인분류 9차)",
            "http://www.hl7korea.or.kr/CodeSystem/kostat-kcd-9"
        },
        {
            "http://www.hl7korea.or.kr/ValueSet/hira-procedure-codes",
            "HiraProcedure", "HIRA EDI 행위코드",
            "http://www.hl7korea.or.kr/CodeSystem/hira-edi-procedure"
        },
        {
            "http://www.hl7korea.or.kr/ValueSet/hira-medication-codes",
            "HiraMedication", "HIRA EDI 약제코드",
            "http://www.hl7korea.or.kr/CodeSystem/hira-edi-medication"
        },
        {
            "http://www.hl7korea.or.kr/ValueSet/hira-material-codes",
            "HiraMaterial", "HIRA EDI 치료재료코드",
            "http://www.hl7korea.or.kr/CodeSystem/hira-edi-material"
        },
        {
            "http://www.hl7korea.or.kr/ValueSet/kpis-kdcode",
            "KpisKdcode", "KPIS KD코드 (표준코드목록)",
            "http://www.hl7korea.or.kr/CodeSystem/kpis-kdcode"
        },
        {
            "http://www.whocc.no/atc/vs",
            "ATC", "ATC (Anatomical Therapeutic Chemical)",
            "http://www.whocc.no/atc"
        },
    };

    // filter 또는 multi-include가 필요한 ValueSet JSON (전체 내용)
    private static final String[] FULL_JSON_VALUE_SETS = {
        // LOINC 전체 (loinc + LP + LG + LL + LA)
        "{\"resourceType\":\"ValueSet\",\"id\":\"loinc-all\",\"url\":\"http://loinc.org/vs\",\"version\":\"2.80\",\"name\":\"LOINCCodes\",\"title\":\"LOINC Codes\",\"status\":\"active\",\"description\":\"LOINC 전체 코드 집합 (일반 LOINC + LP Parts + LG Groups + LL Answer Lists + LA Answers)\",\"publisher\":\"Regenstrief Institute, Inc.\",\"compose\":{\"include\":[{\"system\":\"http://loinc.org\"}]}}",
        // LOINC Parts (LP)
        "{\"resourceType\":\"ValueSet\",\"id\":\"loinc-parts\",\"url\":\"http://loinc.org/vs/lp\",\"version\":\"2.80\",\"name\":\"LOINCParts\",\"title\":\"LOINC Parts (LP)\",\"status\":\"active\",\"description\":\"LOINC Part codes (LP)\",\"publisher\":\"Regenstrief Institute, Inc.\",\"compose\":{\"include\":[{\"system\":\"http://loinc.org\",\"filter\":[{\"property\":\"code\",\"op\":\"regex\",\"value\":\"^LP[0-9]\"}]}]}}",
        // LOINC Groups (LG)
        "{\"resourceType\":\"ValueSet\",\"id\":\"loinc-groups\",\"url\":\"http://loinc.org/vs/lg\",\"version\":\"2.80\",\"name\":\"LOINCGroups\",\"title\":\"LOINC Groups (LG)\",\"status\":\"active\",\"description\":\"LOINC Group codes (LG)\",\"publisher\":\"Regenstrief Institute, Inc.\",\"compose\":{\"include\":[{\"system\":\"http://loinc.org\",\"filter\":[{\"property\":\"code\",\"op\":\"regex\",\"value\":\"^LG[0-9]\"}]}]}}",
        // LOINC Answer Lists (LL)
        "{\"resourceType\":\"ValueSet\",\"id\":\"loinc-answer-lists\",\"url\":\"http://loinc.org/vs/ll\",\"version\":\"2.80\",\"name\":\"LOINCAnswerLists\",\"title\":\"LOINC Answer Lists (LL)\",\"status\":\"active\",\"description\":\"LOINC Answer List codes (LL)\",\"publisher\":\"Regenstrief Institute, Inc.\",\"compose\":{\"include\":[{\"system\":\"http://loinc.org\",\"filter\":[{\"property\":\"code\",\"op\":\"regex\",\"value\":\"^LL[0-9]\"}]}]}}",
        // LOINC Answers (LA)
        "{\"resourceType\":\"ValueSet\",\"id\":\"loinc-answers\",\"url\":\"http://loinc.org/vs/la\",\"version\":\"2.80\",\"name\":\"LOINCAnswers\",\"title\":\"LOINC Answers (LA)\",\"status\":\"active\",\"description\":\"LOINC Answer codes (LA)\",\"publisher\":\"Regenstrief Institute, Inc.\",\"compose\":{\"include\":[{\"system\":\"http://loinc.org\",\"filter\":[{\"property\":\"code\",\"op\":\"regex\",\"value\":\"^LA[0-9]\"}]}]}}",
        // SNOMED CT Clinical Findings
        "{\"resourceType\":\"ValueSet\",\"id\":\"snomed-clinical-finding\",\"url\":\"http://snomed.info/sct/ValueSet/clinical-finding\",\"name\":\"SNOMEDCTClinicalFindings\",\"title\":\"SNOMED CT Clinical Findings\",\"status\":\"active\",\"description\":\"SNOMED CT Clinical finding(404684003) 하위 개념 전체. 버전 미지정 시 최신 릴리즈 사용.\",\"compose\":{\"include\":[{\"system\":\"http://snomed.info/sct\",\"filter\":[{\"property\":\"concept\",\"op\":\"is-a\",\"value\":\"404684003\"}]}]}}",
        // Reason for Encounter
        "{\"resourceType\":\"ValueSet\",\"id\":\"reason-for-encounter\",\"url\":\"http://snomed.info/sct/ValueSet/reason-for-encounter\",\"name\":\"ReasonForEncounter\",\"title\":\"Reason for Encounter (SNOMED CT)\",\"status\":\"active\",\"description\":\"진료 사유 — SNOMED CT Clinical finding(404684003), Situation with explicit context(243796009), Event(272379006). 버전 미지정 시 최신 릴리즈 사용.\",\"compose\":{\"include\":[{\"system\":\"http://snomed.info/sct\",\"filter\":[{\"property\":\"concept\",\"op\":\"is-a\",\"value\":\"404684003\"}]},{\"system\":\"http://snomed.info/sct\",\"filter\":[{\"property\":\"concept\",\"op\":\"is-a\",\"value\":\"243796009\"}]},{\"system\":\"http://snomed.info/sct\",\"filter\":[{\"property\":\"concept\",\"op\":\"is-a\",\"value\":\"272379006\"}]}]}}",
    };

    private boolean initialized = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (initialized) return;
        initialized = true;

        log.info("FHIR ValueSet 초기 등록 시작");
        for (String[] vs : VALUE_SETS) {
            try {
                String url = vs[0], name = vs[1], title = vs[2], system = vs[3];
                String json = String.format(
                    "{\"resourceType\":\"ValueSet\",\"url\":\"%s\",\"name\":\"%s\"," +
                    "\"title\":\"%s\",\"status\":\"active\"," +
                    "\"compose\":{\"include\":[{\"system\":\"%s\"}]}}",
                    url, name, title, system);
                valueSetSvc.save(json);
                log.info("  등록: {}", url);
            } catch (Exception e) {
                log.warn("  ValueSet 등록 실패 ({}): {}", vs[0], e.getMessage());
            }
        }
        for (String json : FULL_JSON_VALUE_SETS) {
            try {
                valueSetSvc.save(json);
                log.info("  등록: {}", json.substring(json.indexOf("\"url\":\"") + 7, json.indexOf("\"", json.indexOf("\"url\":\"") + 7)));
            } catch (Exception e) {
                log.warn("  ValueSet 등록 실패: {}", e.getMessage());
            }
        }
        log.info("FHIR ValueSet 초기 등록 완료");
    }
}
