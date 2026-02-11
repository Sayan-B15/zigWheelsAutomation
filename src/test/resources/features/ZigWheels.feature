Feature: ZigWheels Automation - Royal Enfield and Used Cars Identification

  Background:
    Given User is on ZigWheels Home Page

  Scenario: Identify Upcoming Royal Enfield Bikes under 4Lac
    When User identifies upcoming Royal Enfield bikes under 4Lac
    Then The bike details should be displayed and stored in Excel

  Scenario: Extract Used Cars in Chennai
    When User extracts all popular used cars in Chennai
    Then Display the list of popular models and store in Excel

  Scenario: Verify Google Login Error Message
    When User attempts to login with Google using invalid details
    Then Capture and display "Couldn’t sign you in" error message