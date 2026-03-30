package com.agent.test.agent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UITestGeneratorAgent {
    private final LlmClient client;

    public UITestGeneratorAgent() {
        this.client = new LlmClient();
    }

    public void generateSuite(String featureDescription, String targetUrl) {
        System.out.println("Starting generation for feature: " + featureDescription);

        String htmlContext = "";
        if (targetUrl != null && !targetUrl.isEmpty()) {
            System.out.println("Fetching HTML context from URL: " + targetUrl);
            htmlContext = fetchHtmlFromUrl(targetUrl);
            
            // To prevent massive contexts overwhelming the local LLM window, optionally truncate
            if (htmlContext.length() > 50000) {
                htmlContext = htmlContext.substring(0, 50000) + "... [TRUNCATED]";
            }
        }

        // 1. Generate Feature File
        String featureSystemPrompt = "You are a QA automation engineer. Given a user story, write a concise Cucumber BDD .feature file in Gherkin syntax. Only output the feature file text contained in a markdown code block (```gherkin ... ```).";
        System.out.println("Generating Feature File...");
        String featureOutput = client.generateCompletion(featureDescription, featureSystemPrompt);
        String featureCode = CodeExtractorUtil.extractCodeBlock(featureOutput, "gherkin");
        saveFile("src/test/resources/features/", "generated.feature", featureCode);

        // 2. Generate Page Object Model Class
        String pomSystemPrompt = "You are a Java Selenium expert. Given a user story and this feature file:\n" + featureCode + 
                (htmlContext.isEmpty() ? "" : "\nAnd the following HTML context of the target web page:\n" + htmlContext + "\n") +
                "\nWrite a Selenium Page Object Model (POM) Java class named 'GeneratedPage' that extends 'com.agent.test.pages.BasePage'. " +
                "Include WebElements using @FindBy and methods for actions. Base your CSS selectors or IDs on the provided HTML if available. " +
                "Use 'package com.agent.test.pages;' and import org.openqa.selenium.support.FindBy; etc. " +
                "Only output the Java code enclosed in a markdown block (```java ... ```).";
        System.out.println("Generating POM...");
        String pomOutput = client.generateCompletion(featureDescription, pomSystemPrompt);
        String pomCode = CodeExtractorUtil.extractCodeBlock(pomOutput, "java");
        saveFile("src/main/java/com/agent/test/pages/", "GeneratedPage.java", pomCode);

        // 3. Generate Step Definitions
        String stepsSystemPrompt = "You are a Java Cucumber test engineer. Given a user story, the feature file:\n" + featureCode + 
                "\nAnd the Page Object Model:\n" + pomCode + 
                "\nWrite the Cucumber step definitions class named 'GeneratedSteps'. " +
                "Instantiate GeneratedPage and use its methods to fulfill the Given/When/Then steps. " +
                "Use 'package com.agent.test.steps;'. Include the necessary Cucumber annotations imports like io.cucumber.java.en.Given. " +
                "Only output the Java code enclosed in a markdown block (```java ... ```).";
        System.out.println("Generating Steps...");
        String stepsOutput = client.generateCompletion(featureDescription, stepsSystemPrompt);
        String stepsCode = CodeExtractorUtil.extractCodeBlock(stepsOutput, "java");
        saveFile("src/test/java/com/agent/test/steps/", "GeneratedSteps.java", stepsCode);

        System.out.println("Successfully generated the UI Test Suite!");
    }

    private void saveFile(String dirPath, String fileName, String content) {
        try {
            Path dir = Paths.get(dirPath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path fullPath = dir.resolve(fileName);
            Files.writeString(fullPath, content);
            System.out.println("Saved file: " + fullPath.toString());
        } catch (IOException e) {
            System.err.println("Failed to write to " + fileName);
            e.printStackTrace();
        }
    }
    private String fetchHtmlFromUrl(String targetUrl) {
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            System.err.println("Failed to fetch HTML from " + targetUrl + ": " + e.getMessage());
            return "";
        }
    }

    public static void main(String[] args) {
        UITestGeneratorAgent agent = new UITestGeneratorAgent();
        // Pass a real URL replacing null if you want the LLM to read the DOM for precise element IDs!
        agent.generateSuite("As a user, I want to login to the e-commerce website with username 'demo' and password 'password' so that I can view my dashboard.", null);
    }
}
