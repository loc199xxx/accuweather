package com.accuweather.config;

public record City(String name, String locationId, String slug) {

    public String currentWeatherUrl() {
        return Settings.BASE_URL + "/en/vn/" + slug + "/" + locationId
                + "/current-weather/" + locationId;
    }

    public String dailyForecastUrl() {
        return Settings.BASE_URL + "/en/vn/" + slug + "/" + locationId
                + "/daily-weather-forecast/" + locationId;
    }
}
