Feature: String Encoder with Producer Method
  As a user
  I want to encode strings using a coder chosen by a producer method
  So that the application dynamically selects between Shift and Test coders at runtime

  Background:
    Given the producer methods application is running

  # ---------------------------------------------------------------------------
  # Encoder form
  # ---------------------------------------------------------------------------

  Scenario: Encoder form displays input fields and coder type selector
    When I view the encoder form
    Then I should see an input string field
    And I should see a shift value field
    And I should see a coder type selector with Shift and Test options
    And the default coder type should be Shift

  # ---------------------------------------------------------------------------
  # Shift coder (default, coderType=2)
  # ---------------------------------------------------------------------------

  Scenario: Shift coder encodes a lowercase string
    Given the coder type is set to Shift
    When I encode "abc" with shift 1
    Then the result should be "bcd"

  Scenario: Shift coder encodes an uppercase string with wrap-around
    Given the coder type is set to Shift
    When I encode "XYZ" with shift 3
    Then the result should be "ABC"

  Scenario: Shift coder preserves spaces
    Given the coder type is set to Shift
    When I encode "a b" with shift 1
    Then the result should be "b c"

  Scenario: Shift coder does not modify punctuation
    Given the coder type is set to Shift
    When I encode "a.b!" with shift 1
    Then the result should be "b.c!"

  # ---------------------------------------------------------------------------
  # Test coder (coderType=1)
  # ---------------------------------------------------------------------------

  Scenario: Test coder echoes input string and shift value
    Given the coder type is set to Test
    When I encode "Hello" with shift 3
    Then the result should be "input string is Hello, shift value is 3"

  Scenario: Test coder echoes with shift value 0
    Given the coder type is set to Test
    When I encode "World" with shift 0
    Then the result should be "input string is World, shift value is 0"

  Scenario: Test coder echoes with empty string
    Given the coder type is set to Test
    When I encode "" with shift 5
    Then the result should be "input string is , shift value is 5"

  # ---------------------------------------------------------------------------
  # Producer method behavior
  # ---------------------------------------------------------------------------

  Scenario: Producer method returns CoderImpl when coderType is Shift
    Given the coder type is 2 (Shift)
    When the @Chosen producer method is invoked
    Then it should return an instance of CoderImpl

  Scenario: Producer method returns TestCoderImpl when coderType is Test
    Given the coder type is 1 (Test)
    When the @Chosen producer method is invoked
    Then it should return an instance of TestCoderImpl

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
  # Reset
  # ---------------------------------------------------------------------------

  Scenario: Reset clears the input fields
    Given I have entered "Hello" with shift 3
    When I reset the form
    Then the input string should be empty
    And the shift value should be 0
