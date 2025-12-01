package bh_qualifier.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GENERATE_WEBHOOK_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
    private static final String SUBMIT_WEBHOOK_URL   = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

    public void startProcess() {
        try {
            log.info("Starting webhook generation flow...");

            WebhookResponse resp = callGenerateWebhook();
            Assert.notNull(resp, "WebhookResponse must not be null");
            Assert.hasText(resp.getWebhook(), "webhook URL missing in response");
            Assert.hasText(resp.getAccessToken(), "accessToken missing in response");

            log.info("Received webhook URL: {}", resp.getWebhook());
            log.info("Received accessToken (truncated): {}", truncate(resp.getAccessToken(), 16));

            String finalQuery = buildFinalQueryPlaceholder();

            submitFinalQuery(resp.getWebhook(),
                             resp.getAccessToken(),
                             finalQuery);

            log.info("Flow completed.");
        } catch (Exception ex) {
            log.error("Error while executing webhook flow", ex);
        }
    }

    private WebhookResponse callGenerateWebhook() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Kumayl Lokhandwala");
        body.put("regNo", "22BCE2975");
        body.put("email", "kumayl.lokhandwala2022@vitstudent.ac.in");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            log.info("POST {}", GENERATE_WEBHOOK_URL);
            ResponseEntity<WebhookResponse> responseEntity =
                    restTemplate.postForEntity(GENERATE_WEBHOOK_URL, request, WebhookResponse.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                WebhookResponse resp = responseEntity.getBody();
                if (resp == null) {
                    log.error("generateWebhook returned empty body");
                    throw new RestClientException("generateWebhook returned empty body");
                }
                return resp;
            } else {
                log.error("generateWebhook returned non-2xx status: {}", responseEntity.getStatusCode());
                throw new RestClientException("Non-2xx response: " + responseEntity.getStatusCode());
            }
        } catch (RestClientException ex) {
            log.error("Failed to call generateWebhook", ex);
            throw ex;
        }
    }

    private void submitFinalQuery(String targetWebhookUrl, String accessToken, String finalQuery) {
        Assert.hasText(targetWebhookUrl, "targetWebhookUrl must not be empty");
        Assert.hasText(accessToken, "accessToken must not be empty");
        Assert.hasText(finalQuery, "finalQuery must not be empty");

        Map<String, Object> payload = new HashMap<>();
        payload.put("finalQuery", finalQuery);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("Authorization", accessToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            log.info("Submitting final query to {}", targetWebhookUrl);
            ResponseEntity<String> resp = restTemplate.postForEntity(targetWebhookUrl, request, String.class);
            log.info("Submit final query response: status={}, body={}", resp.getStatusCode(), resp.getBody());
        } catch (RestClientException ex) {
            log.error("Failed to submit final query", ex);
            throw ex;
        }
    }

    private String buildFinalQueryPlaceholder() {
        return "SELECT d.DEPARTMENT_NAME, " +
       "emp_totals.total_salary AS SALARY, " +
       "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS EMPLOYEE_NAME, " +
       "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE " +
       "FROM ( " +
       "    SELECT p.EMP_ID, SUM(p.AMOUNT) AS total_salary, e.DEPARTMENT " +
       "    FROM PAYMENTS p " +
       "    JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
       "    WHERE DAY(p.PAYMENT_TIME) <> 1 " +
       "    GROUP BY p.EMP_ID, e.DEPARTMENT " +
       ") emp_totals " +
       "JOIN ( " +
       "    SELECT DEPARTMENT, MAX(total_salary) AS max_salary " +
       "    FROM ( " +
       "        SELECT p.EMP_ID, SUM(p.AMOUNT) AS total_salary, e.DEPARTMENT " +
       "        FROM PAYMENTS p " +
       "        JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
       "        WHERE DAY(p.PAYMENT_TIME) <> 1 " +
       "        GROUP BY p.EMP_ID, e.DEPARTMENT " +
       "    ) t " +
       "    GROUP BY DEPARTMENT " +
       ") dept_max ON emp_totals.DEPARTMENT = dept_max.DEPARTMENT " +
       "           AND emp_totals.total_salary = dept_max.max_salary " +
       "JOIN EMPLOYEE e ON emp_totals.EMP_ID = e.EMP_ID " +
       "JOIN DEPARTMENT d ON emp_totals.DEPARTMENT = d.DEPARTMENT_ID";

    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookResponse {

        @JsonProperty("webhook")
        private String webhook;

        @JsonProperty("accessToken")
        private String accessToken;

        public String getWebhook() {
            return webhook;
        }

        public void setWebhook(String webhook) {
            this.webhook = webhook;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        @Override
        public String toString() {
            return "WebhookResponse{webhook='" + webhook + "', accessToken='" + (accessToken == null ? null : "[redacted]") + "'}";
        }
    }
}
