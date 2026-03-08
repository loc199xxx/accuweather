package com.accuweather.pages;

import com.accuweather.config.Settings;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class SearchPage extends BasePage {

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
        enter();
        sleep(2000);
        return this;
    }

    public void selectFirstResult() {
        log.info("Dang chon ket qua tim kiem dau tien");
        String[] selectors = {
                ".search-results a",
                ".locations-list a",
                "a[href*='weather-forecast']",
                "a[href*='current-weather']"
        };
        for (String sel : selectors) {
            try {
                List<WebElement> links = new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(sel)));
                if (!links.isEmpty()) {
                    WebElement first = links.get(0);
                    log.info("Tim thay {} ket qua voi selector '{}', dang chuyen trang qua href", links.size(), sel);
                    String href = first.getAttribute("href");
                    if (href == null || href.isBlank()) {
                        List<WebElement> childLinks = first.findElements(By.tagName("a"));
                        if (!childLinks.isEmpty()) href = childLinks.get(0).getAttribute("href");
                    }
                    if (href != null && !href.isBlank()) {
                        log.info("Dang chuyen den: {}", href);
                        driver.get(href);
                    } else {
                        log.warn("Khong tim thay href, click truc tiep");
                        first.click();
                    }
                    waitForPageLoad();
                    return;
                }
            } catch (Exception ignored) {}
        }
        log.warn("Khong tim thay ket qua tim kiem de click");
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    private void enter() {
        Actions actions = new Actions(driver);
        actions.sendKeys(Keys.RETURN).perform();
    }

    private WebElement findSearchInput() {
        String[] css = {
                "#locationSearch",
                "#location-search-input",
                "input[id*='search' i]",
                "input[name='query']",
                "input[placeholder*='Search' i]",
                "input[placeholder*='City' i]",
                "input[placeholder*='Location' i]",
                "input[placeholder*='Address' i]",
                "input[aria-label*='Search' i]",
                "[data-testid*='search'] input",
                ".search-input input",
                "header input[type='text']",
                "nav input[type='text']",
                "form input[type='text']",
                "input[type='search']"
        };
        for (String sel : css) {
            try {
                List<WebElement> els = new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(sel)));
                for (WebElement el : els) {
                    if (el.isDisplayed()) return el;
                }
            } catch (Exception ignored) {}
        }
        // Last resort: any visible text input on the page
        List<WebElement> all = driver.findElements(By.xpath("//input[@type='text' or @type='search']"));
        for (WebElement el : all) {
            if (el.isDisplayed()) return el;
        }
        throw new NoSuchElementException("Khong tim thay o nhap tim kiem");
    }
}
