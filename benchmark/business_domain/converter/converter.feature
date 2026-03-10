Feature: Currency Conversion
  As a user
  I want to convert US dollars to Japanese Yen and Euros
  So that I can understand the equivalent value in other currencies

  # Conversion rates used by the application:
  #   1 USD  = 104.34 JPY
  #   1 JPY  = 0.007 EUR

  Background:
    Given the converter web application is running
    And I am on the converter home page

  # ---------------------------------------------------------------------------
  # Page structure
  # ---------------------------------------------------------------------------

  Scenario: Converter home page displays the input form
    Then I should see the label "Enter a dollar amount to convert:"
    And I should see an amount input field with title "Amount"
    And I should see a "Submit" button
    And the page title should not be empty

  Scenario: Form has a visible amount input field
    Then the element with title "Amount" should be visible on the page

  Scenario: Form has a visible Submit button
    Then a button named "Submit" should be visible on the page

  # ---------------------------------------------------------------------------
  # Correct conversion results
  # ---------------------------------------------------------------------------

  Scenario: Convert 1 dollar
    When I enter "1" in the amount field and submit
    Then the result page should contain "104.34 yen"
    And the result page should contain "0.74 Euro"

  Scenario: Convert 5 dollars
    When I enter "5" in the amount field and submit
    Then the result page should contain "521.70 yen"
    And the result page should contain "3.66 Euro"

  Scenario: Convert 10 dollars
    When I enter "10" in the amount field and submit
    Then the result page should contain "1043.40 yen"
    And the result page should contain "7.31 Euro"

  Scenario: Convert 50 dollars
    When I enter "50" in the amount field and submit
    Then the result page should contain "5217.00 yen"
    And the result page should contain "36.52 Euro"

  Scenario: Convert 100 dollars
    When I enter "100" in the amount field and submit
    Then the result page should contain "10434.00 yen"
    And the result page should contain "73.04 Euro"

  Scenario Outline: Multiple dollar amounts produce correct Yen conversions
    When I enter "<dollars>" in the amount field and submit
    Then the result page should contain "<yen> yen"

    Examples:
      | dollars | yen      |
      | 1       | 104.34   |
      | 5       | 521.70   |
      | 10      | 1043.40  |

  # ---------------------------------------------------------------------------
  # Result page content
  # ---------------------------------------------------------------------------

  Scenario: Result page echoes the submitted amount
    When I enter "5" in the amount field and submit
    Then the result page should contain the text "5"

  Scenario: Result page contains no Java stack traces
    When I enter "5" in the amount field and submit
    Then the result should not contain "at java."
    And the result should not contain "Exception"

  Scenario: Result page title is non-empty after conversion
    When I enter "5" in the amount field and submit
    Then the page title should not be empty

  # ---------------------------------------------------------------------------
  # Navigation
  # ---------------------------------------------------------------------------

  Scenario: Can convert twice in a row by returning to the form
    When I enter "5" in the amount field and submit
    And I navigate back to the converter home page
    Then I should see the label "Enter a dollar amount to convert:"
    When I enter "1" in the amount field and submit
    Then the result page should contain "104.34 yen"

  # ---------------------------------------------------------------------------
  # Edge cases - no server errors
  # ---------------------------------------------------------------------------

  Scenario: Zero dollar amount produces no server error
    When I enter "0" in the amount field and submit
    Then the result should not contain "500"
    And the result should not contain "Exception"

  Scenario: Large dollar amount produces no server error
    When I enter "1000000" in the amount field and submit
    Then the result should not contain "500"
    And the result should not contain "Exception"

  Scenario: Empty submission produces no server error
    When I submit the form with an empty amount field
    Then the result should not contain "500"
    And the result should not contain "Exception"

  Scenario: Decimal amount produces no server error
    When I enter "2.5" in the amount field and submit
    Then the result should not contain "500"
    And the result should not contain "Exception"

  Scenario: Negative amount produces no server error
    When I enter "-1" in the amount field and submit
    Then the result should not contain "500"
    And the result should not contain "Exception"
