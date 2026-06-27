package co.infoclinic.term.fhir.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

/**
 * 외부 FHIR 서버로의 요청을 서버 사이드에서 프록시.
 * CORS 우회 + GET/POST/PUT/DELETE 지원.
 *
 * GET    /fhir/$proxy?url=...&method=GET
 * POST   /fhir/$proxy?url=...&method=POST   (body: JSON)
 * POST   /fhir/$proxy?url=...&method=PUT    (body: JSON)
 * POST   /fhir/$proxy?url=...&method=DELETE
 */
@Api(tags = "V-08. FHIR Proxy")
@RestController
public class FhirProxyController {

    private static final RestTemplate REST_TEMPLATE;
    static {
        REST_TEMPLATE = new RestTemplate();
        REST_TEMPLATE.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override public boolean hasError(ClientHttpResponse r) throws IOException { return false; }
            @Override public void handleError(ClientHttpResponse r) throws IOException {}
        });
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @ApiOperation(value = "외부 FHIR 서버 프록시 [GET]")
    @RequestMapping(value = "/fhir/$proxy", method = RequestMethod.GET,
                    produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> proxyGet(
            @RequestParam("url") String targetUrl,
            @RequestParam(value = "headers", required = false) String headersJson) {
        return doProxy(targetUrl, HttpMethod.GET, null, headersJson);
    }

    @ApiOperation(value = "외부 FHIR 서버 프록시 [POST/PUT/DELETE]")
    @RequestMapping(value = "/fhir/$proxy", method = RequestMethod.POST,
                    produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> proxyPost(
            @RequestParam("url") String targetUrl,
            @RequestParam(value = "method", defaultValue = "POST") String method,
            @RequestParam(value = "headers", required = false) String headersJson,
            @RequestBody(required = false) String body) {
        HttpMethod httpMethod;
        try { httpMethod = HttpMethod.valueOf(method.toUpperCase()); }
        catch (Exception e) { httpMethod = HttpMethod.POST; }
        return doProxy(targetUrl, httpMethod, body, headersJson);
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<String> doProxy(String targetUrl, HttpMethod method, String body, String headersJson) {
        try {
            // 클라이언트가 보낸 커스텀 헤더 파싱
            Map<String, String> customHeaders = null;
            if (headersJson != null && !headersJson.isEmpty()) {
                try { customHeaders = MAPPER.readValue(headersJson, Map.class); } catch (Exception ignored) {}
            }

            String url = targetUrl;
            for (int redirects = 0; redirects < 5; redirects++) {
                HttpHeaders headers = new HttpHeaders();
                // 커스텀 헤더 적용 (없으면 기본 application/json)
                if (customHeaders != null && !customHeaders.isEmpty()) {
                    for (Map.Entry<String, String> e : customHeaders.entrySet()) {
                        headers.set(e.getKey(), e.getValue());
                    }
                } else {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                }
                HttpEntity<String> entity = (body != null && !body.isEmpty())
                    ? new HttpEntity<>(body, headers)
                    : new HttpEntity<>(headers);

                ResponseEntity<String> remote = REST_TEMPLATE.exchange(url, method, entity, String.class);
                int    status      = remote.getStatusCode().value();
                String respBody    = remote.getBody() != null ? remote.getBody() : "";
                String contentType = remote.getHeaders().getFirst("Content-Type");

                if (status >= 300 && status < 400) {
                    String location = remote.getHeaders().getFirst("Location");
                    if (location != null && !location.isEmpty()) {
                        url = location;
                        continue;
                    }
                }

                boolean isJson  = contentType != null && contentType.contains("json");
                boolean isError = status >= 400;

                if (!isError && isJson) {
                    return ResponseEntity.ok(respBody);
                }

                String wrapped = "{\"__proxy_error__\":true"
                    + ",\"__status__\":" + status
                    + ",\"__contentType__\":" + toJson(contentType)
                    + ",\"__body__\":" + toJson(respBody) + "}";
                return ResponseEntity.ok(wrapped);
            }

            String wrapped = "{\"__proxy_error__\":true,\"__status__\":0"
                + ",\"__contentType__\":null"
                + ",\"__body__\":" + toJson("Too many redirects: " + targetUrl) + "}";
            return ResponseEntity.ok(wrapped);

        } catch (Exception e) {
            String wrapped = "{\"__proxy_error__\":true,\"__status__\":0"
                + ",\"__contentType__\":null"
                + ",\"__body__\":" + toJson(e.getMessage()) + "}";
            return ResponseEntity.ok(wrapped);
        }
    }

    private String toJson(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\r", "\\r")
                       .replace("\n", "\\n")
                       .replace("\t", "\\t") + "\"";
    }
}
