Feature: Bill Payment with CDI Events
  As a user
  I want to make debit and credit bill payments
  So that my payments are processed and logged via CDI events and interceptors

  Background:
    Given the bill payment application is running

  # ---------------------------------------------------------------------------
  # Payment form
  # ---------------------------------------------------------------------------

  Scenario: Payment form displays debit and credit options
    When I view the payment form
    Then I should see a payment type selector with Debit and Credit options
    And I should see a value input field
    And I should see a Submit button

  Scenario: Default payment option is Debit
    When I view the payment form
    Then the payment option should default to Debit

  # ---------------------------------------------------------------------------
  # Debit payments
  # ---------------------------------------------------------------------------

  Scenario: Submit a debit payment with a valid amount
    When I select payment type "Debit"
    And I enter a value of "50.00"
    And I submit the payment
    Then the payment should be processed successfully
    And a PaymentEvent with type "Debit" and value "50.00" should be fired

  Scenario: Debit payment event is observed by PaymentHandler
    When I submit a debit payment of "25.50"
    Then the PaymentHandler debitPayment observer should receive the event
    And the event should contain the payment type "Debit"
    And the event should contain the value "25.50"
    And the event should contain a timestamp

  # ---------------------------------------------------------------------------
  # Credit payments
  # ---------------------------------------------------------------------------

  Scenario: Submit a credit payment with a valid amount
    When I select payment type "Credit"
    And I enter a value of "100.00"
    And I submit the payment
    Then the payment should be processed successfully
    And a PaymentEvent with type "Credit" and value "100.00" should be fired

  Scenario: Credit payment event is observed by PaymentHandler
    When I submit a credit payment of "75.25"
    Then the PaymentHandler creditPayment observer should receive the event
    And the event should contain the payment type "Credit"
    And the event should contain the value "75.25"

  # ---------------------------------------------------------------------------
  # Interceptor logging
  # ---------------------------------------------------------------------------

  Scenario: Pay method invocation is logged by the interceptor
    When I submit a payment
    Then the LoggedInterceptor should log "Entering method: pay in class" to the console

  Scenario: Reset method invocation is logged by the interceptor
    When I reset the payment form
    Then the LoggedInterceptor should log "Entering method: reset in class" to the console

  # ---------------------------------------------------------------------------
  # Value validation
  # ---------------------------------------------------------------------------

  Scenario: Payment value accepts up to 10 integer digits and 2 fraction digits
    When I enter a value of "1234567890.99"
    And I submit the payment
    Then the payment should be processed successfully

  Scenario: Payment value with more than 2 decimal places is rejected
    When I enter a value of "50.123"
    And I submit the payment
    Then a validation error "Invalid value" should be displayed

  Scenario: Payment value with more than 10 integer digits is rejected
    When I enter a value of "12345678901.00"
    And I submit the payment
    Then a validation error "Invalid value" should be displayed

  # ---------------------------------------------------------------------------
  # Reset functionality
  # ---------------------------------------------------------------------------

  Scenario: Reset clears the form to default values
    Given I have entered a value of "99.99" and selected "Credit"
    When I reset the payment form
    Then the payment option should be set to Debit
    And the value should be set to zero

  # ---------------------------------------------------------------------------
  # PaymentEvent structure
  # ---------------------------------------------------------------------------

  Scenario: PaymentEvent toString includes type, value, and timestamp
    When I submit a debit payment of "42.00"
    Then the PaymentEvent toString should match "Debit = $42.00 at <timestamp>"

  Scenario: PaymentEvent contains all required fields
    When I submit a credit payment of "10.00"
    Then the event paymentType should be "Credit"
    And the event value should be a BigDecimal
    And the event datetime should not be null
