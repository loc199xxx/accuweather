package com.accuweather.pages;

import com.accuweather.config.City;
import com.accuweather.models.WeatherData;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherPage extends BasePage {

    private static final Set<String> KNOWN_LABELS = Set.of(
            "Wind","Wind Gusts","Humidity","Indoor Humidity","Dew Point",
            "Pressure","Cloud Cover","Visibility","Cloud Ceiling",
            "Air Quality","Max UV Index","RealFeel\u00ae","RealFeel Shade\u2122");

    public WeatherPage(WebDriver driver) {
        super(driver);
    }

    public void openCityWeather(City city) {
        navigateTo(city.currentWeatherUrl());
        dismissAllPopups();
        waitForWeatherContent();
    }

    private void waitForWeatherContent() {
        // Wait until we are on a weather page, not redirected to homepage
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
                    d.getCurrentUrl().contains("current-weather")
                    || d.getTitle().toLowerCase().contains("weather"));
        } catch (Exception e) {
            log.warn("Co the bi chuyen huong: URL={} Title={}", driver.getCurrentUrl(), driver.getTitle());
        }

        // Scroll to trigger lazy-loaded content
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, 400)");
            sleep(1500);
            js.executeScript("window.scrollTo(0, 0)");
            sleep(500);
        } catch (Exception ignored) {}

        dismissAllPopups();

        // Try CSS selectors for weather content
        String[] selectors = {
            ".temp-container", "[class*='CurrentConditions']",
            "[class*='current-weather']", "[data-testid*='temperature']",
            ".weather-container .temp", ".temp"
        };
        for (String sel : selectors) {
            try {
                new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(sel)));
                log.info("Noi dung thoi tiet da tai xong ({})", sel);
                sleep(1000);
                return;
            } catch (Exception ignored) {}
        }

        // Fallback: wait for any degree sign in body text
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(d ->
                    d.findElement(By.tagName("body")).getText().contains("\u00b0"));
            log.info("Tim thay ky hieu nhiet do trong body");
        } catch (Exception e) {
            log.warn("Khong tim thay noi dung thoi tiet sau khi cho - trang co the bi block");
        }
        sleep(2000);
    }

    public WeatherData scrapeWeather(City city) {
        log.info("Dang thu thap du lieu thoi tiet: {}", city.name());
        logPageSnapshot();

        WeatherData d = new WeatherData();
        d.setCity(city.name());
        d.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        d.setTemperature(getTemperature());
        d.setRealfeel(getRealfeel());
        d.setDescription(getDescription());
        d.setWind(getDetail("Wind"));
        d.setWindGusts(getDetail("Wind Gusts"));
        d.setHumidity(getDetail("Humidity"));
        d.setDewPoint(getDetail("Dew Point"));
        d.setPressure(getDetail("Pressure"));
        d.setCloudCover(getDetail("Cloud Cover"));
        d.setVisibility(getDetail("Visibility"));
        d.setAirQuality(getDetail("Air Quality"));

        log.info("Da thu thap -> {} | Nhiet do: {} | {}", d.getCity(), d.getTemperature(), d.getDescription());
        return d;
    }

    private String bodyText() {
        return driver.findElement(By.tagName("body")).getText();
    }

    private void logPageSnapshot() {
        try {
            String body = bodyText();
            int idx = body.indexOf("Current Weather");
            if (idx >= 0) {
                String snippet = body.substring(idx, Math.min(idx + 500, body.length())).replace("\n", " | ");
                log.info("Trich doan trang: {}", snippet.substring(0, Math.min(300, snippet.length())));
            } else {
                log.warn("Khong tim thay 'Current Weather' trong body");
            }
        } catch (Exception ignored) {}
    }

    public String getTemperature() {
        // Try CSS selectors first (more reliable than text parsing)
        String[] tempSelectors = {
            ".temp-container .temp", "[class*='CurrentConditions'] .temp",
            "[class*='current-weather'] .temp", ".weather-module .temp",
            "[data-testid*='temperature']", "[class*='Temperature'] .temp",
            ".temp"
        };
        for (String sel : tempSelectors) {
            try {
                List<WebElement> els = driver.findElements(By.cssSelector(sel));
                if (!els.isEmpty()) {
                    String txt = els.get(0).getText().trim();
                    if (txt.contains("\u00b0")) { log.info("Nhiet do (css {}): {}", sel, txt); return txt; }
                }
            } catch (Exception ignored) {}
        }

        // Fallback: regex on full body text
        try {
            String body = bodyText();
            Matcher m = Pattern.compile("(-?\\d+\\u00b0[CF]?)").matcher(body);
            if (m.find()) { log.info("Nhiet do (regex): {}", m.group(1)); return m.group(1); }
        } catch (Exception ignored) {}

        log.warn("Khong lay duoc nhiet do");
        return "N/A";
    }

    public String getRealfeel() {
        try {
            Matcher m = Pattern.compile("RealFeel[\u00ae\u2122]?\\s*(-?\\d+\u00b0[CF]?)").matcher(bodyText());
            if (m.find()) { log.info("RealFeel: {}", m.group(1)); return m.group(1); }
        } catch (Exception e) { log.warn("Lay RealFeel that bai: {}", e.getMessage()); }
        return "N/A";
    }

    public String getDescription() {
        // Try CSS selectors first
        String[] descSelectors = {
            "[class*='CurrentConditions'] [class*='phrase']",
            "[class*='CurrentConditions'] [class*='Phrase']",
            "[data-testid*='phrase']", "[data-testid*='Phrase']",
            "[class*='condition-phrase']", "[class*='weather-phrase']",
            "[class*='Phrase']", ".phrase"
        };
        for (String sel : descSelectors) {
            try {
                List<WebElement> els = driver.findElements(By.cssSelector(sel));
                if (!els.isEmpty()) {
                    String txt = els.get(0).getText().trim();
                    if (!txt.isEmpty() && txt.length() < 100 && !txt.matches("^-?\\d+.*")) {
                        log.info("Mo ta (css {}): {}", sel, txt);
                        return txt;
                    }
                }
            } catch (Exception ignored) {}
        }

        // Fallback: find a non-numeric, non-label line after a temperature line in body
        try {
            String body = bodyText();
            String[] lines = body.split("\n");
            for (int i = 0; i < lines.length - 1; i++) {
                String ln = lines[i].trim();
                if (ln.matches("^-?\\d+\u00b0.*")) {
                    String next = lines[i + 1].trim();
                    if (!next.isEmpty() && !next.matches("^[\\d\u00b0+-].*")
                            && next.length() < 80 && !KNOWN_LABELS.contains(next)) {
                        log.info("Mo ta (body fallback): {}", next);
                        return next;
                    }
                }
            }
        } catch (Exception ignored) {}

        log.warn("Khong lay duoc mo ta");
        return "N/A";
    }

    public String getDetail(String label) {
        try {
            List<String> lines = Arrays.stream(bodyText().split("\n"))
                    .map(String::trim).filter(l -> !l.isEmpty()).toList();

            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).equals(label) && i + 1 < lines.size()) {
                    String val = lines.get(i + 1);
                    if (KNOWN_LABELS.contains(val)) continue;
                    if ("Wind".equals(label) && i > 0 && lines.get(i - 1).contains("Gusts")) continue;
                    log.info("Chi tiet '{}' -> {}", label, val);
                    return val;
                }
            }
        } catch (Exception e) { log.warn("Loi lay chi tiet '{}': {}", label, e.getMessage()); }
        log.warn("Khong tim thay chi tiet '{}'", label);
        return "N/A";
    }

    public void assertPageLoaded(City city) {
        // Wait for title to contain city name
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
                String t = d.getTitle().toLowerCase();
                return t.contains(city.name().toLowerCase())
                    || t.contains(city.slug().replace("-", " "));
            });
        } catch (Exception ignored) {}

        String title = getTitle();
        String titleLower = title.toLowerCase();
        String slug = city.slug().replace("-", " ");
        if (!titleLower.contains(slug) && !titleLower.contains(city.name().toLowerCase())) {
            throw new AssertionError(
                "Tieu de phai chua '" + city.name() + "', nhung nhan duoc: '" + title + "'");
        }
        log.info("OK - Trang da tai cho {}", city.name());
    }

    public void assertTemperatureDisplayed() {
        String temp = getTemperature();
        assert !"N/A".equals(temp) && temp.contains("\u00b0")
                : "Nhiet do phai chua ky hieu '\u00b0', nhung nhan duoc: '" + temp + "'";
        log.info("OK - Nhiet do hien thi: {}", temp);
    }
}
