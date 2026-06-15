package co.infoclinic.term.common.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * KCD-9 Korean label + neoplasm morphology loader.
 *
 * Input files (tab-separated, UTF-8):
 *   kcd9_main.tsv  - CODE, KOREAN_LABEL, ENGLISH_LABEL, IS_KCD_EXT
 *   kcd9_morph.tsv - CODE, KOREAN_LABEL, ENGLISH_LABEL
 *
 * Run:
 *   mvn compile exec:java -Dexec.mainClass=co.infoclinic.term.common.loader.KcdDataLoader \
 *       "-Dexec.args=/path/to/icd10/KCD-9"
 */
public class KcdDataLoader {

    private static final Logger log = Logger.getLogger(KcdDataLoader.class.getName());

    private static final String JDBC_URL      = "jdbc:postgresql://localhost:5432/term";
    private static final String JDBC_USER     = "postgres";
    private static final String JDBC_PASSWORD = "julab123!";
    private static final int    BATCH_SIZE    = 1000;

    public static void main(String[] args) throws Exception {
        String dir = args.length > 0 ? args[0]
                : "/Users/seungjong.yu/github/term/icd10/KCD-9";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);
            load(conn, dir);
        }
        System.out.println("[INFO] KCD-9 적재 완료.");
    }

    public static void load(Connection conn, String dir) throws Exception {
        // 1. Add columns to ICD10_CLASS if not exist
        try (Statement st = conn.createStatement()) {
            st.execute(
                "ALTER TABLE icd10.ICD10_CLASS ADD COLUMN IF NOT EXISTS KOREAN_LABEL TEXT");
            st.execute(
                "ALTER TABLE icd10.ICD10_CLASS ADD COLUMN IF NOT EXISTS IS_KCD_EXT BOOLEAN DEFAULT FALSE");
            conn.commit();
            log.info("ICD10_CLASS 컬럼 추가 완료");
        }

        // 2. Create KCD9_MORPH table
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS icd10.KCD9_MORPH");
            st.execute(
                "CREATE TABLE icd10.KCD9_MORPH (" +
                "  CODE          VARCHAR(20) NOT NULL PRIMARY KEY," +
                "  KOREAN_LABEL  TEXT," +
                "  ENGLISH_LABEL TEXT" +
                ")"
            );
            conn.commit();
            log.info("KCD9_MORPH 테이블 생성 완료");
        }

        // 3. Load kcd9_main.tsv → UPDATE ICD10_CLASS SET KOREAN_LABEL, IS_KCD_EXT
        loadMain(conn, new File(dir, "kcd9_main.tsv"));

        // 4. Load kcd9_morph.tsv → INSERT INTO KCD9_MORPH
        loadMorph(conn, new File(dir, "kcd9_morph.tsv"));

        // 5. Index
        conn.setAutoCommit(true);
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE INDEX IF NOT EXISTS idx_kcd9_morph_label ON icd10.KCD9_MORPH(KOREAN_LABEL)");
        }
        log.info("KCD-9 적재 완료");
    }

    private static void loadMain(Connection conn, File file) throws Exception {
        if (!file.exists()) { log.warning("파일 없음: " + file); return; }
        log.info("kcd9_main 적재: " + file);

        String sql = "UPDATE icd10.ICD10_CLASS SET KOREAN_LABEL=?, IS_KCD_EXT=? WHERE CODE=?";
        long rows = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                String[] f = line.split("\t", -1);
                if (f.length < 4) continue;
                String code = f[0].trim();
                String korean = f[1].trim();
                boolean isExt = "1".equals(f[3].trim());
                if (code.isEmpty() || korean.isEmpty()) continue;
                ps.setString(1, korean);
                ps.setBoolean(2, isExt);
                ps.setString(3, code);
                ps.addBatch();
                rows++;
                if (rows % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); log.info("  main: " + rows); }
            }
            ps.executeBatch();
            conn.commit();
        }
        log.info("kcd9_main 완료: " + rows + "행");
    }

    private static void loadMorph(Connection conn, File file) throws Exception {
        if (!file.exists()) { log.warning("파일 없음: " + file); return; }
        log.info("kcd9_morph 적재: " + file);

        String sql = "INSERT INTO icd10.KCD9_MORPH(CODE,KOREAN_LABEL,ENGLISH_LABEL) VALUES(?,?,?) ON CONFLICT DO NOTHING";
        long rows = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                String[] f = line.split("\t", -1);
                if (f.length < 3) continue;
                ps.setString(1, f[0].trim());
                ps.setString(2, f[1].trim());
                ps.setString(3, f[2].trim());
                ps.addBatch();
                rows++;
                if (rows % BATCH_SIZE == 0) { ps.executeBatch(); conn.commit(); }
            }
            ps.executeBatch();
            conn.commit();
        }
        log.info("kcd9_morph 완료: " + rows + "행");
    }
}
