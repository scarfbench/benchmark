Feature: Hello Servlet
  As a client
  I want to send a GET request with a name parameter
  So that I receive a personalized "Hello, NAME!" greeting

  Background:
    Given the hello servlet application is running

  # ---------------------------------------------------------------------------
  # Core greeting functionality
  # ---------------------------------------------------------------------------

  Scenario: Greet a user by name
    When I GET /greeting?name=Duke
    Then the response status should be 200
    And the response body should be "Hello, Duke!"

  Scenario: Greet another user by name
    When I GET /greeting?name=Alice
    Then the response status should be 200
    And the response body should be "Hello, Alice!"

  Scenario: Greet with name "World"
    When I GET /greeting?name=World
    Then the response status should be 200
    And the response body should be "Hello, World!"

  Scenario Outline: Various names produce correct greetings
    When I GET /greeting?name=<name>
    Then the response status should be 200
    And the response body should be "Hello, <name>!"

    Examples:
      | name    |
      | Duke    |
      | Alice   |
      | Bob     |
      | Charlie |

  # ---------------------------------------------------------------------------
  # Missing name parameter
  # ---------------------------------------------------------------------------

  Scenario: Missing name parameter returns 400
    When I GET /greeting without a name parameter
    Then the response status should be 400
    And the response body should contain "Missing required parameter: name"

  Scenario: Blank name parameter returns 400
    When I GET /greeting?name=
    Then the response status should be 400
    And the response body should contain "Missing required parameter: name"

  Scenario: Whitespace-only name parameter returns 400
    When I GET /greeting?name=%20%20
    Then the response status should be 400
    And the response body should contain "Missing required parameter: name"

  # ---------------------------------------------------------------------------
  # Response format
  # ---------------------------------------------------------------------------

  Scenario: Response content type is text/plain
    When I GET /greeting?name=Duke
    Then the response Content-Type should be "text/plain"

  Scenario: Response body contains only the greeting text
    When I GET /greeting?name=Duke
    Then the response body should be exactly "Hello, Duke!"

  # ---------------------------------------------------------------------------
  # Edge cases
  # ---------------------------------------------------------------------------

  Scenario: Name with spaces is handled correctly
    When I GET /greeting?name=Mary%20Jane
    Then the response body should be "Hello, Mary Jane!"

  Scenario: Name with special characters
    When I GET /greeting?name=O'Brien
    Then the response status should be 200
    And the response body should contain "Hello, O'Brien!"

  Scenario: Long name is handled correctly
    When I GET /greeting?name=Bartholomew
    Then the response status should be 200
    And the response body should be "Hello, Bartholomew!"
