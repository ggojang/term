package co.infoclinic.term.fhir.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicInteger;

@Component("fhirAccessLogFilter")
public class FhirAccessLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FhirAccessLogFilter.class);
    private static final int MAX_BODY = 50 * 1024; // 50KB per field
    private static final long MAX_TABLE_BYTES = 100L * 1024 * 1024; // 100MB
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    @Autowired
    private DataSource dataSource;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String path = req.getRequestURI();
        // $access-log 자체는 로깅 제외 (무한루프 방지)
        return path.startsWith("/fhir/$access-log")
            || path.startsWith("/fhir/$access-log-body");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!path.startsWith("/fhir/")) {
            chain.doFilter(request, response);
            return;
        }

        long start = System.currentTimeMillis();
        ContentCachingRequestWrapper  reqW = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper resW = new ContentCachingResponseWrapper(response);

        Throwable caught = null;
        try {
            chain.doFilter(reqW, resW);
        } catch (Throwable t) {
            caught = t;
        } finally {
            int duration = (int)(System.currentTimeMillis() - start);

            String reqBody = readBody(reqW.getContentAsByteArray(), reqW.getCharacterEncoding());
            String resBody = readBody(resW.getContentAsByteArray(), resW.getCharacterEncoding());

            // 예외로 체인이 끊긴 경우 래퍼 버퍼가 비어 있으므로 예외 메시지를 기록
            if (resBody == null && caught != null) {
                resBody = "[EXCEPTION] " + caught.getClass().getName() + ": " + caught.getMessage();
            }

            // 반드시 response를 원래 스트림으로 복사
            resW.copyBodyToResponse();

            int status = resW.getStatus();
            if (caught != null && status == 200) status = 500;

            insertLog(request, status, duration, reqBody, resBody);

            // 50건마다 정리 실행 (24h 보존 + 100MB 상한)
            if (COUNTER.incrementAndGet() % 50 == 0) {
                cleanupAsync();
            }
        }

        // 예외는 로깅 후 다시 던져 Tomcat이 에러 처리를 계속하도록 함
        if (caught instanceof ServletException) throw (ServletException) caught;
        if (caught instanceof IOException)      throw (IOException)      caught;
        if (caught instanceof RuntimeException)  throw (RuntimeException)  caught;
        if (caught != null)                      throw new ServletException(caught);
    }

    private String readBody(byte[] bytes, String encoding) {
        if (bytes == null || bytes.length == 0) return null;
        try {
            int len = Math.min(bytes.length, MAX_BODY);
            String s = new String(bytes, 0, len, encoding != null ? encoding : "UTF-8");
            return bytes.length > MAX_BODY ? s + "\n...[TRUNCATED]" : s;
        } catch (Exception e) {
            return null;
        }
    }

    private void insertLog(HttpServletRequest req, int status, int duration,
                           String reqBody, String resBody) {
        try {
            String ip = req.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) ip = req.getRemoteAddr();
            else ip = ip.split(",")[0].trim();

            String ua = req.getHeader("User-Agent");
            if (ua != null && ua.length() > 300) ua = ua.substring(0, 300);

            String query = req.getQueryString();
            if (query != null && query.length() > 1000) query = query.substring(0, 1000);

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO fhir.access_log " +
                     "(method, path, query, client_ip, user_agent, status, duration_ms, request_body, response_body) " +
                     "VALUES (?,?,?,?,?,?,?,?,?)")) {
                ps.setString(1, req.getMethod());
                ps.setString(2, req.getRequestURI());
                ps.setString(3, query);
                ps.setString(4, ip);
                ps.setString(5, ua);
                ps.setInt   (6, status);
                ps.setInt   (7, duration);
                ps.setString(8, reqBody);
                ps.setString(9, resBody);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            log.warn("access_log insert failed: {}", e.getMessage());
        }
    }

    private void cleanupAsync() {
        new Thread(() -> {
            try (Connection conn = dataSource.getConnection()) {
                // 24시간 초과 삭제
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM fhir.access_log WHERE ts < NOW() - INTERVAL '24 hours'")) {
                    ps.executeUpdate();
                }
                // 테이블 크기 100MB 초과 시 오래된 20% 삭제
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT pg_total_relation_size('fhir.access_log')");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getLong(1) > MAX_TABLE_BYTES) {
                        try (PreparedStatement del = conn.prepareStatement(
                                "DELETE FROM fhir.access_log WHERE id IN " +
                                "(SELECT id FROM fhir.access_log ORDER BY ts ASC " +
                                " LIMIT (SELECT COUNT(*)/5 FROM fhir.access_log))")) {
                            del.executeUpdate();
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("access_log cleanup failed: {}", e.getMessage());
            }
        }, "access-log-cleanup").start();
    }
}
