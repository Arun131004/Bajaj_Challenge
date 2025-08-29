package com.example.bajaj_challenge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Component
public class ChallengeRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- Starting Bajaj Finserv Health Challenge ---");

        // === Step 1: Generate Webhook and Get Access Token ===
        RestTemplate restTemplate = new RestTemplate();
        String generateWebhookUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        // IMPORTANT: Replace these values with your own details
        String registrationDetails = "{\"name\": \"Arun E\", \"regNo\": \"22BEC0128\", \"email\": \"arun.e2022@vitstudent.ac.in\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> generateRequest = new HttpEntity<>(registrationDetails, headers);
        
        String accessToken = "";
        
        try {
            System.out.println("Sending request to generate webhook...");
            ResponseEntity<String> generateResponse = restTemplate.postForEntity(generateWebhookUrl, generateRequest, String.class);
            System.out.println("Webhook Generation Response: " + generateResponse.getBody());

            // Parse the response to get the accessToken
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(generateResponse.getBody());
            accessToken = rootNode.path("accessToken").asText();

            if (accessToken.isEmpty()) {
                throw new IllegalStateException("accessToken not found in response.");
            }
             System.out.println("Successfully retrieved accessToken.");

        } catch (Exception e) {
            System.err.println("Error during webhook generation: " + e.getMessage());
            return; // Stop execution if we can't get the token
        }

        // === Step 2: Prepare and Submit the SQL Query ===
        String submissionUrl = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

        // The final SQL query for your even registration number
        String finalQuery = "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, (SELECT COUNT(*) FROM EMPLOYEE e2 WHERE e2.DEPARTMENT = e1.DEPARTMENT AND e2.DOB > e1.DOB) AS YOUNGER_EMPLOYEES_COUNT FROM EMPLOYEE e1 JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID ORDER BY e1.EMP_ID DESC;";
        
        String submissionBody = "{\"finalQuery\": \"" + finalQuery + "\"}";

        HttpHeaders submissionHeaders = new HttpHeaders();
        submissionHeaders.setContentType(MediaType.APPLICATION_JSON);
        submissionHeaders.set("Authorization", accessToken); // Use the retrieved JWT token [cite: 49, 70, 80]

        HttpEntity<String> submissionRequest = new HttpEntity<>(submissionBody, submissionHeaders);

        try {
            System.out.println("Submitting the final SQL query...");
            ResponseEntity<String> submissionResponse = restTemplate.postForEntity(submissionUrl, submissionRequest, String.class);
            System.out.println("Submission Response: " + submissionResponse.getBody());
            System.out.println("--- Challenge Completed Successfully ---");
        } catch (Exception e) {
            System.err.println("Error during query submission: " + e.getMessage());
        }
    }
}