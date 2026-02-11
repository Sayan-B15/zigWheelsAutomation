Feature: ZigWheels New Bikes and Used Cars Identification

  Background:
    Given User is on ZigWheels Home Page

  Scenario: Identify Upcoming Honda Bikes under 4Lac
    When User identifies upcoming Honda bikes under 4Lac
    Then The bike details should be displayed and stored

  Scenario: Extract Used Cars in Chennai
    When User extracts used cars in Chennai
    Then Display the list of popular models

  Scenario: Verify Google Login Error Message
    When User attempts to login with Google using invalid details
    Then Capture and display the error message