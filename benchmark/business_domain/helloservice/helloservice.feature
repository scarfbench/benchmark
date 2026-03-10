Feature: Hello Web Service (SOAP)
  As a client application
  I want to call the sayHello SOAP web service endpoint
  So that I receive a personalized greeting message

  Background:
    Given the HelloService SOAP endpoint is running

  # ---------------------------------------------------------------------------
  # Core greeting scenarios
  # ---------------------------------------------------------------------------

  Scenario: Say hello to John
    When I send a SOAP sayHello request with arg0 "John"
    Then the response should contain "Hello, John."

  Scenario: Say hello to Jane
    When I send a SOAP sayHello request with arg0 "Jane"
    Then the response should contain "Hello, Jane."

  Scenario: Say hello to World
    When I send a SOAP sayHello request with arg0 "World"
    Then the response should contain "Hello, World."

  Scenario: Say hello to Alice
    When I send a SOAP sayHello request with arg0 "Alice"
    Then the response should contain "Hello, Alice."

  Scenario: Say hello to a name containing a space
    When I send a SOAP sayHello request with arg0 "Mary Jane"
    Then the response should contain "Hello, Mary Jane."

  Scenario Outline: Various names are greeted correctly
    When I send a SOAP sayHello request with arg0 "<name>"
    Then the response should contain "Hello, <name>."

    Examples:
      | name    |
      | John    |
      | Jane    |
      | Alice   |
      | Bob     |
      | World   |

  # ---------------------------------------------------------------------------
  # SOAP response structure
  # ---------------------------------------------------------------------------

  Scenario: Response is a valid XML document
    When I send a SOAP sayHello request with arg0 "John"
    Then the response body should be non-empty
    And the response body should start with an XML or SOAP declaration

  Scenario: Response contains a SOAP Envelope element
    When I send a SOAP sayHello request with arg0 "John"
    Then the response should contain "Envelope"

  Scenario: Response contains a SOAP Body element
    When I send a SOAP sayHello request with arg0 "John"
    Then the response should contain "Body"

  Scenario: Response references the helloservice namespace
    When I send a SOAP sayHello request with arg0 "John"
    Then the response should contain "helloservice"

  Scenario: Response does not contain a SOAP Fault
    When I send a SOAP sayHello request with arg0 "John"
    Then the response should not contain "Fault"

  # ---------------------------------------------------------------------------
  # Service metadata
  # ---------------------------------------------------------------------------

  Scenario: WSDL document is accessible at the service URL
    When I request the WSDL document for the service
    Then the response status should be 200
    And the response body should contain "wsdl" or "WSDL"
    And the response body should contain "sayHello"

  # ---------------------------------------------------------------------------
  # HTTP-level checks
  # ---------------------------------------------------------------------------

  Scenario: Endpoint returns HTTP 200 for a valid SOAP request
    When I send a SOAP sayHello request with arg0 "John"
    Then the HTTP response status should be 200

  Scenario: Content-Type header indicates XML in the response
    When I send a SOAP sayHello request with arg0 "John"
    Then the response Content-Type should contain "xml"
