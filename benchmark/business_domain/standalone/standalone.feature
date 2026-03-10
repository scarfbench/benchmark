Feature: Standalone Greeting Service
  As an API client
  I want to call the /greet REST endpoint
  So that I receive a greeting message in JSON format

  Background:
    Given the standalone service is running

  # ---------------------------------------------------------------------------
  # HTTP response status
  # ---------------------------------------------------------------------------

  Scenario: GET /greet returns HTTP 200
    When I send a GET request to /greet
    Then the response status should be 200

  Scenario: GET /greet responds consistently across multiple calls
    When I send three consecutive GET requests to /greet
    Then every response status should be 200

  # ---------------------------------------------------------------------------
  # Response body
  # ---------------------------------------------------------------------------

  Scenario: GET /greet response body is non-empty
    When I send a GET request to /greet
    Then the response body should not be empty

  Scenario: GET /greet returns valid JSON
    When I send a GET request to /greet
    Then the response body should be parseable as JSON

  Scenario: GET /greet JSON contains a "message" field
    When I send a GET request to /greet
    Then the JSON body should contain a field named "message"

  Scenario: GET /greet message field equals "Greetings!"
    When I send a GET request to /greet
    Then the JSON "message" field should equal "Greetings!"

  Scenario: GET /greet message is a string value
    When I send a GET request to /greet
    Then the JSON "message" value should be of type string

  # ---------------------------------------------------------------------------
  # Repeated calls — idempotency
  # ---------------------------------------------------------------------------

  Scenario: Repeated GET /greet calls always return "Greetings!"
    When I send five consecutive GET requests to /greet
    Then every response JSON "message" should equal "Greetings!"

  # ---------------------------------------------------------------------------
  # HTTP headers
  # ---------------------------------------------------------------------------

  Scenario: GET /greet response Content-Type indicates JSON
    When I send a GET request to /greet
    Then the response Content-Type header should contain "json"
