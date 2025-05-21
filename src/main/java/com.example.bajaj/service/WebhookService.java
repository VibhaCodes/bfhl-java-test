package com.example.bajaj.service;

import com.example.bajaj.model.WebhookResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.Map;

@Service
public class WebhookService {

    private final RestTemplate restTemplate;

    @Autowired
    public WebhookService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @PostConstruct
    public void executeFlow() {
        try {
            WebhookResponse response = generateWebhook();
            if (response != null) {
                // since odd
                String sql = "SELECT p.AMOUNT AS SALARY, " +
                        "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
                        "FLOOR(DATEDIFF(CURRENT_DATE, e.DOB) / 365) AS AGE, " +
                        "d.DEPARTMENT_NAME " +
                        "FROM PAYMENTS p " +
                        "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
                        "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                        "WHERE DAY(p.PAYMENT_TIME) != 1 " +
                        "ORDER BY p.AMOUNT DESC " +
                        "LIMIT 1;";

                String url = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";
                submitQuery(sql, response.getAccessToken(), url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private WebhookResponse generateWebhook() {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of(
                "name", "John Doe",
                "regNo", "REG1292240005",
                "email", "vibha.pateshwari@mitwpu.edu.ins"
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<WebhookResponse> response = restTemplate.postForEntity(url, request, WebhookResponse.class);
        return response.getBody();
    }

    private void submitQuery(String sql, String token, String webhookUrl) {
        System.out.println("Access Token: " + token);
        System.out.println("SQL : " + sql);
        System.out.println("test url: " + webhookUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, String> body = Map.of("finalQuery", sql);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);
        System.out.println("Submission Status: " + response.getStatusCode());
    }
}
