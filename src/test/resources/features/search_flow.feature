@smoke @debug
Feature: Luong tim kiem thanh pho

  Scenario Outline: Tim kiem <city> va kiem tra URL
    Given mo trang tim kiem
    When tim kiem "<city>"
    And chon ket qua tim kiem dau tien
    Then URL phai chua "<slug>"

    Examples:
      | city             | slug             |
      | Ho Chi Minh City | ho-chi-minh-city |
      | Hanoi            | hanoi            |
