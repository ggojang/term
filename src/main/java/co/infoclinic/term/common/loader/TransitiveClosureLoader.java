package co.infoclinic.term.common.loader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SNOMED CT Direct IS-A 관계 적재기 (Snowstorm 방식)
 *
 * TC 테이블에는 직접 IS-A 관계 (child_id, parent_id)만 저장.
 * 계층 탐색(조상/자손)은 조회 시 재귀 CTE로 실시간 계산.
 *
 * diff 키: (child_id, parent_id)
 *   - 신규 IS-A → INSERT (valid_from=ET, valid_to='99991231')
 *   - 제거 IS-A → CLOSE  (valid_to=ET)
 *   - 유지 IS-A → no-op
 *
 * 행 수: ~70만 행 (기존 18M 행 대비 1/25)
 * 적재 속도: 첫 릴리즈 ~2초, 증분 ~수초
 */
public class TransitiveClosureLoader {

    private static final Logger log = Logger.getLogger(TransitiveClosureLoader.class.getName());

    private static final String JDBC_URL      = "jdbc:postgresql://localhost:5432/term";
    private static final String JDBC_USER     = "postgres";
    private static final String JDBC_PASSWORD = "julab123!";

    private static final String ISA_TYPE_ID   = "116680003";
    private static final String VALID_TO_OPEN = "99991231";

    private static final int BATCH_SIZE = 5000;

    // ── 적재 대상: effectiveTime 시점의 활성 IS-A 관계 ──────────────
    // key: "child_id|parent_id"
    private final Set<String> newIsaSet = new HashSet<>();

    // ── 현재 활성 TC 스냅샷 (CLOSE 대상 계산용) ─────────────────────
    // key: "child_id|parent_id"
    private final Set<String> activeTcSet = new HashSet<>();

    private Connection        conn;
    private String            effectiveTime;
    private long insertedRows = 0;
    private long closedRows   = 0;

