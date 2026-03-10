Feature: Duke ETF2 WebSocket Price Updates
  As an investor
  I want to receive real-time ETF price and volume updates over WebSocket
  So that I can monitor Duke ETF performance with persistent bidirectional connections

  Background:
    Given the Duke ETF2 application is running
    And the PriceVolumeBean is generating updates every second

  # ---------------------------------------------------------------------------
  # WebSocket connection
  # ---------------------------------------------------------------------------

  Scenario: Connect to the WebSocket endpoint
    When I open a WebSocket connection to /dukeetf
    Then the connection should be established successfully
    And the server log should contain "Connection opened"

  Scenario: Close a WebSocket connection
    Given I have an open WebSocket connection
    When I close the connection
    Then the server log should contain "Connection closed"
    And the session should be removed from the queue

  # ---------------------------------------------------------------------------
  # Price and volume updates
  # ---------------------------------------------------------------------------

  Scenario: Receive price/volume updates over WebSocket
    Given I am connected via WebSocket
    When an update is sent
    Then I should receive a message in format "X.XX / NNNN"

  Scenario: Updates are sent to all connected WebSocket clients
    Given 3 clients are connected via WebSocket
    When an update is sent
    Then all 3 clients should receive the same message

  Scenario: Price starts near 100.0
    When the application first starts
    Then the initial price should be approximately 100.0

  Scenario: Volume starts near 300000
    When the application first starts
    Then the initial volume should be approximately 300000

  Scenario: Price fluctuates with each update
    When multiple updates are received
    Then the price should change by at most 0.50 per update

  Scenario: Volume fluctuates with each update
    When multiple updates are received
    Then the volume should change by at most 2500 per update

  # ---------------------------------------------------------------------------
  # Connection management
  # ---------------------------------------------------------------------------

  Scenario: New connections are added to the session queue
    When a new client connects
    Then the session queue should contain the new session

  Scenario: Closed connections are removed from the session queue
    Given a client is connected
    When the client disconnects
    Then the session should be removed from the queue

  Scenario: Error connections are removed from the session queue
    Given a client is connected
    When a connection error occurs
    Then the session should be removed from the queue
    And the server log should contain "Connection error"

  # ---------------------------------------------------------------------------
  # Update format
  # ---------------------------------------------------------------------------

  Scenario: Update message uses two decimal places for price
    When a price update of 100.5 is sent
    Then the message should format the price as "100.50"

  Scenario: Update message includes both price and volume separated by " / "
    When an update is sent
    Then the message should match the pattern "\\d+\\.\\d{2} / \\d+"
