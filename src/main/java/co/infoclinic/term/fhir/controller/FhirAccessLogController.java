package co.infoclinic.term.fhir.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "V-07. FHIR Access Log")
@RestController
public class FhirAccessLogController {

    @Autowired
    private DataSource dataSource;

    /**
     * GET /fhir/$access-log
     *   ?page=1&size=50&method=GET&path=CodeSystem&ip=&from=2026-01-01&to=2026-12-31
     */
    @ApiOperation(value = "FHIR 접근 로그 조회 [GET]")
    @RequestMapping(value = "/fhir/$access-log", method = RequestMethod.GET,
            produces = "application/json")
    public Map<String, Object> getAccessLog(
            @RequestParam(defaultValue = "1")   int    page,
            @RequestParam(defaultValue = "50")  int    size,
            @RequestParam(required = false)     String method,
            @RequestParam(required = false)     String path,
            @RequestParam(required = false)     String ip,
            @RequestParam(required = false)     String from,
            @RequestParam(required = false)     String to) {

        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE 1=1");

        if (method != null && !method.isEmpty()) {
            where.append(" AND method = ?"); params.add(method.toUpperCase());
        }
        if (path != null && !path.isEmpty()) {
            where.append(" AND path ILIKE ?"); params.add("%" + path + "%");
        }
        if (ip != null && !ip.isEmpty()) {
            where.append(" AND client_ip ILIKE ?"); params.add("%" + ip + "%");
        }
        if (from != null && !from.isEmpty()) {
            where.append(" AND ts >= ?::timestamp"); params.add(from);
        }
        if (to != null && !to.isEmpty()) {
            where.append(" AND ts < (?::timestamp + interval '1 day')"); params.add(to);
        }

        long total = 0;
        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            // 총 건수
            String countSql = "SELECT COUNT(*) FROM fhir.access_log" + where;
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                setParams(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) total = rs.getLong(1);
                }
            }

            // 데이터 조회
            int offset = (Math.max(page, 1) - 1) * size;
            List<Object> dataParams = new ArrayList<>(params);
            dataParams.add(size);
            dataParams.add(offset);
            String dataSql = "SELECT id, ts, method, path, query, client_ip, user_agent, status, duration_ms"
                    + " FROM fhir.access_log" + where
                    + " ORDER BY ts DESC LIMIT ? OFFSET ?";
            try (PreparedStatement ps = conn.prepareStatement(dataSql)) {
                setParams(ps, dataParams);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id",         rs.getLong("id"));
                        m.put("ts",         rs.getString("ts"));
                        m.put("method",     rs.getString("method"));
                        m.put("path",       rs.getString("path"));
                        m.put("query",      rs.getString("query"));
                        m.put("clientIp",   rs.getString("client_ip"));
                        m.put("userAgent",  rs.getString("user_agent"));
                        m.put("status",     rs.getInt("status"));
                        m.put("durationMs", rs.getInt("duration_ms"));
                        rows.add(m);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("page",  page);
        result.put("size",  size);
        result.put("items", rows);
        return result;
    }

    /** 요약 통계: IP별 접근 횟수, 메서드별 비율, 최근 24시간 추이 */
    @ApiOperation(value = "FHIR 접근 통계 [GET]")
    @RequestMapping(value = "/fhir/$access-log/stats", method = RequestMethod.GET,
            produces = "application/json")
    public Map<String, Object> getStats() {
        Map<String, Object> result = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            result.put("topIps",     queryList(conn,
                "SELECT client_ip AS ip, COUNT(*) AS cnt FROM fhir.access_log " +
                "GROUP BY client_ip ORDER BY cnt DESC LIMIT 10"));
            result.put("byMethod",   queryList(conn,
                "SELECT method, COUNT(*) AS cnt FROM fhir.access_log " +
                "GROUP BY method ORDER BY cnt DESC"));
            result.put("byResource", queryList(conn,
                "SELECT REGEXP_REPLACE(path, '^/fhir/([^/\\$]+).*$', '\\1') AS resource, " +
                "COUNT(*) AS cnt FROM fhir.access_log " +
                "GROUP BY resource ORDER BY cnt DESC LIMIT 10"));
            result.put("hourly",     queryList(conn,
                "SELECT TO_CHAR(DATE_TRUNC('hour', ts), 'MM-DD HH24:00') AS hour, COUNT(*) AS cnt " +
                "FROM fhir.access_log WHERE ts >= NOW() - INTERVAL '24 hours' " +
                "GROUP BY hour ORDER BY hour"));

            long todayTotal = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM fhir.access_log WHERE ts >= CURRENT_DATE");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) todayTotal = rs.getLong(1);
            }
            result.put("todayTotal", todayTotal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<Map<String, Object>> queryList(Connection conn, String sql) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= cols; i++) row.put(meta.getColumnLabel(i), rs.getObject(i));
                list.add(row);
            }
        }
        return list;
    }

    private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
    }
}
