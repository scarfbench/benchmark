Feature: Caesar Cipher Encoder with CDI Alternatives
  As a user
  I want to encode strings using different Coder implementations
  So that I can switch between a Caesar cipher and a test encoder via CDI alternatives

  Background:
    Given the encoder application is running

  # ---------------------------------------------------------------------------
  # Encoder form
  # ---------------------------------------------------------------------------

  Scenario: Encoder form displays input fields
    When I view the encoder form
    Then I should see an input string field
    And I should see a shift value field
    And I should see an Encode button

  # ---------------------------------------------------------------------------
  # CoderImpl (default) - Caesar cipher shift
  # ---------------------------------------------------------------------------

  Scenario: Encode a lowercase string with shift of 1
    Given the default CoderImpl is active
    When I encode "abc" with shift 1
    Then the result should be "bcd"

  Scenario: Encode an uppercase string with shift of 3
    Given the default CoderImpl is active
    When I encode "XYZ" with shift 3
    Then the result should be "ABC"

  Scenario: Spaces are preserved by the cipher
    Given the default CoderImpl is active
    When I encode "a b c" with shift 1
    Then the result should be "b c d"

  Scenario: Punctuation and non-letter characters are not shifted
    Given the default CoderImpl is active
    When I encode "a!b" with shift 1
    Then the result should be "b!c"

  Scenario: Lowercase wrap-around from z to a
    Given the default CoderImpl is active
    When I encode "xyz" with shift 3
    Then the result should be "abc"

  Scenario: Uppercase wrap-around from Z to A
    Given the default CoderImpl is active
    When I encode "XYZ" with shift 1
    Then the result should be "YZA"

  Scenario: Shift of 0 leaves the string unchanged
    Given the default CoderImpl is active
    When I encode "Hello" with shift 0
    Then the result should be "Hello"

  # ---------------------------------------------------------------------------
  # TestCoderImpl (alternative) - echoes input
  # ---------------------------------------------------------------------------

  Scenario: TestCoderImpl echoes the input string and shift value
    Given the TestCoderImpl alternative is active
    When I encode "Hello" with shift 3
    Then the result should be "input string is Hello, shift value is 3"

  Scenario: TestCoderImpl echoes correctly with shift of 0
    Given the TestCoderImpl alternative is active
    When I encode "test" with shift 0
    Then the result should be "input string is test, shift value is 0"

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

  Scenario: Shift value is required
    When I submit the encode request without a shift value
    Then a validation error should be displayed

  # ---------------------------------------------------------------------------
  # Reset
  # ---------------------------------------------------------------------------

  Scenario: Reset clears the input fields
    Given I have entered "Hello" with shift 3
    When I reset the form
    Then the input string should be empty
    And the shift value should be 0
