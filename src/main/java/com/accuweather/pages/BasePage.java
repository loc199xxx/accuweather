package com.accuweather.pages;

import com.accuweather.config.Settings;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BasePage {

    protected static final Logger log = LoggerFactory.getLogger(BasePage.class);
    protected WebDriver driver;

    public BasePage(WebDriver driver) {
        this.driver = driver;
    }

    public void navigateTo(String url) {
        log.info("Dang mo trang: {}", url);
        driver.get(url);
        waitForPageLoad();
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public WebElement waitFor(By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(Settings.TIMEOUT_SEC))
                .until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public boolean isVisible(By locator, int timeoutSec) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSec))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void waitForPageLoad() {
        new WebDriverWait(driver, Duration.ofSeconds(Settings.TIMEOUT_SEC)).until(d ->
                ((JavascriptExecutor) d).executeScript("return document.readyState")
                        .toString().matches("complete|interactive"));
    }

    public void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public String takeScreenshot(String name) {
        try {
            Files.createDirectories(Settings.REPORTS_DIR);
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File dest = Settings.REPORTS_DIR.resolve(name + ".png").toFile();
            Files.copy(src.toPath(), dest.toPath());
            log.info("Da luu anh chup: {}", dest);
            return dest.getPath();
        } catch (Exception e) {
            log.error("Chup anh that bai: {}", e.getMessage());
            return null;
        }
    }

    public String snapshotOnFailure(String testName) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String safe = testName.replaceAll("[/:\\\\]", "_");
        String base = "FAILED__" + safe + "__" + ts;

        String shotPath = takeScreenshot(base);

        try {
            File html = Settings.REPORTS_DIR.resolve(base + ".html").toFile();
            try (FileWriter w = new FileWriter(html)) { w.write(driver.getPageSource()); }
            log.error("Da luu HTML loi: {}", html);
        } catch (Exception e) { log.error("Luu HTML that bai: {}", e.getMessage()); }

        return shotPath;
    }

    public void dismissAllPopups() {
        dismissCookieBanner();
        dismissGoogleAds();
    }

    private void dismissCookieBanner() {
        String[] xpaths = {
                "//button[contains(text(),'Accept')]",
                "//button[contains(text(),'I Understand')]",
                "//button[contains(text(),'I Accept')]",
                "//button[contains(text(),'Agree')]",
                "//*[@data-testid='accept-btn']"
        };
        for (String xp : xpaths) {
            try {
                List<WebElement> btns = driver.findElements(By.xpath(xp));
                if (!btns.isEmpty() && btns.get(0).isDisplayed()) {
                    btns.get(0).click();
                    log.info("Da dong banner cookie: {}", xp);
                    sleep(1000);
                    return;
                }
            } catch (Exception ignored) {}
        }
        log.info("Khong phat hien banner cookie");
    }

    private void dismissGoogleAds() {
        try {
            List<WebElement> iframes = driver.findElements(
                    By.cssSelector("iframe[name*='google_ads_iframe'][name*='interstitial']"));
            for (WebElement iframe : iframes) {
                if (!iframe.isDisplayed()) continue;
                driver.switchTo().frame(iframe);
                for (String sel : List.of("#dismiss-button", "[aria-label='Close']", ".close-button")) {
                    try {
                        List<WebElement> btns = driver.findElements(By.cssSelector(sel));
                        if (!btns.isEmpty() && btns.get(0).isDisplayed()) {
                            btns.get(0).click();
                            log.info("Da dong quang cao Google");
                            driver.switchTo().defaultContent();
                            sleep(1000);
                            return;
                        }
                    } catch (Exception ignored) {}
                }
                driver.switchTo().defaultContent();
            }
        } catch (Exception ignored) {}

        try { new Actions(driver).sendKeys(Keys.ESCAPE).perform(); sleep(500); }
        catch (Exception ignored) {}

        try {
            ((JavascriptExecutor) driver).executeScript(
                "document.querySelectorAll('iframe').forEach(function(f){f.remove()});");
        } catch (Exception ignored) {}
    }
}
