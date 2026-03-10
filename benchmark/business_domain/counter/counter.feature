Feature: Page Hit Counter
  As a web page visitor
  I want to see how many times the page has been accessed
  So that I can track how popular the page is

  Background:
    Given the counter web application is running

  # ---------------------------------------------------------------------------
  # Page loading
  # ---------------------------------------------------------------------------

  Scenario: Counter page loads successfully
    When I visit the counter page
    Then the page should render without errors
    And the page body should not be empty

  Scenario: Counter page displays the hit-count sentence
    When I visit the counter page
    Then the page should contain text matching "This page has been accessed N time(s)."

  Scenario: Counter shows a positive integer on first visit
    When I visit the counter page
    Then the displayed access count should be a positive integer greater than zero

  # ---------------------------------------------------------------------------
  # Counter increment behavior
  # ---------------------------------------------------------------------------

  Scenario: Counter increments by exactly 1 on the next visit
    Given I visit the counter page and record the current count as N
    When I visit the counter page again
    Then the displayed count should be N + 1

  Scenario: Counter increments monotonically across multiple consecutive visits
    Given I record the count from three consecutive page visits
    Then each subsequent count should be exactly 1 more than the previous

  # ---------------------------------------------------------------------------
  # Error-free page
  # ---------------------------------------------------------------------------

  Scenario: Counter page contains no Java exception text
    When I visit the counter page
    Then the page should not contain "Exception"
    And the page should not contain "at java."

  Scenario: Counter page contains no HTTP 500 error indicator
    When I visit the counter page
    Then the page should not contain "500"
    And the page should not contain "Internal Server Error"

  # ---------------------------------------------------------------------------
  # Content integrity
  # ---------------------------------------------------------------------------

  Scenario: Counter text format is exactly correct
    When I visit the counter page
    Then the page should match the pattern "This page has been accessed \d+ time\(s\)\."

  Scenario: Counter value is always a non-negative integer
    When I visit the counter page multiple times
    Then every recorded count should be a non-negative integer
