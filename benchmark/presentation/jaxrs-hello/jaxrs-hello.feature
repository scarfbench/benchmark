Feature: JAX-RS Hello World
  As a client application
  I want to call the helloworld REST endpoint
  So that I receive an HTML greeting response

  Background:
    Given the JAX-RS hello application is running

  # ---------------------------------------------------------------------------
  # Core greeting
  # ---------------------------------------------------------------------------

  Scenario: GET /helloworld returns "Hello, World!!" in HTML
    When I GET /helloworld
    Then the response status should be 200
    And the response body should contain "Hello, World!!"

  Scenario: Response contains a complete HTML document
    When I GET /helloworld
    Then the response body should contain "<html"
    And the response body should contain "<body>"
    And the response body should contain "</body>"
    And the response body should contain "</html>"

  Scenario: Response contains an H1 heading with the greeting
    When I GET /helloworld
    Then the response body should contain "<h1>Hello, World!!</h1>"

  # ---------------------------------------------------------------------------
  # Response format
  # ---------------------------------------------------------------------------

  Scenario: Response content type is text/html
    When I GET /helloworld
    Then the response Content-Type should contain "text/html"

  Scenario: Response body is not empty
    When I GET /helloworld
    Then the response body should not be empty

  # ---------------------------------------------------------------------------
  # HTTP methods
  # ---------------------------------------------------------------------------

  Scenario: GET method is supported
    When I send a GET request to /helloworld
    Then the response status should be 200

  Scenario: PUT method is supported
    When I send a PUT request to /helloworld with content type "text/html"
    Then the response status should be 204 or 200

  # ---------------------------------------------------------------------------
  # Response structure
  # ---------------------------------------------------------------------------

  Scenario: Response does not contain error messages
    When I GET /helloworld
    Then the response body should not contain "Exception"
    And the response body should not contain "Error"

  Scenario: HTML includes lang attribute
    When I GET /helloworld
    Then the response body should contain "lang=\"en\""
