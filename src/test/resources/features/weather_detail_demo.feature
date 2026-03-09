@demo
Feature: Thu thap va kiem tra chi tiet thoi tiet

  # ── Scenario 1: Kiem tra trang tai thanh cong ──
  Scenario Outline: Trang thoi tiet tai dung cho <city>
    Given nguoi dung mo trang thoi tiet cua "<city>"
    Then trang phai tai dung cho thanh pho "<city>"
    And nhiet do hien thi phai hop le

    Examples:
      | city             |
      | Ho Chi Minh City |
      | Hanoi            |

  # ── Scenario 2: Kiem tra thu thap du lieu thoi tiet ──
  Scenario Outline: Thu thap du lieu thoi tiet cho <city>
    Given nguoi dung mo trang thoi tiet cua "<city>"
    When  nguoi dung thu thap du lieu thoi tiet cua "<city>"
    Then  nhiet do hien thi phai hop le

    Examples:
      | city             |
      | Ho Chi Minh City |
      | Hanoi            |