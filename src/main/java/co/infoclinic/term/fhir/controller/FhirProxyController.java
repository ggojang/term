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
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                        isError ? conn.getErrorStream() : conn.getInputStream(),
                        StandardCharsets.UTF_8))) {
                String body = reader.lines().collect(Collectors.joining("\n"));
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return ResponseEntity.status(isError ? status : HttpStatus.OK).headers(headers).body(body);
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
