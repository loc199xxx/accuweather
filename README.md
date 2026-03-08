# AccuWeather Cucumber + TestNG

Dự án Java tự động hóa scraping dữ liệu thời tiết từ AccuWeather, sử dụng **Cucumber + TestNG + Selenium**.

> Clone từ dự án Python gốc (`Accuwather-nvp-main`) – ánh xạ 1:1 các chức năng.

---

## Cấu trúc thư mục

```
accuweather-cucumber-testng/
├── pom.xml                          # Maven build (Selenium, Cucumber, TestNG)
├── testng.xml                       # Smoke test suite
├── testng-integration.xml           # Integration test suite
├── Dockerfile
├── docker-compose.yml
│
├── src/main/java/com/accuweather/
│   ├── config/
│   │   ├── City.java                # ← config/settings.py :: City
│   │   └── Settings.java            # ← config/settings.py :: constants
│   ├── models/
│   │   └── WeatherData.java         # ← pages/weather_page.py :: WeatherData
│   ├── pages/
│   │   ├── BasePage.java            # ← pages/base_page.py
│   │   ├── SearchPage.java          # ← pages/search_page.py
│   │   └── WeatherPage.java         # ← pages/weather_page.py
│   ├── utils/
│   │   ├── BrowserSetup.java        # ← utils/browser_setup.py
│   │   └── GoogleSheetsClient.java  # ← utils/google_sheets.py
│   └── Scheduler.java               # ← scheduler.py
│
├── src/test/java/com/accuweather/
│   ├── hooks/
│   │   └── CucumberHooks.java       # ← tests/conftest.py
│   ├── runners/
│   │   ├── TestRunner.java          # Smoke runner (not @integration)
│   │   └── IntegrationTestRunner.java
│   └── steps/
│       ├── WeatherSteps.java        # TC-01 + TC-02
│       ├── SearchSteps.java         # TC-03
│       ├── GoogleSheetsSteps.java   # TC-04
│       └── ScreenshotSteps.java     # TC-05
│
└── src/test/resources/
    ├── features/
    │   ├── weather_page_load.feature       # TC-01
    │   ├── weather_data_scraping.feature   # TC-02
    │   ├── search_flow.feature             # TC-03
    │   ├── google_sheets_integration.feature # TC-04
    │   └── screenshot.feature              # TC-05
    └── logback-test.xml
```

---

## Ánh xạ Python → Java

| Python                          | Java                          |
|---------------------------------|-------------------------------|
| `config/settings.py`           | `config/City.java` + `Settings.java` |
| `pages/base_page.py`           | `pages/BasePage.java`         |
| `pages/search_page.py`         | `pages/SearchPage.java`       |
| `pages/weather_page.py`        | `pages/WeatherPage.java` + `models/WeatherData.java` |
| `utils/browser_setup.py`       | `utils/BrowserSetup.java`     |
| `utils/google_sheets.py`       | `utils/GoogleSheetsClient.java` |
| `utils/logger.py`              | SLF4J + Logback (built-in)    |
| `tests/conftest.py`            | `hooks/CucumberHooks.java`    |
| `tests/test_weather_scraper.py`| 5 feature files + 4 step defs |
| `scheduler.py`                 | `Scheduler.java`              |
| `pytest.ini`                   | `testng.xml`                  |
| `requirements.txt`             | `pom.xml`                     |

---

## Yêu cầu

- **Java 17+**
- **Maven 3.9+**
- **Google Chrome** (tự động tải chromedriver qua WebDriverManager)

---

## Chạy test

```bash
# Smoke tests (mặc định, loại @integration)
mvn clean test

# Integration tests (Google Sheets E2E)
mvn test -Dsurefire.suiteXmlFiles=testng-integration.xml

# Headless mode
mvn test -Dheadless=true

# Chạy scraper 1 lần
mvn exec:java -Dexec.mainClass=com.accuweather.Scheduler -Dexec.args="--now"

# Chạy scheduler hàng ngày
mvn exec:java -Dexec.mainClass=com.accuweather.Scheduler
```

---

## Docker

```bash
# Smoke tests
docker compose run test

# Integration tests
docker compose run test-e2e

# Scraper
docker compose run scraper

# Scheduler
docker compose up scheduler
```

---

## Báo cáo

- **Cucumber HTML**: `target/cucumber-reports/cucumber.html`
- **Screenshots (failure)**: `reports/FAILED__*.png`
- **CSV fallback**: `reports/weather_data.csv`
