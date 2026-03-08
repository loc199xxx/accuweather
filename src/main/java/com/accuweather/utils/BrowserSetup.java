package com.accuweather.utils;

import com.accuweather.config.Settings;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowserSetup {

    private static final Logger log = LoggerFactory.getLogger(BrowserSetup.class);

    private BrowserSetup() {}

    public static WebDriver createDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions opts = new ChromeOptions();

        opts.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-blink-features=AutomationControlled",
                "--disable-infobars",
                "--disable-extensions",
                "--disable-popup-blocking",
                "--window-size=1920,1080",
                "--lang=en-US,en",
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36"
        );
        opts.setExperimentalOption("excludeSwitches", java.util.List.of("enable-automation"));
        opts.setExperimentalOption("useAutomationExtension", false);

        if (Settings.HEADLESS) {
            opts.addArguments("--headless=new");
            log.info("Dang chay che do headless");
        }

        WebDriver driver = new ChromeDriver(opts);
        driver.manage().window().maximize();

        // Remove navigator.webdriver flag to bypass bot detection
        try {
            ((JavascriptExecutor) driver).executeScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        } catch (Exception ignored) {}

        log.info("Da tao Chrome driver");
        return driver;
    }
}
