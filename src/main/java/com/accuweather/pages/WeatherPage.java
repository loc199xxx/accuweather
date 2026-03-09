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

    private final WebDriverWait wait;
    private final By temperatureLocator = By.xpath("//div[@class='display-temp']");
    private final By descriptionLocator = By.xpath("//div[contains(@class,'current-weather-info')]/following-sibling::div[@class='phrase']");

    // Locators do bạn cung cấp (dùng XPath chuẩn xác)
    private final By windValueLocator = By.xpath("//div[text()='Wind']/following-sibling::div");
    private final By windGustsValueLocator = By.xpath("//div[text()='Wind Gusts']/following-sibling::div");
    private final By humidityValueLocator = By.xpath("//div[text()='Humidity']/following-sibling::div");
    // private final By indoorHumidityValueLocator = By.xpath("//div[text()='Indoor Humidity']/following-sibling::div");
    private final By dewPointValueLocator = By.xpath("//div[text()='Dew Point']/following-sibling::div");
    private final By pressureValueLocator = By.xpath("//div[text()='Pressure']/following-sibling::div");
    private final By cloudCoverValueLocator = By.xpath("//div[text()='Cloud Cover']/following-sibling::div");
    private final By visibilityValueLocator = By.xpath("//div[text()='Visibility']/following-sibling::div");
    // private final By cloudCeilingValueLocator = By.xpath("//div[text()='Cloud Ceiling']/following-sibling::div");
    private final By airQualityValueLocator = By.xpath("//div[text()='Air Quality']/following-sibling::div");
    // private final By maxUVIndexValueLocator = By.xpath("//div[text()='Max UV Index']/following-sibling::div");
    private final By realFeelValueLocator = By.xpath("//div[text()='RealFeel\u00ae']/following-sibling::div");
    public WeatherPage(WebDriver driver) {
        super(driver);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    public WeatherData scrapeWeather(City city) {
        log.info("Đang thu thập dữ liệu thời tiết: {}", city.name());
        waitForWeatherContent();

        WeatherData d = new WeatherData();
        d.setCity(city.name());
        d.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // Thu thập thông tin chính
        d.setTemperature(getSafeText(temperatureLocator, "Temperature"));
        d.setDescription(getSafeText(descriptionLocator, "Description"));
        
        // Thu thập thông tin chi tiết (sử dụng XPath chính xác)
        d.setRealfeel(getSafeText(realFeelValueLocator, "RealFeel"));
        d.setWind(getSafeText(windValueLocator, "Wind"));
        d.setWindGusts(getSafeText(windGustsValueLocator, "Wind Gusts"));
        d.setHumidity(getSafeText(humidityValueLocator, "Humidity"));
        d.setDewPoint(getSafeText(dewPointValueLocator, "Dew Point"));
        d.setPressure(getSafeText(pressureValueLocator, "Pressure"));
        d.setCloudCover(getSafeText(cloudCoverValueLocator, "Cloud Cover"));
        d.setVisibility(getSafeText(visibilityValueLocator, "Visibility"));
        d.setAirQuality(getSafeText(airQualityValueLocator, "Air Quality"));

        log.info("Hoàn tất thu thập -> {} | Nhiệt độ: {} | {}", d.getCity(), d.getTemperature(), d.getDescription());
        return d;
    }

    public void openCityWeather(City city) {
        navigateTo(city.currentWeatherUrl());
        dismissAllPopups();
        waitForWeatherContent();
    }
    public void assertPageLoaded(City city) {
        try {
            wait.until(ExpectedConditions.urlContains("weather"));
        } catch (Exception ignored) {}

        String titleLower = driver.getTitle().toLowerCase();
        String slug = city.slug().replace("-", " ");
        
        if (!titleLower.contains(slug) && !titleLower.contains(city.name().toLowerCase())) {
            throw new AssertionError("Tiêu đề trang không chứa tên thành phố: " + city.name());
        }
        log.info("OK - Trang đã tải đúng cho thành phố: {}", city.name());
    }

    public void assertTemperatureDisplayed() {
        String temp = getSafeText(temperatureLocator, "Temperature Check");
        assert !"N/A".equals(temp) && temp.contains("\u00b0") 
               : "Nhiệt độ hiển thị không hợp lệ: " + temp;
        log.info("OK - Nhiệt độ hiển thị rõ ràng: {}", temp);
    }

    // ==========================================
    // 5. HELPER / UTILITY METHODS
    // ==========================================

    /**
     * Hàm lấy text an toàn, chống crash (Flaky Test)
     */
    private String getSafeText(By locator, String fieldName) {
        try {
            // Chờ tối đa 5s cho mỗi element xuất hiện
            WebElement element = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            
            String value = element.getText().trim();
            log.info("Trích xuất thành công '{}' -> {}", fieldName, value);
            return value;
            
        } catch (Exception e) {
            log.warn("Không tìm thấy giá trị cho '{}' (Locator: {})", fieldName, locator.toString());
            return "N/A";
        }
    }

    /**
     * Chờ trang web load xong các thành phần thời tiết
     */
    private void waitForWeatherContent() {
        try {
            // Cuộn trang để kích hoạt Lazy Load
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, 400)");
            Thread.sleep(1000);
            js.executeScript("window.scrollTo(0, 0)");
            
            // Chờ cụm nhiệt độ chính xuất hiện để đảm bảo trang đã sẵn sàng
            wait.until(ExpectedConditions.presenceOfElementLocated(temperatureLocator));
            log.info("Nội dung thời tiết đã tải xong và sẵn sàng để cào.");
        } catch (Exception e) {
            log.warn("Lỗi trong quá trình chờ load trang thời tiết: {}", e.getMessage());
        }
    }
}

    