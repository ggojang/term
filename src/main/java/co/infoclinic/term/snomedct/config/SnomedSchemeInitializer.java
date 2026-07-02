package co.infoclinic.term.snomedct.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

/**
 * 앱 시작 시 SNOMED CT 릴리즈 플레이스홀더를 scheme + tc_meta에 미리 삽입.
 *
 * Delta 데이터가 아직 적재되지 않아도 Release 드롭박스에 해당 버전이 표시되도록 한다.
 * Delta 적재 후 TransitiveClosureLoader가 tc_meta의 row_count를 실제 값으로 갱신한다.
 *
 * RELEASES 배열에 새 릴리즈 날짜(YYYYMMDD)를 추가하면 드롭박스에 즉시 반영된다.
 */
@Component
public class SnomedSchemeInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(SnomedSchemeInitializer.class);

    /** 드롭박스에 표시할 릴리즈 날짜 목록 (YYYYMMDD). 최신 순으로 유지. */
    private static final String[] RELEASES = {
        "20260701",
        "20260601",
    };

    @PersistenceContext
    private EntityManager em;

    private boolean initialized = false;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (initialized) return;
        initialized = true;

        for (String date : RELEASES) {
            ensureScheme(date);
            ensureTcMeta(date);
        }
        log.info("SnomedSchemeInitializer 완료: {} 건", RELEASES.length);
    }

    private void ensureScheme(String date) {
        String id = "SNOMEDCT-v" + date;
        int updated = em.createNativeQuery(
            "INSERT INTO term.scheme (id, name, edition, version, authority, date) " +
            "VALUES (:id, 'SNOMEDCT-Int', :edition, :version, 'SNOMED International', :date) " +
            "ON CONFLICT (id) DO NOTHING")
            .setParameter("id", id)
            .setParameter("edition", "SNOMEDCT v" + date)
            .setParameter("version", "v" + date)
            .setParameter("date", date)
            .executeUpdate();
        if (updated > 0) log.info("  scheme 삽입: {}", id);
    }

    private void ensureTcMeta(String date) {
        int updated = em.createNativeQuery(
            "INSERT INTO term.tc_meta (effective_time, row_count) " +
            "VALUES (:et, 0) " +
            "ON CONFLICT (effective_time) DO NOTHING")
            .setParameter("et", date)
            .executeUpdate();
        if (updated > 0) log.info("  tc_meta 삽입: {}", date);
    }
}
