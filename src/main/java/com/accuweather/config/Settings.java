package com.accuweather.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class Settings {

    private Settings() {}

    public static final City HCM   = new City("Ho Chi Minh City", "353981", "ho-chi-minh-city");
    public static final City HANOI = new City("Hanoi",            "353412", "hanoi");
    public static final List<City> CITIES = List.of(HCM, HANOI);

    public static final String BASE_URL   = "https://www.accuweather.com";
    public static final String SEARCH_URL = BASE_URL + "/en/";

    public static final boolean HEADLESS    = Boolean.parseBoolean(System.getProperty("headless", "false"));
    public static final int     TIMEOUT_SEC = Integer.parseInt(System.getProperty("timeout", "60"));

    public static final Path   GOOGLE_CREDS     = Paths.get("config", "google_credentials.json");
    public static final String SPREADSHEET_ID   = "16S8HDulq5r-4d9n8HCOPaKwK_29zo_XTWsbkGMrQaro";
    public static final String WORKSHEET        = "weather";

    public static final Path REPORTS_DIR = Paths.get("reports");

    public static final String SCHEDULE_TIME = "14:59";
}
