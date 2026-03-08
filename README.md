# AccuWeather Cucumber + TestNG

Dự án Java tự động thu thập (scraping) dữ liệu thời tiết từ [AccuWeather](https://www.accuweather.com), sử dụng **Cucumber 7 + TestNG + Selenium 4**. Dữ liệu được ghi vào **Google Sheets** (hoặc CSV nếu không có credentials).

---

## Tính năng chính

- **5 kịch bản test** (Cucumber `.feature`) bao phủ: tải trang, scraping dữ liệu, tìm kiếm thành phố, tích hợp Google Sheets, chụp ảnh màn hình
- **Chế độ headless** (`-Dheadless=true`) với anti-bot: custom User-Agent, ẩn `navigator.webdriver`, vô hiệu AutomationControlled
- **Scheduler** chạy thu thập hàng ngày (lúc 14:59 mặc định)
- **CI/CD** qua GitHub Actions – chạy tự động mỗi ngày, upload báo cáo Cucumber
- **Docker** hỗ trợ chạy test và scraper trong container

---

## Yêu cầu

| Thành phần    | Phiên bản   |
|---------------|-------------|
| Java          | 17+         |
| Maven         | 3.9+        |
| Google Chrome | Tự động tải chromedriver qua WebDriverManager |

**Tùy chọn:** File `config/google_credentials.json` (Service Account JSON) để ghi dữ liệu vào Google Sheets. Nếu không có, dữ liệu sẽ ghi ra `reports/weather_data.csv`.

---

## Cấu trúc thư mục

```
accuweather-cucumber-testng/
├── pom.xml                            # Maven – Selenium 4.21, Cucumber 7.18, TestNG 7.10
├── testng.xml                         # Suite: tất cả kịch bản (smoke + screenshot)
├── testng-integration.xml             # Suite: chỉ @integration (Google Sheets E2E)
├── Dockerfile                         # Image: maven:3.9 + Chrome + headless
├── docker-compose.yml                 # Services: test, test-integration, scraper, scheduler
├── .github/workflows/daily-tests.yml  # CI: chạy smoke test hàng ngày
├── config/
│   └── google_credentials.json        # (không commit) Service Account JSON
├── reports/                           # Ảnh chụp lỗi + CSV fallback
│
├── src/main/java/com/accuweather/
│   ├── Scheduler.java                 # Entry point: --now hoặc chạy theo lịch
│   ├── config/
│   │   ├── City.java                  # Record(name, locationId, slug) + URL builder
│   │   └── Settings.java             # Hằng số: BASE_URL, CITIES, HEADLESS, TIMEOUT...
│   ├── models/
│   │   └── WeatherData.java          # POJO 13 trường thời tiết + headers()/toRow()
│   ├── pages/
│   │   ├── BasePage.java             # WebDriver wrapper: navigate, wait, screenshot, popup
│   │   ├── SearchPage.java           # Tìm kiếm thành phố: 15+ CSS selectors fallback
│   │   └── WeatherPage.java          # Scrape thời tiết: CSS selectors → regex fallback
│   └── utils/
│       ├── BrowserSetup.java         # Chrome options + anti-bot (User-Agent, webdriver flag)
│       └── GoogleSheetsClient.java   # Google Sheets API v4 + CSV fallback
│
├── src/test/java/com/accuweather/
│   ├── hooks/
│   │   └── CucumberHooks.java        # @Before/@After: tạo/đóng driver, chụp ảnh khi lỗi
│   ├── runners/
│   │   ├── TestRunner.java           # Chạy tất cả kịch bản
│   │   └── IntegrationTestRunner.java # Chỉ chạy @integration
│   └── steps/
│       ├── WeatherSteps.java         # TC-01 (tải trang) + TC-02 (scraping)
│       ├── SearchSteps.java          # TC-03 (tìm kiếm)
│       ├── GoogleSheetsSteps.java    # TC-04 (Google Sheets E2E)
│       └── ScreenshotSteps.java      # TC-05 (chụp ảnh)
│
└── src/test/resources/
    ├── logback-test.xml
    └── features/
        ├── weather_page_load.feature        # TC-01: Trang tải + hiển thị nhiệt độ
        ├── weather_data_scraping.feature    # TC-02: Scrape nhiệt độ, mô tả, chi tiết
        ├── search_flow.feature              # TC-03: Tìm kiếm → chọn kết quả → kiểm tra URL
        ├── google_sheets_integration.feature # TC-04: Thu thập → ghi Sheets → verify
        └── screenshot.feature               # TC-05: Chụp ảnh → kiểm tra file tồn tại
```

---

## Kịch bản test

| ID    | Feature file                      | Mô tả                                       | Tag            |
|-------|-----------------------------------|----------------------------------------------|----------------|
| TC-01 | `weather_page_load.feature`       | Mở trang thời tiết, kiểm tra tiêu đề + nhiệt độ | `@smoke`       |
| TC-02 | `weather_data_scraping.feature`   | Scrape dữ liệu: nhiệt độ, mô tả, timestamp  | `@smoke`       |
| TC-03 | `search_flow.feature`             | Tìm kiếm thành phố → chọn kết quả → kiểm tra URL | `@smoke @debug`|
| TC-04 | `google_sheets_integration.feature`| Thu thập tất cả thành phố → ghi Google Sheets | `@integration` |
| TC-05 | `screenshot.feature`              | Chụp ảnh màn hình → kiểm tra file `.png`      | `@smoke`       |

**Thành phố kiểm tra:** Ho Chi Minh City, Hanoi

---

## Chạy test

```bash
# Tất cả kịch bản (mặc định)
mvn clean test

# Headless mode (không mở trình duyệt)
mvn test -Dheadless=true

# Chỉ chạy integration test (cần google_credentials.json)
mvn test -Dsurefire.suiteXmlFiles=testng-integration.xml

# Thay đổi timeout (mặc định 60s)
mvn test -Dheadless=true -Dtimeout=30
```

---

## Scraper & Scheduler

```bash
# Thu thập dữ liệu ngay lập tức
mvn exec:java -Dexec.mainClass=com.accuweather.Scheduler -Dexec.args="--now" -Dheadless=true

# Chạy scheduler (thu thập hàng ngày lúc 14:59)
mvn exec:java -Dexec.mainClass=com.accuweather.Scheduler -Dheadless=true
```

---

## Docker

```bash
# Smoke tests
docker compose run test

# Integration tests (cần config/google_credentials.json)
docker compose run test-integration

# Scraper (thu thập 1 lần)
docker compose run scraper

# Scheduler (chạy nền, thu thập hàng ngày)
docker compose up scheduler
```

---

## GitHub Actions (CI/CD)

Workflow `.github/workflows/daily-tests.yml` chạy tự động:

- **Lịch:** mỗi ngày (cron) + chạy thủ công qua tab Actions
- **Môi trường:** Ubuntu + Java 17 (Temurin) + Google Chrome
- **Lệnh:** `mvn test -Dsurefire.suiteXmlFiles=testng.xml -Dheadless=true -B`
- **Artifacts:** Báo cáo Cucumber + ảnh chụp lỗi, lưu 30 ngày

**Setup secret:** Vào repo → Settings → Secrets → thêm `GOOGLE_CREDENTIALS_JSON` (nội dung file Service Account JSON).

---

## Dữ liệu thu thập

Mỗi lần scrape, 13 trường dữ liệu được thu thập cho mỗi thành phố:

| Trường        | Ví dụ              |
|---------------|---------------------|
| City          | Ho Chi Minh City    |
| Timestamp     | 2026-03-09 00:37:21 |
| Temperature   | 26°C                |
| RealFeel      | 28°                 |
| Description   | Mostly cloudy       |
| Wind          | SE 11 km/h          |
| Wind Gusts    | 15 km/h             |
| Humidity      | 74%                 |
| Dew Point     | 21° C               |
| Pressure      | ↑ 1012 mb           |
| Cloud Cover   | 75%                 |
| Visibility    | 16 km               |
| Air Quality   | Fair                |

---

## Báo cáo

| Loại                    | Đường dẫn                                  |
|-------------------------|---------------------------------------------|
| Cucumber HTML report    | `target/cucumber-reports/cucumber.html`     |
| Cucumber JSON report    | `target/cucumber-reports/cucumber.json`     |
| Integration HTML report | `target/cucumber-reports/integration.html`  |
| Ảnh chụp khi test lỗi  | `reports/FAILED__<tên>__<timestamp>.png`    |
| HTML page khi test lỗi  | `reports/FAILED__<tên>__<timestamp>.html`   |
| CSV fallback            | `reports/weather_data.csv`                  |

---

## Công nghệ

| Thư viện               | Phiên bản | Mục đích                    |
|------------------------|-----------|-----------------------------|
| Selenium               | 4.21.0    | Tự động hóa trình duyệt    |
| WebDriverManager       | 5.9.2     | Tự động tải chromedriver    |
| Cucumber Java          | 7.18.1    | BDD framework              |
| Cucumber TestNG        | 7.18.1    | Test runner                 |
| TestNG                 | 7.10.2    | Assertions + test suite     |
| Google Sheets API      | v4        | Ghi dữ liệu lên Sheets     |
| Google Auth            | 1.23.0    | Xác thực Service Account    |
| OpenCSV                | 5.9       | Ghi CSV fallback            |
| SLF4J + Log4j2         | 2.23.1    | Logging                     |

---

## Cấu hình

Các thông số cấu hình trong `Settings.java`:

| Biến                | Giá trị mặc định                          | Ghi đè                  |
|---------------------|-------------------------------------------|--------------------------|
| `HEADLESS`          | `false`                                   | `-Dheadless=true`        |
| `TIMEOUT_SEC`       | `60`                                      | `-Dtimeout=30`           |
| `BASE_URL`          | `https://www.accuweather.com`             | —                        |
| `GOOGLE_CREDS`      | `config/google_credentials.json`          | —                        |
| `SPREADSHEET_ID`    | `16S8HDulq5r-4d9n8HCOPaKwK_29zo_XTWsbkGMrQaro` | —               |
| `WORKSHEET`         | `weather`                                 | —                        |
| `SCHEDULE_TIME`     | `14:59`                                   | —                        |
