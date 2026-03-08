@integration
Feature: Tich hop Google Sheets

  Scenario: Thu thap du lieu va ghi vao Google Sheets
    Given thu thap du lieu thoi tiet cho tat ca thanh pho
    When ghi du lieu vao Google Sheets
    Then Google Sheets phai chua du lieu thoi tiet
