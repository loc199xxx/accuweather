package com.accuweather.steps;

import com.accuweather.config.City;
import com.accuweather.config.Settings;
import com.accuweather.hooks.CucumberHooks;
import com.accuweather.models.WeatherData;
import com.accuweather.pages.WeatherPage;
import com.accuweather.utils.GoogleSheetsClient;
import io.cucumber.java.en.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public class GoogleSheetsSteps {

    private final List<WeatherData> dataList = new ArrayList<>();
    private GoogleSheetsClient client;

    @Given("thu thap du lieu thoi tiet cho tat ca thanh pho")
    public void thu_thap_tat_ca() {
        WeatherPage page = new WeatherPage(CucumberHooks.getDriver());
        for (City city : Settings.CITIES) {
            page.openCityWeather(city);
            WeatherData data = page.scrapeWeather(city);
            dataList.add(data);
        }
        assertFalse(dataList.isEmpty(), "Khong thu thap duoc du lieu thoi tiet");
    }

    @When("ghi du lieu vao Google Sheets")
    public void ghi_du_lieu() {
        client = new GoogleSheetsClient();
        client.appendMany(dataList);
    }

    @Then("Google Sheets phai chua du lieu thoi tiet")
    public void kiem_tra_sheets() {
        if (!client.isAvailable()) {
            assertTrue(Settings.REPORTS_DIR.resolve("weather_data.csv").toFile().exists(),
                    "Tep CSV du phong phai ton tai");
            return;
        }
        List<Map<String, String>> records = client.getAllRecords();
        assertFalse(records.isEmpty(), "Google Sheets phai chua du lieu");
        for (WeatherData d : dataList) {
            boolean found = records.stream()
                    .anyMatch(r -> d.getCity().equals(r.get("City")));
            assertTrue(found, "Thanh pho " + d.getCity() + " phai co trong Sheets");
        }
    }
}