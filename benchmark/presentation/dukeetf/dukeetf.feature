Feature: Duke ETF Async Servlet with Long-Polling Price Updates
  As an investor
  I want to receive real-time ETF price and volume updates
  So that I can monitor Duke ETF performance via long-polling HTTP connections

  Background:
    Given the Duke ETF application is running
    And the PriceVolumeBean is generating updates every second

  # ---------------------------------------------------------------------------
  # Long-polling endpoint
  # ---------------------------------------------------------------------------

  Scenario: Connect to the ETF update endpoint
    When I send a GET request to /dukeetf
    Then the request should be put into async mode
    And the response should be held until an update is available

  Scenario: Receive a price/volume update
    When I connect to /dukeetf and wait for an update
    Then the response should contain a price and volume in format "X.XX / NNNN"

  Scenario: Response content type is text/html
    When I send a GET request to /dukeetf
    Then the response Content-Type should be "text/html"

  # ---------------------------------------------------------------------------
  # Price and volume updates
  # ---------------------------------------------------------------------------

  Scenario: Price starts near 100.0
    When the application first starts
    Then the initial price should be approximately 100.0

  Scenario: Volume starts near 300000
    When the application first starts
    Then the initial volume should be approximately 300000

  Scenario: Price fluctuates randomly within a range
    When multiple updates are sent
    Then the price should change by at most 0.50 per update

  Scenario: Volume fluctuates randomly
    When multiple updates are sent
    Then the volume should change by at most 2500 per update

  # ---------------------------------------------------------------------------
  # Async servlet behavior
  # ---------------------------------------------------------------------------

  Scenario: Multiple clients can connect simultaneously
    When 3 clients connect to /dukeetf
    Then all 3 should receive the same update when it fires

  Scenario: Connection is completed after sending an update
    When a client receives an update
    Then the async context should be completed
    And the client should reconnect for the next update

  Scenario: Client removal on connection close
    When a connected client closes the connection
    Then the client should be removed from the request queue
    And the server log should contain "Connection closed"

  Scenario: Client removal on timeout
    When a connected client times out
    Then the client should be removed from the request queue
    And the server log should contain "Connection timeout"

  Scenario: Client removal on error
    When a connection error occurs
    Then the client should be removed from the request queue
    And the server log should contain "Connection error"

  # ---------------------------------------------------------------------------
  # Timer-driven updates
  # ---------------------------------------------------------------------------

  Scenario: Updates are generated every second via timer service
    Then the PriceVolumeBean should create an interval timer of 1000ms
    And the timeout method should fire every second

  Scenario: Updates are only sent when a servlet is registered
    Given no servlet has registered with PriceVolumeBean
    When the timer fires
    Then no update should be sent
