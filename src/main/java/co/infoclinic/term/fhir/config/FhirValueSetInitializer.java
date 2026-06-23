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
        log.info("FHIR ValueSet 초기 등록 완료");
    }
}
