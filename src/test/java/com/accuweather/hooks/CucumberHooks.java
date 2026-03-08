package com.accuweather.hooks;

import com.accuweather.pages.BasePage;
import com.accuweather.utils.BrowserSetup;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CucumberHooks {

    private static final Logger log = LoggerFactory.getLogger(CucumberHooks.class);
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return DRIVER.get();
    }

    @Before
    public void setUp(Scenario scenario) {
        log.info("Bat dau: {}", scenario.getName());
        WebDriver driver = BrowserSetup.createDriver();
        DRIVER.set(driver);
    }

    @After
    public void tearDown(Scenario scenario) {
        WebDriver driver = DRIVER.get();
        if (driver == null) return;

        try {
            if (scenario.isFailed()) {
                log.error("THAT BAI: {}", scenario.getName());
                byte[] shot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(shot, "image/png", "anh-chup-loi");
                new BasePage(driver).snapshotOnFailure(scenario.getName());
            } else {
                log.info("THANH CONG: {}", scenario.getName());
            }
        } finally {
            driver.quit();
            DRIVER.remove();
            log.info("Da dong trinh duyet");
        }
    }
}
