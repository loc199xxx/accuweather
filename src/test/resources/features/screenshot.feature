@smoke
Feature: Chup anh man hinh

  Scenario: Chup anh man hinh trang thoi tiet
    Given mo trang thoi tiet cho "Ho Chi Minh City"
    When chup anh man hinh voi ten "test_screenshot"
    Then tep anh chup phai ton tai
