package com.agent.test.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.agent.test.steps",
        plugin = {"pretty", "html:target/cucumber-reports.html"}
)
public class TestRunner {
}
