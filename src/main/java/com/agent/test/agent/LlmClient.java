package com.agent.test.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LlmClient {
    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL_NAME = "llama3";
    private final HttpClient client;
    private final ObjectMapper mapper;

    public LlmClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
    }

    public String generateCompletion(String prompt, String systemPrompt) {
        try {
            String fullPrompt = systemPrompt + "\n\nUser Context:\n" + prompt;
            
            String jsonPayload = mapper.writeValueAsString(new OllamaRequest(MODEL_NAME, fullPrompt, false));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofMinutes(5)) // LLM generation can take time
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode rootNode = mapper.readTree(response.body());
                return rootNode.path("response").asText();
            } else {
                throw new RuntimeException("Failed to call Ollama API. Status: " + response.statusCode() + " Body: " + response.body());
            }

        } catch (Exception e) {
            System.err.println("Error generating LLM completion: " + e.getMessage());
            return "";
        }
    }

    private static class OllamaRequest {
        public String model;
        public String prompt;
        public boolean stream;

        public OllamaRequest(String model, String prompt, boolean stream) {
            this.model = model;
            this.prompt = prompt;
            this.stream = stream;
        }
    }
}
