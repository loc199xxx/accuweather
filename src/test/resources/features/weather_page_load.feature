@smoke
Feature: Tai trang thoi tiet

  Scenario Outline: Trang tai thanh cong va hien thi nhiet do cho <city>
    Given mo trang thoi tiet cho "<city>"
    Then tieu de trang phai chua "<city>"
    And nhiet do phai duoc hien thi

    Examples:
      | city             |
      | Ho Chi Minh City |
      | Hanoi            |
