package co.infoclinic.term.fhir.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class FhirAccessLogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(FhirAccessLogInterceptor.class);
    private static final String ATTR_START = "fhir.start";

    @Autowired
    private JdbcTemplate jdbc;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        req.setAttribute(ATTR_START, System.currentTimeMillis());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse res, Object handler, ModelAndView mav) {
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        try {
            long start = (Long) req.getAttribute(ATTR_START);
            int duration = (int) (System.currentTimeMillis() - start);

            String ip = req.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) ip = req.getRemoteAddr();
            else ip = ip.split(",")[0].trim();

            String ua = req.getHeader("User-Agent");
            if (ua != null && ua.length() > 300) ua = ua.substring(0, 300);

            String query = req.getQueryString();
            if (query != null && query.length() > 1000) query = query.substring(0, 1000);

            jdbc.update(
                "INSERT INTO fhir.access_log (method, path, query, client_ip, user_agent, status, duration_ms) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                req.getMethod(), req.getRequestURI(), query, ip, ua, res.getStatus(), duration
            );
        } catch (Exception e) {
            log.warn("access_log insert failed: {}", e.getMessage());
        }
    }
}
