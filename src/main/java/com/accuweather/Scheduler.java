package com.accuweather;

import com.accuweather.config.City;
import com.accuweather.config.Settings;
import com.accuweather.models.WeatherData;
import com.accuweather.pages.WeatherPage;
import com.accuweather.utils.BrowserSetup;
import com.accuweather.utils.GoogleSheetsClient;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Scheduler {

    private static final Logger log = LoggerFactory.getLogger(Scheduler.class);

    public static void main(String[] args) {
        if (args.length > 0 && "--now".equals(args[0])) {
            log.info("Chay thu thap ngay lap tuc (--now)");
            runDailyJob();
        } else {
            scheduleDailyJob();
        }
    }

    private static void runDailyJob() {
        log.info("=== Bat dau thu thap du lieu hang ngay ===");
        WebDriver driver = null;
        try {
            driver = BrowserSetup.createDriver();
            WeatherPage page = new WeatherPage(driver);
            GoogleSheetsClient sheets = new GoogleSheetsClient();

            List<WeatherData> results = new ArrayList<>();
            for (City city : Settings.CITIES) {
                try {
                    page.openCityWeather(city);
                    WeatherData data = page.scrapeWeather(city);
                    results.add(data);
                    log.info("OK {}: {} - {}", city.name(), data.getTemperature(), data.getDescription());
                } catch (Exception e) {
                    log.error("LOI {}: {}", city.name(), e.getMessage());
                }
            }

            if (!results.isEmpty()) {
                sheets.appendMany(results);
                log.info("Da ghi {} ban ghi", results.size());
            }
        } catch (Exception e) {
            log.error("Thu thap hang ngay that bai: {}", e.getMessage(), e);
        } finally {
            if (driver != null) driver.quit();
        }
        log.info("=== Ket thuc thu thap du lieu hang ngay ===");
    }

    private static void scheduleDailyJob() {
        log.info("Len lich chay hang ngay luc {}", Settings.SCHEDULE_TIME);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            String now = java.time.LocalTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            if (now.equals(Settings.SCHEDULE_TIME)) {
                runDailyJob();
            }
        }, 0, 60, TimeUnit.SECONDS);

        log.info("Bo lap lich dang chay. Nhan Ctrl+C de dung.");
    }
}
