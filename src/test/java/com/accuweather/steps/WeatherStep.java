package com.accuweather.steps;

import com.accuweather.config.City;
import com.accuweather.config.Settings;
import com.accuweather.hooks.CucumberHooks;
import com.accuweather.models.WeatherData;
import com.accuweather.pages.WeatherPage;
import io.cucumber.java.en.*;

import static org.testng.Assert.*;

/**
 * Step definitions demo sử dụng WeatherPage1
 * – Bao phủ toàn bộ dữ liệu chi tiết mà WeatherPage1 scrape được
 */
public class WeatherStep {

    private WeatherPage weatherPage;
    private WeatherData scrapedData;

    // ── Helper ───────────────────────────────────────────────────

    private WeatherPage page() {
        if (weatherPage == null) {
            weatherPage = new WeatherPage(CucumberHooks.getDriver());
        }
        return weatherPage;
    }

    private City findCity(String name) {
        return Settings.CITIES.stream()
                .filter(c -> c.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay thanh pho: " + name));
    }

    // ── GIVEN ────────────────────────────────────────────────────

    @Given("nguoi dung mo trang thoi tiet cua {string}")
    public void nguoi_dung_mo_trang_thoi_tiet(String cityName) {
        City city = findCity(cityName);
        page().navigateTo(city.currentWeatherUrl());
    }

    // ── WHEN ─────────────────────────────────────────────────────

    @When("nguoi dung thu thap du lieu thoi tiet cua {string}")
    public void nguoi_dung_thu_thap_du_lieu(String cityName) {
        scrapedData = page().scrapeWeather(findCity(cityName));
        assertNotNull(scrapedData, "scrapeWeather() khong duoc tra ve null");
    }

    // ── THEN – Trang & thong tin co ban ──────────────────────────

    @Then("trang phai tai dung cho thanh pho {string}")
    public void trang_tai_dung(String cityName) {
        page().assertPageLoaded(findCity(cityName));
    }

    @Then("nhiet do hien thi phai hop le")
    public void nhiet_do_hop_le() {
        page().assertTemperatureDisplayed();
    }

    @Then("du lieu phai thuoc thanh pho {string}")
    public void du_lieu_thuoc_thanh_pho(String cityName) {
        assertNotNull(scrapedData, "Du lieu thoi tiet bi null");
        assertEquals(scrapedData.getCity(), cityName,
                "Ten thanh pho trong du lieu khong khop");
    }

    @Then("nhiet do phai co gia tri")
    public void nhiet_do_co_gia_tri() {
        String temp = scrapedData.getTemperature();
        assertNotNull(temp, "Nhiet do bi null");
        assertNotEquals(temp, "N/A", "Nhiet do khong duoc la N/A");
        assertTrue(temp.contains("\u00b0"),
                "Nhiet do phai chua ky hieu do (\u00b0), thuc te: " + temp);
    }

    @Then("mo ta thoi tiet phai co gia tri")
    public void mo_ta_co_gia_tri() {
        String desc = scrapedData.getDescription();
        assertNotNull(desc, "Mo ta bi null");
        assertNotEquals(desc, "N/A", "Mo ta khong duoc la N/A");
        assertFalse(desc.isEmpty(), "Mo ta khong duoc rong");
    }

    @Then("thoi gian thu thap phai co gia tri")
    public void thoi_gian_co_gia_tri() {
        String ts = scrapedData.getTimestamp();
        assertNotNull(ts, "Timestamp bi null");
        assertFalse(ts.isEmpty(), "Timestamp khong duoc rong");
    }

    // ── THEN – Gio ──────────────────────────────────────────────

    @Then("toc do gio phai co gia tri")
    public void toc_do_gio() {
        assertFieldNotEmpty(scrapedData.getWind(), "Wind");
    }

    @Then("gio giat phai co gia tri")
    public void gio_giat() {
        assertFieldNotEmpty(scrapedData.getWindGusts(), "Wind Gusts");
    }

    // ── THEN – Do am & diem suong ───────────────────────────────

    @Then("do am phai co gia tri")
    public void do_am() {
        assertFieldNotEmpty(scrapedData.getHumidity(), "Humidity");
    }

    @Then("diem suong phai co gia tri")
    public void diem_suong() {
        assertFieldNotEmpty(scrapedData.getDewPoint(), "Dew Point");
    }

    // ── THEN – Ap suat & tam nhin ───────────────────────────────

    @Then("ap suat phai co gia tri")
    public void ap_suat() {
        assertFieldNotEmpty(scrapedData.getPressure(), "Pressure");
    }

    @Then("tam nhin phai co gia tri")
    public void tam_nhin() {
        assertFieldNotEmpty(scrapedData.getVisibility(), "Visibility");
    }

    // ── THEN – May & chat luong khong khi ───────────────────────

    @Then("do phu may phai co gia tri")
    public void do_phu_may() {
        assertFieldNotEmpty(scrapedData.getCloudCover(), "Cloud Cover");
    }

    @Then("chat luong khong khi phai co gia tri")
    public void chat_luong_khong_khi() {
        assertFieldNotEmpty(scrapedData.getAirQuality(), "Air Quality");
    }

    // ── THEN – RealFeel ─────────────────────────────────────────

    @Then("nhiet do cam nhan RealFeel phai co gia tri")
    public void realfeel() {
        assertFieldNotEmpty(scrapedData.getRealfeel(), "RealFeel");
    }

    // ── Private utility ─────────────────────────────────────────

    private void assertFieldNotEmpty(String value, String fieldName) {
        assertNotNull(value, fieldName + " bi null");
        assertNotEquals(value, "N/A",
                fieldName + " khong duoc la N/A");
        assertFalse(value.trim().isEmpty(),
                fieldName + " khong duoc rong");
    }
}
