Feature: Timer Session with Programmatic and Automatic Timeouts
  As a user
  I want to create timers that fire at specified intervals
  So that I can observe both programmatic and automatic timeout events

  Background:
    Given the timer session application is running

  # ---------------------------------------------------------------------------
  # Timer management page
  # ---------------------------------------------------------------------------

  Scenario: Timer page displays the set timer controls
    When I view the timer management page
    Then I should see an interval input field for programmatic timer duration
    And I should see a Set Timer button
    And I should see the last programmatic timeout display
    And I should see the last automatic timeout display

  # ---------------------------------------------------------------------------
  # Programmatic timer
  # ---------------------------------------------------------------------------

  Scenario: Create a programmatic timer with default 8-second interval
    When I set a programmatic timer with interval 8000 milliseconds
    Then the timer should be created successfully
    And the server log should contain "Setting a programmatic timeout for 8000 milliseconds"

  Scenario: Programmatic timer fires after the specified interval
    When I set a programmatic timer with interval 8000 milliseconds
    And I wait for the timer to fire
    Then the last programmatic timeout should display a timestamp
    And the server log should contain "Programmatic timeout occurred"

  Scenario: Programmatic timer displays "never" before first timeout
    Given no programmatic timer has been set
    When I view the timer page
    Then the last programmatic timeout should display "never"

  Scenario: Programmatic timer updates the last timeout timestamp
    When I set a programmatic timer and it fires
    Then the last programmatic timeout should no longer be "never"
    And it should display a valid date/time string

  Scenario: Programmatic timer is a single-action timer
    When I set a programmatic timer with interval 5000 milliseconds
    And the timer fires once
    Then it should not fire again automatically

  # ---------------------------------------------------------------------------
  # Automatic timer
  # ---------------------------------------------------------------------------

  Scenario: Automatic timer fires every 1 minute
    Given the application has been running for at least 1 minute
    Then the automatic timer should have fired
    And the last automatic timeout should display a timestamp

  Scenario: Automatic timer displays "never" before first firing
    Given the application just started
    When I view the timer page before 1 minute has elapsed
    Then the last automatic timeout should display "never"

  Scenario: Automatic timer logs each firing
    When the automatic timer fires
    Then the server log should contain "Automatic timeout occurred"

  Scenario: Automatic timer is scheduled with @Schedule annotation
    Then the automatic timer should be configured with minute="*/1" and hour="*"
    And the timer should be non-persistent

  # ---------------------------------------------------------------------------
  # Timer configuration
  # ---------------------------------------------------------------------------

  Scenario: Programmatic timer is non-persistent
    When I create a programmatic timer
    Then the TimerConfig should have persistent set to false

  Scenario: Programmatic timer carries info message
    When I create a programmatic timer
    Then the TimerConfig info should be "Created new programmatic timer"

  # ---------------------------------------------------------------------------
  # Multiple programmatic timers
  # ---------------------------------------------------------------------------

  Scenario: Setting a new programmatic timer creates an additional timer
    When I set a programmatic timer with interval 5000
    And I set another programmatic timer with interval 10000
    Then both timers should eventually fire
