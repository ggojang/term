package co.infoclinic.term.common.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UMLS 동의어 파일 PostgreSQL 적재기
 *
 * map_index/UMLS_SYNONYM/ 의 두 파일을 읽어 PostgreSQL에 적재:
 *
 *   syn.txt  → UMLS_SYNONYM 테이블
 *     형식: canonical,syn1,syn2,syn3,...
 *     각 행의 모든 항목이 서로 동의어 (언더스코어 = 공백 대체)
 *     예: renal_failure,kidney_failure,renal_insufficiency
 *
 *   syn_ph.txt → UMLS_SYNONYM_PHRASE 테이블
 *     형식: "phrase with spaces => canonical_underscore_form"
 *     예: renal failure => renal_failure
 *     검색어(다중 단어)를 canonical로 변환할 때 사용
 *
 * ES stop_synonym_analyzer 대체 역할:
 *   - 검색어 정규화: syn_ph.txt로 구문 → canonical 변환
 *   - 동의어 확장: syn.txt로 canonical → 모든 동의어 열거
 *   - SEARCH_INDEX에서 확장된 동의어 각각으로 검색
 */
public class UmlsSynonymLoader {

    private static final Logger log = Logger.getLogger(UmlsSynonymLoader.class.getName());

    private static final String JDBC_URL      = "jdbc:postgresql://localhost:5432/term";
    private static final String JDBC_USER     = "postgres";
    private static final String JDBC_PASSWORD = "julab123!";

    private static final int BATCH_SIZE = 2000;

    // SnomedDataLoader에서 프로젝트 루트를 알고 있으므로 basePath 기준으로 파일 탐색
    // 기본 상대 경로: <project_root>/map_index/UMLS_SYNONYM/
    private static final String DEFAULT_SYN_DIR = "map_index/UMLS_SYNONYM";

    // =========================================================================
    // 진입점 (단독 실행)
    // =========================================================================

    public static void main(String[] args) throws Exception {
        String synDir = args.length > 0 ? args[0] : DEFAULT_SYN_DIR;
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);
            try (Statement s = conn.createStatement()) {
                s.execute("SET search_path TO term");
            }
            load(conn, synDir);
            conn.commit();
        }
        System.out.println("[INFO] UMLS 동의어 적재 완료.");
    }

    // =========================================================================
    // SnomedDataLoader에서 호출
    // =========================================================================

    /**
     * @param conn   기존 JDBC 연결 (autoCommit=false 전제)
     * @param synDir syn.txt / syn_ph.txt 가 있는 디렉토리 절대/상대 경로
     */
    public static void load(Connection conn, String synDir) throws Exception {
        File dir = new File(synDir);
        if (!dir.exists()) {
            log.warning("UMLS_SYNONYM 디렉토리 없음: " + dir.getAbsolutePath() + " → 건너뜀");
            return;
        }

        File synFile   = new File(dir, "syn.txt");
        File synPhFile = new File(dir, "syn_ph.txt");

        log.info("UMLS 동의어 적재 시작: " + dir.getAbsolutePath());

        // 기존 데이터 초기화
        try (Statement s = conn.createStatement()) {
            s.execute("TRUNCATE TABLE term.umls_synonym RESTART IDENTITY");
            s.execute("TRUNCATE TABLE term.umls_synonym_phrase RESTART IDENTITY");
        }
        conn.commit();

        // syn.txt → UMLS_SYNONYM
        if (synFile.exists()) {
            loadSynonyms(conn, synFile);
            conn.commit();
            log.info("  syn.txt 적재 완료");
        } else {
            log.warning("  syn.txt 없음: " + synFile.getAbsolutePath());
        }

        // syn_ph.txt → UMLS_SYNONYM_PHRASE
        if (synPhFile.exists()) {
            loadPhrases(conn, synPhFile);
            conn.commit();
            log.info("  syn_ph.txt 적재 완료");
        } else {
            log.warning("  syn_ph.txt 없음: " + synPhFile.getAbsolutePath());
        }
    }

    // =========================================================================
    // syn.txt 적재
    // 형식: canonical,syn1,syn2,...
    // → 첫 항목이 canonical, 모든 항목 쌍으로 (canonical, synonym) INSERT
    // =========================================================================

    private static void loadSynonyms(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO term.umls_synonym (canonical, synonym) VALUES (?, ?)";
        long count = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",");
                if (parts.length < 2) continue;

                String canonical = parts[0].trim();
                if (canonical.isEmpty()) continue;

                // canonical과 모든 synonym 쌍으로 삽입
                for (int i = 1; i < parts.length; i++) {
                    String synonym = parts[i].trim();
                    if (synonym.isEmpty()) continue;

                    ps.setString(1, canonical);
                    ps.setString(2, synonym);
                    ps.addBatch();
                    count++;

                    if (count % BATCH_SIZE == 0) {
                        ps.executeBatch();
                        conn.commit();
                        log.log(Level.INFO, "  UMLS_SYNONYM 적재 중: {0}건...", count);
                    }
                }
            }

            if (count % BATCH_SIZE != 0) {
                ps.executeBatch();
            }
        }
        log.info("  UMLS_SYNONYM 총 " + count + "건 적재");
    }

    // =========================================================================
    // syn_ph.txt 적재
    // 형식: "phrase with spaces => canonical_underscore_form"
    // =========================================================================

    private static void loadPhrases(Connection conn, File file) throws Exception {
        String sql = "INSERT INTO term.umls_synonym_phrase (phrase, canonical) VALUES (?, ?)";
        long count = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                int sepIdx = line.indexOf("=>");
                if (sepIdx < 0) continue;

                String phrase    = line.substring(0, sepIdx).trim();
                String canonical = line.substring(sepIdx + 2).trim();
                if (phrase.isEmpty() || canonical.isEmpty()) continue;

                ps.setString(1, phrase.toLowerCase());
                ps.setString(2, canonical);
                ps.addBatch();
                count++;

                if (count % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();
                    log.log(Level.INFO, "  UMLS_SYNONYM_PHRASE 적재 중: {0}건...", count);
                }
            }

            if (count % BATCH_SIZE != 0) {
                ps.executeBatch();
            }
        }
        log.info("  UMLS_SYNONYM_PHRASE 총 " + count + "건 적재");
    }
}
