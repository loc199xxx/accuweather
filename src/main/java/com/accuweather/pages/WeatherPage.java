package com.accuweather.pages;

import com.accuweather.config.City;
import com.accuweather.models.WeatherData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
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
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//*[contains(text(),'Current Weather')]")));
            sleep(2000);
            log.info("Noi dung thoi tiet da tai xong");
        } catch (Exception e) {
            log.warn("Khong tim thay 'Current Weather' - cho them");
            sleep(5000);
            dismissAllPopups();
            sleep(3000);
        }
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
        try {
            String body = bodyText();
            int idx = body.indexOf("Current Weather");
            if (idx >= 0) {
                Matcher m = Pattern.compile("(-?\\d+\u00b0[CF]?)").matcher(body.substring(idx, Math.min(idx + 200, body.length())));
                if (m.find()) { log.info("Nhiet do: {}", m.group(1)); return m.group(1); }
            }
        } catch (Exception ignored) {}

        try {
            String txt = driver.findElement(By.cssSelector(".temp")).getText().trim();
            if (txt.contains("\u00b0")) { log.info("Nhiet do (css): {}", txt); return txt; }
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
        try {
            String body = bodyText();
            int idx = body.indexOf("Current Weather");
            if (idx >= 0) {
                String[] lines = body.substring(idx, Math.min(idx + 300, body.length())).split("\n");
                for (int i = 0; i < lines.length; i++) {
                    String ln = lines[i].trim();
                    if (ln.matches("^-?\\d+\u00b0.*") && i + 1 < lines.length) {
                        String desc = lines[i + 1].trim();
                        if (!desc.matches("^[\\d\u00b0].*") && desc.length() < 80 && !desc.isEmpty()) {
                            log.info("Mo ta: {}", desc);
                            return desc;
                        }
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
        String title = getTitle().toLowerCase();
        String slug = city.slug().replace("-", " ");
        assert title.contains(slug) || title.contains(city.name().toLowerCase())
                : "Tieu de phai chua '" + city.name() + "', nhung nhan duoc: '" + title + "'";
        log.info("OK - Trang da tai cho {}", city.name());
    }

    public void assertTemperatureDisplayed() {
        String temp = getTemperature();
        assert !"N/A".equals(temp) && temp.contains("\u00b0")
                : "Nhiet do phai chua ky hieu '\u00b0', nhung nhan duoc: '" + temp + "'";
        log.info("OK - Nhiet do hien thi: {}", temp);
    }
}
