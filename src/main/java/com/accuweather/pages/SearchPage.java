package com.accuweather.pages;

import com.accuweather.config.Settings;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
// import java.util.List;

public class SearchPage extends BasePage {

    private final By searchInputLocator = By.xpath("//form[@class='search-form']/input");
    // private final By firstResultLocator = By.xpath("//div[@class='locations-list content-module']/a[1]/p");
    private final By firstResultLocator = By.xpath("//*[@class='results-container']/div[1]/p[1]");

    public SearchPage(WebDriver driver) {
        super(driver);
    }

    public SearchPage open() {
        navigateTo(Settings.SEARCH_URL);
        dismissAllPopups();
        return this;
    }

    public SearchPage searchCity(String cityName) {
        log.info("Dang tim kiem thanh pho: {}", cityName);
        WebElement box = findSearchInput();
        box.click();
        box.clear();
        box.sendKeys(cityName);
        // enter();
        return this;
    }

    public void selectFirstResult() {
        log.info("Dang click chon ket qua tim kiem dau tien...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement firstResult = wait.until(ExpectedConditions.elementToBeClickable(firstResultLocator));
        try {
            // log.info("Tim thay ket qua, dang chuyen den: {}", firstResult.getAttribute("href"));
            firstResult.click();
        } catch (ElementClickInterceptedException e) {
            log.warn("Phan tu bi che khuat! Dang goi ham xoa Ads va thu lai...");
            // 1. Dọn rác quảng cáo
            dismissGoogleAds2();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", firstResult);
            log.info("Ep click JS thanh cong!");
        }
    }
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    private void enter() {
        Actions actions = new Actions(driver);
        actions.sendKeys(Keys.RETURN).perform();
    }

    private WebElement findSearchInput() {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(searchInputLocator));
    }   
    public void verifyUrlContainsKeyword(String expectedKeyword) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            wait.until(ExpectedConditions.urlContains(expectedKeyword));
            log.info("URL da thay doi va chua tu khoa: {}", expectedKeyword);
        } catch (TimeoutException e) {
            log.error("Sau 10 giay, URL van chua thay doi hoac khong chua tu khoa '{}'. URL hien tai: {}", 
                      expectedKeyword, driver.getCurrentUrl());
            throw e;
        }
    }
}
