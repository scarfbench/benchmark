Feature: Asynchronous Email Sending via EJB
  As a user
  I want to send emails asynchronously using the EJB @Asynchronous annotation
  So that the UI does not block while the email is being sent via SMTP on port 3025

  Background:
    Given the async email application is running
    And the SMTP server is listening on port 3025

  # ---------------------------------------------------------------------------
  # Email form
  # ---------------------------------------------------------------------------

  Scenario: Email form displays an email address input field
    When I view the email sending page
    Then I should see an email address input field
    And I should see a Send button

  # ---------------------------------------------------------------------------
  # Sending emails
  # ---------------------------------------------------------------------------

  Scenario: Send an email to a valid address
    When I enter email address "duke@example.com"
    And I click Send
    Then the email should be sent asynchronously
    And the status should eventually be "Sent"

  Scenario: Email is sent to the correct recipient
    When I send an email to "alice@example.com"
    Then the SMTP server should receive a message addressed to "alice@example.com"

  Scenario: Email has the correct subject line
    When I send an email to "duke@example.com"
    Then the email subject should be "Test message from async example"

  Scenario: Email has the X-Mailer header set
    When I send an email to "duke@example.com"
    Then the email should contain header "X-Mailer" with value "Jakarta Mail"

  Scenario: Email body contains the test message text
    When I send an email to "duke@example.com"
    Then the email body should contain "This is a test message from the async example"

  Scenario: Email body contains the sent date
    When I send an email to "duke@example.com"
    Then the email body should contain a formatted date and time

  # ---------------------------------------------------------------------------
  # Asynchronous behavior
  # ---------------------------------------------------------------------------

  Scenario: sendMessage returns a Future
    When I invoke sendMessage with "duke@example.com"
    Then the method should return a Future<String>
    And the Future result should eventually be "Sent"

  Scenario: UI is not blocked during email sending
    When I click Send
    Then the response should return before the email delivery completes

  # ---------------------------------------------------------------------------
  # SMTP configuration
  # ---------------------------------------------------------------------------

  Scenario: Emails are sent via SMTP on port 3025
    When an email is sent
    Then the SMTP transport should use port 3025

  # ---------------------------------------------------------------------------
  # Error handling
  # ---------------------------------------------------------------------------

  Scenario: Messaging error returns an error status
    Given the SMTP server is not available
    When I try to send an email to "duke@example.com"
    Then the status should contain "Encountered an error"

  Scenario: Messaging error is logged
    Given the SMTP server is not available
    When I try to send an email
    Then the server log should contain "Error in sending message"
