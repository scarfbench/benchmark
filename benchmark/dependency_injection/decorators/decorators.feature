Feature: String Encoder with CDI Decorator
  As a user
  I want to encode strings using a Caesar cipher with a decorator wrapper
  So that the output includes both the shifted result and a descriptive format

  Background:
    Given the decorators application is running
    And the CoderDecorator is active

  # ---------------------------------------------------------------------------
  # Encoder form
  # ---------------------------------------------------------------------------

  Scenario: Encoder form displays input fields
    When I view the encoder form
    Then I should see an input string field
    And I should see a shift value field
    And I should see an Encode button

  # ---------------------------------------------------------------------------
  # Core encoding with decorator
  # ---------------------------------------------------------------------------

  Scenario: Encode a simple string with shift of 1
    When I enter input string "abc" with shift value 1
    And I submit the encode request
    Then the result should be "\"abc\" becomes \"bcd\", 3 characters in length"

  Scenario: Encode uppercase letters with shift of 3
    When I enter input string "XYZ" with shift value 3
    And I submit the encode request
    Then the result should be "\"XYZ\" becomes \"ABC\", 3 characters in length"

  Scenario: Encode a mixed-case string with shift of 1
    When I enter input string "Hello" with shift value 1
    And I submit the encode request
    Then the result should be "\"Hello\" becomes \"Ifmmp\", 5 characters in length"

  Scenario: Spaces are preserved during encoding
    When I enter input string "a b" with shift value 1
    And I submit the encode request
    Then the result should contain "becomes \"b c\""

  Scenario: Shift of 0 leaves the string unchanged
    When I enter input string "test" with shift value 0
    And I submit the encode request
    Then the result should be "\"test\" becomes \"test\", 4 characters in length"

  # ---------------------------------------------------------------------------
  # Decorator wrapping behavior
  # ---------------------------------------------------------------------------

  Scenario: Decorator wraps the shifted output with descriptive format
    When I encode "Hi" with shift 2
    Then the result should start with "\"Hi\" becomes \""
    And the result should end with "\", 2 characters in length"

  Scenario: Decorator reports correct character length
    When I encode "Hello World" with shift 1
    Then the result should contain "11 characters in length"

  # ---------------------------------------------------------------------------
  # Cipher wrapping at alphabet boundaries
  # ---------------------------------------------------------------------------

  Scenario: Lowercase z wraps to a with shift 1
    When I encode "z" with shift 1
    Then the encoded portion should be "a"

  Scenario: Uppercase Z wraps to A with shift 1
    When I encode "Z" with shift 1
    Then the encoded portion should be "A"

  Scenario: Shift of 26 returns the original string
    When I encode "abc" with shift 26
    Then the encoded portion should be "abc"

  # ---------------------------------------------------------------------------
  # Validation
  # ---------------------------------------------------------------------------

  Scenario: Shift value must be between 0 and 26
    When I enter a shift value of 27
    And I submit the encode request
    Then a validation error should be displayed

  Scenario: Shift value must not be negative
    When I enter a shift value of -1
    And I submit the encode request
    Then a validation error should be displayed

  # ---------------------------------------------------------------------------
  # Interceptor logging
  # ---------------------------------------------------------------------------

  Scenario: Encoding invocation is logged by the LoggedInterceptor
    When I encode a string
    Then the LoggedInterceptor should log "Entering method: codeString" to the console
