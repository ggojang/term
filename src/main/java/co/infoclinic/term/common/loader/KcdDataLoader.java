package co.infoclinic.term.common.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * KCD-9 Korean label + neoplasm morphology loader.
 *
 * kcd9_main.tsv 처리:
 *   - 챕터 범위코드(A00-B99 등) → ICD10_CLASS 로마자 챕터(I, II...)에 매핑하여 UPDATE
 *   - 일반 코드 → ICD10_CLASS UPDATE (Korean label)
 *   - KCD 확장코드(is_ext=1, ICD10_CLASS에 없는 코드) → INSERT
 */
public class KcdDataLoader {

    private static final Logger log = Logger.getLogger(KcdDataLoader.class.getName());

    private static final String JDBC_URL      = "jdbc:postgresql://localhost:5432/term";
    private static final String JDBC_USER     = "postgres";
    private static final String JDBC_PASSWORD = "julab123!";
    private static final int    BATCH_SIZE    = 1000;

    // 챕터 범위코드 → 로마자 챕터 코드 매핑
    private static final Map<String, String> CHAPTER_MAP = new HashMap<>();
    static {
        CHAPTER_MAP.put("A00-B99",  "I");
        CHAPTER_MAP.put("C00-D48",  "II");
        CHAPTER_MAP.put("D50-D89",  "III");
        CHAPTER_MAP.put("E00-E90",  "IV");
        CHAPTER_MAP.put("F00-F99",  "V");
        CHAPTER_MAP.put("G00-G99",  "VI");
        CHAPTER_MAP.put("H00-H59",  "VII");
        CHAPTER_MAP.put("H60-H95",  "VIII");
        CHAPTER_MAP.put("I00-I99",  "IX");
        CHAPTER_MAP.put("J00-J99",  "X");
        CHAPTER_MAP.put("K00-K93",  "XI");
        CHAPTER_MAP.put("L00-L99",  "XII");
        CHAPTER_MAP.put("M00-M99",  "XIII");
        CHAPTER_MAP.put("N00-N99",  "XIV");
        CHAPTER_MAP.put("O00-O99",  "XV");
        CHAPTER_MAP.put("P00-P96",  "XVI");
        CHAPTER_MAP.put("Q00-Q99",  "XVII");
        CHAPTER_MAP.put("R00-R99",  "XVIII");
        CHAPTER_MAP.put("S00-T98",  "XIX");
        CHAPTER_MAP.put("V01-Y98",  "XX");
        CHAPTER_MAP.put("Z00-Z99",  "XXI");
        CHAPTER_MAP.put("U00-U99",  "XXII");
    }

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
            st.execute("ALTER TABLE icd10.ICD10_CLASS ADD COLUMN IF NOT EXISTS KOREAN_LABEL TEXT");
            st.execute("ALTER TABLE icd10.ICD10_CLASS ADD COLUMN IF NOT EXISTS IS_KCD_EXT BOOLEAN DEFAULT FALSE");
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

        // 3. Load kcd9_main.tsv
        loadMain(conn, new File(dir, "kcd9_main.tsv"));

        // 4. Load kcd9_morph.tsv
        loadMorph(conn, new File(dir, "kcd9_morph.tsv"));

        // 5. 확장코드 추가로 변경된 부모 노드의 CHILDREN_COUNT 갱신
        try (Statement st = conn.createStatement()) {
            int cnt = st.executeUpdate(
                "UPDATE icd10.ICD10_CLASS p " +
                "SET children_count = (SELECT COUNT(*) FROM icd10.ICD10_CLASS c WHERE c.super_class = p.code) " +
                "WHERE EXISTS (SELECT 1 FROM icd10.ICD10_CLASS c WHERE c.super_class = p.code AND c.is_kcd_ext = true)"
            );
            conn.commit();
            log.info("CHILDREN_COUNT 갱신: " + cnt + "개 부모 노드");
        }