    // =========================================================================
    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);
            try (Statement s = conn.createStatement()) {
                s.execute("SET search_path TO term");
            }
            load(conn, args.length > 0 ? args[0] : null);
            conn.commit();
        }
        System.out.println("[INFO] TC 적재 완료.");
    }

    public static void load(Connection conn, String effectiveTime) throws Exception {
        new TransitiveClosureLoader().doLoad(conn, effectiveTime);
    }

    // =========================================================================
    private void doLoad(Connection conn, String etParam) throws Exception {
        this.conn = conn;
        long start = System.currentTimeMillis();

        this.effectiveTime = (etParam != null && !etParam.isEmpty())
                ? etParam : resolveLatestEffectiveTime();
        log.info("TC 생성 시작 (effectiveTime=" + this.effectiveTime + ")");

        // 1. effectiveTime 시점 활성 IS-A 관계 로드
        loadActiveIsaRelationships();
        log.info("  IS-A 관계 로딩 완료: " + newIsaSet.size() + "건");

        // 2. 현재 TC 활성 스냅샷 로드
        boolean isEmpty = loadActiveTcSnapshot();
        log.info("  현재 활성 TC: " + activeTcSet.size() + "건"
                + (isEmpty ? " (초기 적재)" : " (증분 적재)"));

        // 3. diff → INSERT
        insertNewRows();
        conn.commit();

        // 4. CLOSE: 이번 릴리즈에서 사라진 IS-A 관계
        if (!isEmpty) {
            closeRemovedRows();
            conn.commit();
        }

        // 5. TC_META 갱신
        updateTcMeta();
        conn.commit();

        long elapsed = (System.currentTimeMillis() - start) / 1000;
        log.info("TC 완료 (effectiveTime=" + this.effectiveTime + "): "
                + "INSERT=" + insertedRows + ", CLOSE=" + closedRows + ", 소요=" + elapsed + "초");
    }

    // ── effectiveTime 자동 결정 ──────────────────────────────────────
    private String resolveLatestEffectiveTime() throws Exception {
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(
                 "SELECT MAX(effective_time) FROM term.inferred_relationship")) {
            if (rs.next()) {
                String et = rs.getString(1);
                if (et != null && !et.isEmpty()) return et;
            }
        }
        throw new IllegalStateException("inferred_relationship에서 effectiveTime 조회 실패");
    }

    // ── effectiveTime 시점의 활성 IS-A 관계 로드 ────────────────────
    private void loadActiveIsaRelationships() throws Exception {
        String sql =
            "SELECT source_id, destination_id FROM (" +
            "  SELECT DISTINCT ON (source_id, destination_id)" +
            "    source_id, destination_id, active" +
            "  FROM term.inferred_relationship" +
            "  WHERE type_id='" + ISA_TYPE_ID + "'" +
            "    AND effective_time<='" + this.effectiveTime + "'" +
            "  ORDER BY source_id, destination_id, effective_time DESC" +
            ") x WHERE active=1";

        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                newIsaSet.add(rs.getString(1) + "|" + rs.getString(2));
            }
        }
    }

    // ── 현재 활성 TC 스냅샷 로드 ────────────────────────────────────
    private boolean loadActiveTcSnapshot() throws Exception {
        String sql = "SELECT child_id, parent_id FROM term.tc WHERE valid_to='" + VALID_TO_OPEN + "'";
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                activeTcSet.add(rs.getString(1) + "|" + rs.getString(2));
            }
        }
        return activeTcSet.isEmpty();
    }

    // ── 신규 IS-A → INSERT ───────────────────────────────────────────
    private void insertNewRows() throws Exception {
        String sql = "INSERT INTO term.tc (child_id, parent_id, valid_from, valid_to) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            long pending = 0;
            for (String key : newIsaSet) {
                if (activeTcSet.contains(key)) continue; // 유지 → no-op
                String[] p = key.split("\\|", 2);
                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setString(3, effectiveTime);
                ps.setString(4, VALID_TO_OPEN);
                ps.addBatch();
                insertedRows++;
                pending++;
                if (pending % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();
                    log.log(Level.INFO, "  TC INSERT 중: {0}건...", insertedRows);
                }
            }
            if (pending % BATCH_SIZE != 0) ps.executeBatch();
        }
    }

    // ── CLOSE: 사라진 IS-A 관계 (deadlock 재시도 포함) ───────────────
    private void closeRemovedRows() throws Exception {
        Set<String> removed = new HashSet<>(activeTcSet);
        removed.removeAll(newIsaSet);
        if (removed.isEmpty()) return;
        log.info("  CLOSE 대상: " + removed.size() + "건...");

        try (Statement s = conn.createStatement()) {
            s.execute("SET lock_timeout = '10s'");
        }

        String sql = "UPDATE term.tc SET valid_to=? WHERE child_id=? AND parent_id=? AND valid_to='" + VALID_TO_OPEN + "'";
        String[] keys = removed.toArray(new String[0]);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            long pending = 0;
            for (String key : keys) {
                String[] p = key.split("\\|", 2);
                ps.setString(1, effectiveTime);
                ps.setString(2, p[0]);
                ps.setString(3, p[1]);
                ps.addBatch();
                closedRows++;
                pending++;
                if (pending % BATCH_SIZE == 0) {
                    executeBatchWithRetry(ps, conn);
                }
            }
            if (pending % BATCH_SIZE != 0) executeBatchWithRetry(ps, conn);
        }
    }

    private void executeBatchWithRetry(PreparedStatement ps, Connection conn) throws Exception {
        for (int retry = 0; retry < 3; retry++) {
            try {
                ps.executeBatch();
                conn.commit();
                return;
            } catch (java.sql.BatchUpdateException e) {
                if (e.getMessage().contains("deadlock") && retry < 2) {
                    log.warning("  deadlock 재시도 " + (retry + 1) + "...");
                    conn.rollback();
                    Thread.sleep(500L * (retry + 1));
                    ps.clearBatch();
                } else { throw e; }
            }
        }
    }

    // ── TC_META 갱신 ─────────────────────────────────────────────────
    private void updateTcMeta() throws Exception {
        String sql =
            "INSERT INTO term.tc_meta (effective_time, row_count) " +
            "SELECT ?, COUNT(*) FROM term.tc WHERE valid_from<=? AND valid_to>? " +
            "ON CONFLICT (effective_time) DO UPDATE SET row_count=EXCLUDED.row_count";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, effectiveTime);
            ps.setString(2, effectiveTime);
            ps.setString(3, effectiveTime);
            ps.executeUpdate();
        }
        log.info("  TC_META 갱신 완료 (" + effectiveTime + ")");
    }
}
