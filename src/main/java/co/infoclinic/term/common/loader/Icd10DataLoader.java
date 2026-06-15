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
 * ICD-10 (2019 WHO) PostgreSQL 적재기
 *
 * 입력 파일 (tab-separated, UTF-8):
 *   ICD10Class.txt  - SEQ,Code,Version,Class_Kind,Usage_Kind,Super_Class,Label,REF,Children_count,Descendant_count,PATH
 *   ICD10Rubric.txt - SEQ,Code,Version,Id,Kind,Modifier_Code,Usage_Kind,Lang,Fragment_Type,Para_Type,Label,REF
 *
 * 실행:
 *   mvn compile exec:java -Dexec.mainClass=co.infoclinic.term.common.loader.Icd10DataLoader \
 *       -Dexec.args="/path/to/icd10/bin"
 */
public class Icd10DataLoader {

    private static final Logger log = Logger.getLogger(Icd10DataLoader.class.getName());

    private static final String JDBC_URL      = "jdbc:postgresql://localhost:5432/term";
    private static final String JDBC_USER     = "postgres";
    private static final String JDBC_PASSWORD = "julab123!";

    private static final int BATCH_SIZE = 5000;

    public static void main(String[] args) throws Exception {
        String binDir = args.length > 0 ? args[0]
                : "/Users/seungjong.yu/github/term/icd10/bin";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);
            load(conn, binDir);
        }
        System.out.println("[INFO] ICD-10 적재 완료.");
    }

    public static void load(Connection conn, String binDir) throws Exception {
        long t0 = System.currentTimeMillis();
        log.info("ICD-10 적재 시작: " + binDir);

        try (Statement st = conn.createStatement()) {
            st.execute("SET synchronous_commit = OFF");
            st.execute("SET work_mem = '128MB'");
        }

        // ─── 스키마/테이블 초기화 ───────────────────────────────────────
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE SCHEMA IF NOT EXISTS icd10");
            st.execute("DROP TABLE IF EXISTS icd10.ICD10_RUBRIC CASCADE");
            st.execute("DROP TABLE IF EXISTS icd10.ICD10_CLASS  CASCADE");
            st.execute(
                "CREATE TABLE icd10.ICD10_CLASS (" +
                "  SEQ              INTEGER," +
                "  CODE             VARCHAR(20) NOT NULL," +
                "  VERSION          VARCHAR(10)," +
                "  CLASS_KIND       VARCHAR(20)," +
                "  USAGE_KIND       VARCHAR(20)," +
                "  SUPER_CLASS      VARCHAR(20)," +
                "  LABEL            TEXT," +
                "  REF              TEXT," +
                "  CHILDREN_COUNT   INTEGER DEFAULT 0," +
                "  DESCENDANT_COUNT INTEGER DEFAULT 0," +
                "  PATH             TEXT," +
                "  PRIMARY KEY (CODE)" +
                ")"
            );
            st.execute(
                "CREATE TABLE icd10.ICD10_RUBRIC (" +
                "  SEQ           SERIAL PRIMARY KEY," +
                "  CODE          VARCHAR(20) NOT NULL," +
                "  VERSION       VARCHAR(10)," +
                "  ID            VARCHAR(100)," +
                "  KIND          VARCHAR(20)," +
                "  MODIFIER_CODE VARCHAR(20)," +
                "  USAGE_KIND    VARCHAR(20)," +
                "  LANG          VARCHAR(5)," +
                "  FRAGMENT_TYPE VARCHAR(20)," +
                "  PARA_TYPE     VARCHAR(50)," +
                "  LABEL         TEXT," +
                "  REF           TEXT" +
                ")"
            );
            conn.commit();
        }
        log.info("테이블 초기화 완료");

        // ─── ICD10_CLASS 적재 ──────────────────────────────────────────
        loadClass(conn, new File(binDir, "ICD10Class.txt"));

        // ─── ICD10_RUBRIC 적재 ─────────────────────────────────────────
        loadRubric(conn, new File(binDir, "ICD10Rubric.txt"));

        // ─── 인덱스 생성 ────────────────────────────────────────────────
        conn.setAutoCommit(true);
        log.info("인덱스 생성 중...");
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE INDEX idx_icd10_class_super ON icd10.ICD10_CLASS(SUPER_CLASS)");
            st.execute("CREATE INDEX idx_icd10_class_path  ON icd10.ICD10_CLASS(PATH)");
            st.execute("CREATE INDEX idx_icd10_rubric_code ON icd10.ICD10_RUBRIC(CODE)");
            st.execute("CREATE INDEX idx_icd10_rubric_kind ON icd10.ICD10_RUBRIC(KIND)");
            // pg_trgm 없는 환경에서도 동작하도록 일반 인덱스 사용 (ILIKE는 순차 스캔)
            // st.execute("CREATE INDEX idx_icd10_rubric_label_trgm ON icd10.ICD10_RUBRIC USING GIN (LABEL gin_trgm_ops)");
        }
        log.info("인덱스 생성 완료");

        long elapsed = (System.currentTimeMillis() - t0) / 1000;
        log.info(String.format("ICD-10 적재 완료 (소요: %d초)", elapsed));
    }

    // ─── ICD10_CLASS ──────────────────────────────────────────────────────────
    private static void loadClass(Connection conn, File file) throws Exception {
        if (!file.exists()) {
            log.warning("파일 없음 (건너뜀): " + file);
            return;
        }
        log.info("ICD10_CLASS 적재: " + file);

        String sql = "INSERT INTO icd10.ICD10_CLASS " +
                     "(SEQ,CODE,VERSION,CLASS_KIND,USAGE_KIND,SUPER_CLASS,LABEL,REF,CHILDREN_COUNT,DESCENDANT_COUNT,PATH) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (CODE) DO NOTHING";

        long rows = 0;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; } // header
                if (line.trim().isEmpty()) continue;

                String[] f = line.split("\t", -1);
                if (f.length < 11) continue;

                ps.setInt   (1,  parseIntSafe(f[0]));   // SEQ
                ps.setString(2,  clean(f[1]));           // CODE
                ps.setString(3,  clean(f[2]));           // VERSION
                ps.setString(4,  clean(f[3]));           // CLASS_KIND
                ps.setString(5,  nullIfEmpty(f[4]));     // USAGE_KIND
                ps.setString(6,  nullIfEmpty(f[5]));     // SUPER_CLASS
                ps.setString(7,  clean(f[6]));           // LABEL
                ps.setString(8,  nullIfEmpty(f[7]));     // REF
                ps.setInt   (9,  parseIntSafe(f[8]));    // CHILDREN_COUNT
                ps.setInt   (10, parseIntSafe(f[9]));    // DESCENDANT_COUNT
                ps.setString(11, nullIfEmpty(f[10]));    // PATH
                ps.addBatch();
                rows++;

                if (rows % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();
                    log.info("  ICD10_CLASS: " + rows + "행");
                }
            }
            ps.executeBatch();
            conn.commit();
        }
        log.info("ICD10_CLASS 완료: " + rows + "행");
    }

    // ─── ICD10_RUBRIC ─────────────────────────────────────────────────────────
    private static void loadRubric(Connection conn, File file) throws Exception {
        if (!file.exists()) {
            log.warning("파일 없음 (건너뜀): " + file);
            return;
        }
        log.info("ICD10_RUBRIC 적재: " + file);

        String sql = "INSERT INTO icd10.ICD10_RUBRIC " +
                     "(CODE,VERSION,ID,KIND,MODIFIER_CODE,USAGE_KIND,LANG,FRAGMENT_TYPE,PARA_TYPE,LABEL,REF) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        long rows = 0;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; } // header
                if (line.trim().isEmpty()) continue;

                String[] f = line.split("\t", -1);
                if (f.length < 11) continue;

                // txt columns: SEQ(0),Code(1),Version(2),Id(3),Kind(4),Modifier_Code(5),Usage_Kind(6),Lang(7),Fragment_Type(8),Para_Type(9),Label(10),REF(11)
                ps.setString(1,  clean(f[1]));           // CODE
                ps.setString(2,  clean(f[2]));           // VERSION
                ps.setString(3,  nullIfEmpty(f[3]));     // ID
                ps.setString(4,  nullIfEmpty(f[4]));     // KIND
                ps.setString(5,  nullIfEmpty(f[5]));     // MODIFIER_CODE
                ps.setString(6,  nullIfEmpty(f[6]));     // USAGE_KIND
                ps.setString(7,  nullIfEmpty(f[7]));     // LANG
                ps.setString(8,  nullIfEmpty(f[8]));     // FRAGMENT_TYPE
                ps.setString(9,  nullIfEmpty(f[9]));     // PARA_TYPE
                ps.setString(10, clean(f[10]));          // LABEL
                ps.setString(11, f.length > 11 ? nullIfEmpty(f[11]) : null); // REF
                ps.addBatch();
                rows++;

                if (rows % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();
                    log.info("  ICD10_RUBRIC: " + rows + "행");
                }
            }
            ps.executeBatch();
            conn.commit();
        }
        log.info("ICD10_RUBRIC 완료: " + rows + "행");
    }

    // ─── 유틸 ─────────────────────────────────────────────────────────────────
    private static String clean(String s) {
        return s == null ? "" : s.trim();
    }

    private static String nullIfEmpty(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }
}
