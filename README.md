# UI Test Generator Agent

An autonomous Java-based agent that automatically generates comprehensive UI test suites using a local Large Language Model (LLM).

Given a basic feature description (and optionally a target URL), this agent orchestrates the creation of:
1. **Cucumber `.feature` files** written in Gherkin syntax.
2. **Selenium Page Object Model (POM)** Java classes complete with `@FindBy` locators.
3. **Cucumber Step Definitions** that bind the feature file to the POM actions.

## 🚀 Prerequisites

1. **Java Development Kit (JDK) 17+**
2. **Ollama**: We use Ollama for fast, local, and private LLM inference.
   - Download and install [Ollama](https://ollama.com/).
   - Open your terminal and run the following command to download and start the model:
     ```bash
     ollama run llama3
     ```
   *Make sure Ollama is running (`http://localhost:11434`) before starting the agent!*

## 🛠️ Tech Stack

- **Language:** Java
- **Build Tool:** Gradle
- **Testing Frameworks:** JUnit 4, Cucumber, Selenium WebDriver
- **LLM Integration:** Java `HttpClient` & Jackson (JSON parsing)

## 🎯 How to Use

The core generation logic is located in `src/main/java/com/agent/test/agent/UITestGeneratorAgent.java`.

### Basic Execution

You can run the agent directly from your IDE by executing the `main` method in `UITestGeneratorAgent.java`, or via the terminal using Gradle:

```bash
./gradlew build -x test
```

By default, the `main` method triggers the generation pipeline with a mock feature description:
_"As a user, I want to login to the e-commerce website with username 'demo' and password 'password' so that I can view my dashboard."_

### Fetching Live HTML Context

To ensure the LLM generates 100% accurate CSS selectors and Element IDs, you can pass a live `targetUrl` to the agent. The agent will fetch the live DOM and feed it to the LLM during the Page Object generation step.

```java
public static void main(String[] args) {
    UITestGeneratorAgent agent = new UITestGeneratorAgent();
    // Replace null with your target URL to provide live DOM context!
    agent.generateSuite("As a user, I want to login...", "https://example.com/login");
}
```

## 📁 Generated Outputs

Once the agent completes its run, you will find the generated files materialized in your project structure:

- `src/test/resources/features/generated.feature` - The generated BDD scenarios.
- `src/main/java/com/agent/test/pages/GeneratedPage.java` - The Page Object extending `BasePage`.
- `src/test/java/com/agent/test/steps/GeneratedSteps.java` - The runnable Cucumber steps.

You can then execute the generated test suite using:
```bash
./gradlew test
```

## 🧠 Customization

You can fully customize the agent's behavior by modifying the system prompts inside `UITestGeneratorAgent.java` to enforce your specific coding standards, custom wait logic, or specific testing assertions.
