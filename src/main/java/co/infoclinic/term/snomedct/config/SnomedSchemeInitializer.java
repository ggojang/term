package co.infoclinic.term.snomedct.config;

import co.infoclinic.term.snomedct.model.entity.Scheme;
import co.infoclinic.term.snomedct.repository.SchemeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * 앱 시작 시 SNOMED CT 릴리즈 플레이스홀더를 scheme + tc_meta에 미리 삽입.
 * Delta 데이터 적재 전에도 Release 드롭박스에 해당 버전이 표시된다.
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

    @Autowired
    private SchemeRepository schemeRepo;

    @Autowired
    private DataSource dataSource;

    private boolean initialized = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (initialized) return;
        initialized = true;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            for (String date : RELEASES) {
                ensureScheme(date);
                ensureTcMeta(conn, date);
            }
            conn.commit();
            log.info("SnomedSchemeInitializer 완료: {} 건", RELEASES.length);
        } catch (Exception e) {
            log.warn("SnomedSchemeInitializer 실패: {}", e.getMessage());
        }
    }

    private void ensureScheme(String date) {
        String id = "SNOMEDCT-v" + date;
        if (schemeRepo.findOne(id) == null) {
            Scheme s = new Scheme(id, "SNOMEDCT-Int", "SNOMEDCT v" + date,
                                  "v" + date, "SNOMED International", date, null);
            schemeRepo.save(s);
            log.info("  scheme 삽입: {}", id);
        }
    }

    private void ensureTcMeta(Connection conn, String date) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO term.tc_meta (effective_time, row_count) " +
                "VALUES (?, 0) ON CONFLICT (effective_time) DO NOTHING")) {
            ps.setString(1, date);
            int n = ps.executeUpdate();
            if (n > 0) log.info("  tc_meta 삽입: {}", date);
        }
    }
}
