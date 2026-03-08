package com.accuweather.utils;

import com.accuweather.config.Settings;
import io.github.bonigarcia.wdm.WebDriverManager;
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
                "--start-maximized",
                "--window-size=1280,720"
        );
        opts.setExperimentalOption("excludeSwitches", java.util.List.of("enable-automation"));

        if (Settings.HEADLESS) {
            opts.addArguments("--headless=new");
            log.info("Dang chay che do headless");
        }

        WebDriver driver = new ChromeDriver(opts);
        driver.manage().window().maximize();

        log.info("Da tao Chrome driver");
        return driver;
    }
}
