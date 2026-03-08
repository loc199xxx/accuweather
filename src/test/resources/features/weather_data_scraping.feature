@smoke
Feature: Thu thap du lieu thoi tiet

  Scenario Outline: Thu thap du lieu thoi tiet cho <city>
    Given mo trang thoi tiet cho "<city>"
    When thu thap du lieu thoi tiet cho "<city>"
    Then du lieu phai co thanh pho "<city>"
    And nhiet do khong duoc trong
    And thoi gian khong duoc trong
    And mo ta khong duoc trong

    Examples:
      | city             |
      | Ho Chi Minh City |
      | Hanoi            |
