package com.accuweather.models;

import java.util.List;

public class WeatherData {

    private String city            = "";
    private String timestamp       = "";
    private String temperature     = "";
    private String realfeel        = "";
    private String description     = "";
    private String wind            = "";
    private String windGusts       = "";
    private String humidity        = "";
    private String dewPoint        = "";
    private String pressure        = "";
    private String cloudCover      = "";
    private String visibility      = "";
    private String airQuality      = "";

    // ── headers / toRow  (same order as Python) ──────────────────

    public static List<String> headers() {
        return List.of("City","Timestamp","Temperature","RealFeel",
                "Description","Wind","Wind Gusts","Humidity",
                "Dew Point","Pressure","Cloud Cover","Visibility","Air Quality");
    }

    public List<String> toRow() {
        return List.of(city, timestamp, temperature, realfeel,
                description, wind, windGusts, humidity,
                dewPoint, pressure, cloudCover, visibility, airQuality);
    }

    // ── Getters / Setters ────────────────────────────────────────

    public String getCity()            { return city; }
    public String getTimestamp()       { return timestamp; }
    public String getTemperature()     { return temperature; }
    public String getRealfeel()        { return realfeel; }
    public String getDescription()     { return description; }
    public String getWind()            { return wind; }
    public String getWindGusts()       { return windGusts; }
    public String getHumidity()        { return humidity; }
    public String getDewPoint()        { return dewPoint; }
    public String getPressure()        { return pressure; }
    public String getCloudCover()      { return cloudCover; }
    public String getVisibility()      { return visibility; }
    public String getAirQuality()      { return airQuality; }

    public void setCity(String v)            { city = v; }
    public void setTimestamp(String v)       { timestamp = v; }
    public void setTemperature(String v)     { temperature = v; }
    public void setRealfeel(String v)        { realfeel = v; }
    public void setDescription(String v)     { description = v; }
    public void setWind(String v)            { wind = v; }
    public void setWindGusts(String v)       { windGusts = v; }
    public void setHumidity(String v)        { humidity = v; }
    public void setDewPoint(String v)        { dewPoint = v; }
    public void setPressure(String v)        { pressure = v; }
    public void setCloudCover(String v)      { cloudCover = v; }
    public void setVisibility(String v)      { visibility = v; }
    public void setAirQuality(String v)      { airQuality = v; }

    @Override
    public String toString() {
        return city + " | " + temperature + " | " + description;
    }
}