        // 6. Index
        conn.setAutoCommit(true);
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE INDEX IF NOT EXISTS idx_kcd9_morph_label ON icd10.KCD9_MORPH(KOREAN_LABEL)");
        }
        log.info("KCD-9 적재 완료");
    }

    private static void loadMain(Connection conn, File file) throws Exception {
        if (!file.exists()) { log.warning("파일 없음: " + file); return; }
        log.info("kcd9_main 적재: " + file);

        // 현재 ICD10_CLASS에 있는 코드 목록 캐시
        Map<String, String> existingCodes = new HashMap<>(); // code → super_class
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT code, super_class FROM icd10.ICD10_CLASS")) {
            while (rs.next()) {
                existingCodes.put(rs.getString(1), rs.getString(2) == null ? "" : rs.getString(2));
            }
        }
        log.info("기존 ICD10_CLASS 코드 수: " + existingCodes.size());

        String sqlUpdate = "UPDATE icd10.ICD10_CLASS SET KOREAN_LABEL=?, IS_KCD_EXT=? WHERE CODE=?";
        String sqlInsert =
            "INSERT INTO icd10.ICD10_CLASS " +
            "(CODE, LABEL, SUPER_CLASS, KOREAN_LABEL, IS_KCD_EXT, CHILDREN_COUNT, DESCENDANT_COUNT) " +
            "VALUES (?, ?, ?, ?, TRUE, 0, 0) ON CONFLICT (CODE) DO NOTHING";

        long updated = 0, inserted = 0, chapter = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
             PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);
             PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {

            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.trim().isEmpty()) continue;
                String[] f = line.split("\t", -1);
                if (f.length < 4) continue;

                String code    = f[0].trim();
                String korean  = f[1].trim();
                String english = f[2].trim();
                boolean isExt  = "1".equals(f[3].trim());

                if (code.isEmpty() || korean.isEmpty()) continue;

                // 챕터 범위코드 처리 (A00-B99 → 로마자 I)
                if (CHAPTER_MAP.containsKey(code)) {
                    String romanCode = CHAPTER_MAP.get(code);
                    // "Ⅰ.특정 감염성 및 기생충성 질환(A00-B99)" → "특정 감염성 및 기생충성 질환" 정제
                    String cleanKorean = korean.replaceAll("^[Ⅰ-Ⅻivxlcdm]+\\.?\\s*", "")  // 로마자 접두어 제거
                                               .replaceAll("\\([A-Z0-9\\-]+\\)\\s*$", "") // 끝 범위코드 제거
                                               .trim();
                    psUpdate.setString(1, cleanKorean.isEmpty() ? korean : cleanKorean);
                    psUpdate.setBoolean(2, false);
                    psUpdate.setString(3, romanCode);
                    psUpdate.addBatch();
                    chapter++;
                    continue;
                }

                if (existingCodes.containsKey(code)) {
                    // 이미 있는 코드 → UPDATE
                    psUpdate.setString(1, korean);
                    psUpdate.setBoolean(2, isExt);
                    psUpdate.setString(3, code);
                    psUpdate.addBatch();
                    updated++;
                } else if (isExt) {
                    // ICD10_CLASS에 없는 KCD 확장코드 → INSERT
                    // super_class: 마지막 '.' 앞까지 (A08.30 → A08.3, A08 → A08)
                    String superClass = deriveSuperClass(code, existingCodes);
                    psInsert.setString(1, code);
                    psInsert.setString(2, english.isEmpty() ? korean : english);
                    psInsert.setString(3, superClass);
                    psInsert.setString(4, korean);
                    psInsert.addBatch();
                    inserted++;
                }
                // isExt=false이고 existingCodes에 없으면 범위코드(A00-A09 등) → 무시
                // (ICD10_CLASS에는 범위코드가 없음)

                if ((updated + inserted) % BATCH_SIZE == 0) {
                    psUpdate.executeBatch(); psInsert.executeBatch(); conn.commit();
                    log.info("  진행: updated=" + updated + " inserted=" + inserted);
                }
            }
            psUpdate.executeBatch();
            psInsert.executeBatch();
            conn.commit();
        }
        log.info("kcd9_main 완료: chapter=" + chapter + " updated=" + updated + " inserted=" + inserted);
    }

    /** 확장코드의 super_class 추정: 끝 문자를 하나씩 제거하며 기존 코드 탐색
     *  A08.30 → A08.3(존재) → 반환
     *  A08.301 → A08.30 → A08.3 → ... 순으로 탐색
     */
    private static String deriveSuperClass(String code, Map<String, String> existingCodes) {
        String candidate = code.substring(0, code.length() - 1);
        while (candidate.length() > 0) {
            if (existingCodes.containsKey(candidate)) return candidate;
            candidate = candidate.substring(0, candidate.length() - 1);
        }
        return code.length() >= 3 ? code.substring(0, 3) : code;
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
