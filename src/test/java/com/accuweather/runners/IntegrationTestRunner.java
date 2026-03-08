package com.accuweather.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * TestNG + Cucumber runner for @integration tests.
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.accuweather.steps", "com.accuweather.hooks"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/integration.html",
                "json:target/cucumber-reports/integration.json"
        },
        tags = "@integration"
)
public class IntegrationTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
