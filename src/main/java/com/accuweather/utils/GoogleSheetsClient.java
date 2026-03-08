package com.accuweather.utils;

import com.accuweather.config.Settings;
import com.accuweather.models.WeatherData;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class GoogleSheetsClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleSheetsClient.class);

    private Sheets sheetsService;
    private String spreadsheetId;
    private final String worksheetName;
    private boolean available;

    public GoogleSheetsClient() {
        this.worksheetName = Settings.WORKSHEET;
        this.available = false;
        init();
    }

    private void init() {
        File creds = Settings.GOOGLE_CREDS.toFile();
        if (!creds.exists()) {
            log.warn("Khong tim thay thong tin xac thuc Google tai '{}' - dung CSV thay the", Settings.GOOGLE_CREDS);
            return;
        }
        try {
            GoogleCredentials credential = GoogleCredentials
                    .fromStream(new FileInputStream(creds))
                    .createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS));

            sheetsService = new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credential))
                    .setApplicationName("AccuWeather Scraper")
                    .build();

            spreadsheetId = findSpreadsheet();
            ensureWorksheet();
            available = true;
            log.info("Da ket noi Google Sheets: {}", spreadsheetId);
        } catch (Exception e) {
            log.error("Khoi tao Google Sheets that bai: {} - dung CSV thay the", e.getMessage());
        }
    }

    public void appendWeather(WeatherData data) {
        appendMany(List.of(data));
    }

    public void appendMany(List<WeatherData> dataList) {
        if (dataList.isEmpty()) return;

        ensureHeaders();

        List<List<Object>> rows = new ArrayList<>();
        for (WeatherData d : dataList) rows.add(new ArrayList<Object>(d.toRow()));

        if (available) {
            try {
                ValueRange body = new ValueRange().setValues(rows);
                sheetsService.spreadsheets().values()
                        .append(spreadsheetId, worksheetName + "!A1", body)
                        .setValueInputOption("RAW")
                        .setInsertDataOption("INSERT_ROWS")
                        .execute();
                log.info("Da ghi {} dong vao Google Sheets", rows.size());
            } catch (Exception e) {
                log.error("Ghi Google Sheets that bai: {} - dung CSV thay the", e.getMessage());
                writeCsv(convertRowsToString(rows));
            }
        } else {
            writeCsv(convertRowsToString(rows));
        }
    }

    public List<Map<String, String>> getAllRecords() {
        if (!available) { log.warn("Google Sheets khong kha dung"); return Collections.emptyList(); }
        try {
            ValueRange result = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, worksheetName + "!A:Z").execute();
            List<List<Object>> values = result.getValues();
            if (values == null || values.size() < 2) return Collections.emptyList();

            List<String> headers = values.get(0).stream().map(Object::toString).toList();
            List<Map<String, String>> records = new ArrayList<>();
            for (int i = 1; i < values.size(); i++) {
                Map<String, String> row = new LinkedHashMap<>();
                for (int j = 0; j < headers.size() && j < values.get(i).size(); j++) {
                    row.put(headers.get(j), values.get(i).get(j).toString());
                }
                records.add(row);
            }
            return records;
        } catch (Exception e) {
            log.error("Lay toan bo ban ghi that bai: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean isAvailable() { return available; }

    private List<List<String>> convertRowsToString(List<List<Object>> rows) {
        List<List<String>> result = new ArrayList<>();
        for (List<Object> row : rows) {
            List<String> strRow = new ArrayList<>();
            for (Object o : row) strRow.add(o == null ? "" : o.toString());
            result.add(strRow);
        }
        return result;
    }

    private String findSpreadsheet() throws Exception {
        log.info("Dang su dung spreadsheet: {}", Settings.SPREADSHEET_ID);
        return Settings.SPREADSHEET_ID;
    }

    private void ensureWorksheet() {
        try {
            Spreadsheet meta = sheetsService.spreadsheets()
                    .get(spreadsheetId).execute();
            boolean exists = meta.getSheets().stream()
                    .anyMatch(s -> s.getProperties().getTitle().equals(worksheetName));
            if (!exists) {
                sheetsService.spreadsheets().batchUpdate(spreadsheetId,
                        new BatchUpdateSpreadsheetRequest().setRequests(List.of(
                                new Request().setAddSheet(new AddSheetRequest()
                                        .setProperties(new SheetProperties().setTitle(worksheetName))))))
                        .execute();
                log.info("Da tao worksheet '{}'", worksheetName);
            }
        } catch (Exception e) {
            log.warn("Dam bao worksheet: {}", e.getMessage());
        }
    }

    private void ensureHeaders() {
        if (!available) return;
        try {
            ValueRange r = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, worksheetName + "!A1:Z1").execute();
            if (r.getValues() == null || r.getValues().isEmpty()) {
                List<Object> headers = new ArrayList<>(WeatherData.headers());
                sheetsService.spreadsheets().values()
                        .update(spreadsheetId, worksheetName + "!A1",
                                new ValueRange().setValues(List.of(headers)))
                        .setValueInputOption("RAW").execute();
            }
        } catch (Exception e) { log.warn("Dam bao tieu de: {}", e.getMessage()); }
    }

    private void writeCsv(List<List<String>> rows) {
        try {
            Files.createDirectories(Settings.REPORTS_DIR);
            File csv = Settings.REPORTS_DIR.resolve("weather_data.csv").toFile();
            boolean exists = csv.exists();
            try (CSVWriter writer = new CSVWriter(new FileWriter(csv, true))) {
                if (!exists) writer.writeNext(WeatherData.headers().toArray(String[]::new));
                for (List<String> row : rows) {
                    writer.writeNext(row.toArray(new String[0]));
                }
            }
            log.info("Da ghi CSV: {}", csv);
        } catch (Exception e) {
            log.error("Ghi CSV that bai: {}", e.getMessage());
        }
    }
}
