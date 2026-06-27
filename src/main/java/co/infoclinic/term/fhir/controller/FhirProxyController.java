package co.infoclinic.term.fhir.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * 외부 FHIR 서버로의 요청을 서버 사이드에서 프록시.
 * 브라우저에서 다른 origin FHIR endpoint를 호출할 때 발생하는 CORS 문제를 우회.
 *
 * GET /fhir/$proxy?url=https://other-fhir-server/fhir/ValueSet/$expand
 */
@Api(tags = "V-08. FHIR Proxy")
@RestController
public class FhirProxyController {

    // 4xx/5xx에서도 예외를 던지지 않는 RestTemplate
    private static final RestTemplate REST_TEMPLATE;
    static {
        REST_TEMPLATE = new RestTemplate();
        REST_TEMPLATE.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override public boolean hasError(ClientHttpResponse r) throws IOException { return false; }
            @Override public void handleError(ClientHttpResponse r) throws IOException {}
        });
    }

    @ApiOperation(value = "외부 FHIR 서버 프록시 [GET]")
    @RequestMapping(value = "/fhir/$proxy", method = RequestMethod.GET,
                    produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> proxy(@RequestParam("url") String targetUrl) {
        try {
            String url = targetUrl;
            // 최대 5번 리다이렉트 수동 처리 (http→https 등 cross-protocol 포함)
            for (int redirects = 0; redirects < 5; redirects++) {
                ResponseEntity<String> remote = REST_TEMPLATE.getForEntity(url, String.class);
                int    status      = remote.getStatusCode().value();
                String body        = remote.getBody() != null ? remote.getBody() : "";
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
                    return ResponseEntity.ok(body);
                }

                String wrapped = "{\"__proxy_error__\":true"
                    + ",\"__status__\":" + status
                    + ",\"__contentType__\":" + toJson(contentType)
                    + ",\"__body__\":" + toJson(body) + "}";
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
