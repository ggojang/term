package co.infoclinic.term.fhir.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 외부 FHIR 서버로의 요청을 서버 사이드에서 프록시.
 * 브라우저에서 다른 origin FHIR endpoint를 호출할 때 발생하는 CORS 문제를 우회한다.
 *
 * GET /fhir/$proxy?url=https://other-fhir-server/fhir/ValueSet/$expand&...추가파라미터
 */
@Api(tags = "V-08. FHIR Proxy")
@RestController
public class FhirProxyController {

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS    = 60_000;

    @ApiOperation(value = "외부 FHIR 서버 프록시 [GET]")
    @RequestMapping(value = "/fhir/$proxy", method = RequestMethod.GET,
                    produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> proxy(@RequestParam("url") String targetUrl) {
        try {
            URL url = new URL(targetUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/fhir+json, application/json");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setInstanceFollowRedirects(true);

            int status = conn.getResponseCode();

            boolean isError = status >= 400;
            String remoteContentType = conn.getContentType(); // 원격 서버의 실제 Content-Type
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                        isError ? conn.getErrorStream() : conn.getInputStream(),
                        StandardCharsets.UTF_8))) {
                String body = reader.lines().collect(Collectors.joining("\n"));
                HttpHeaders headers = new HttpHeaders();
                // 원격 Content-Type 전달; 없거나 알 수 없으면 JSON 기본값
                if (remoteContentType != null && remoteContentType.contains("html")) {
                    // HTML 응답은 plain text로 감싸서 JSON 파싱 오류 방지
                    String escaped = body
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\r", "\\r")
                        .replace("\n", "\\n");
                    body = "{\"__raw__\":\"" + escaped + "\",\"__contentType__\":\"" + remoteContentType + "\"}";
                }
                headers.setContentType(MediaType.APPLICATION_JSON);
                if (isError) {
                    return ResponseEntity.status(status).headers(headers).body(body);
                }
                return ResponseEntity.ok().headers(headers).body(body);
            }
        } catch (Exception e) {
            String msg = "{\"error\":\"proxy-error\",\"message\":" + jsonEscape(e.getMessage()) + "}";
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(msg);
        }
    }

    private String jsonEscape(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
