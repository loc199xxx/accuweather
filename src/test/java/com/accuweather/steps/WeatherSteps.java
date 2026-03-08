package com.accuweather.steps;

import com.accuweather.config.City;
import com.accuweather.config.Settings;
import com.accuweather.hooks.CucumberHooks;
import com.accuweather.models.WeatherData;
import com.accuweather.pages.WeatherPage;
import io.cucumber.java.en.*;

import static org.testng.Assert.*;

public class WeatherSteps {

    private WeatherPage weatherPage;
    private WeatherData scrapedData;

    private WeatherPage page() {
        if (weatherPage == null) weatherPage = new WeatherPage(CucumberHooks.getDriver());
        return weatherPage;
    }

    private City findCity(String name) {
        return Settings.CITIES.stream()
                .filter(c -> c.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay thanh pho: " + name));
    }

    @Given("mo trang thoi tiet cho {string}")
    public void mo_trang_thoi_tiet(String cityName) {
        page().openCityWeather(findCity(cityName));
    }

    @When("thu thap du lieu thoi tiet cho {string}")
    public void thu_thap_du_lieu(String cityName) {
        scrapedData = page().scrapeWeather(findCity(cityName));
    }

    @Then("tieu de trang phai chua {string}")
    public void tieu_de_phai_chua(String cityName) {
        page().assertPageLoaded(findCity(cityName));
    }

    @Then("nhiet do phai duoc hien thi")
    public void nhiet_do_phai_hien_thi() {
        page().assertTemperatureDisplayed();
    }

    @Then("du lieu phai co thanh pho {string}")
    public void du_lieu_phai_co_thanh_pho(String cityName) {
        assertNotNull(scrapedData, "Du lieu thoi tiet bi null");
        assertEquals(scrapedData.getCity(), cityName);
    }

    @Then("nhiet do khong duoc trong")
    public void nhiet_do_khong_trong() {
        assertNotNull(scrapedData.getTemperature());
        assertNotEquals(scrapedData.getTemperature(), "N/A", "Nhiet do khong duoc la N/A");
        assertTrue(scrapedData.getTemperature().contains("\u00b0"), "Nhiet do phai chua ky hieu do");
    }

    @Then("thoi gian khong duoc trong")
    public void thoi_gian_khong_trong() {
        assertNotNull(scrapedData.getTimestamp());
        assertFalse(scrapedData.getTimestamp().isEmpty());
    }

    @Then("mo ta khong duoc trong")
    public void mo_ta_khong_trong() {
        assertNotNull(scrapedData.getDescription());
        assertNotEquals(scrapedData.getDescription(), "N/A", "Mo ta khong duoc la N/A");
    }
}