Feature: RSVP Event Management System
  As an event organizer
  I want to manage events with invitees and track RSVP responses
  So that I can monitor attendance status via a REST API

  Background:
    Given the RSVP application is running
    And the database is initialized with events and invitees

  # ---------------------------------------------------------------------------
  # Event listing
  # ---------------------------------------------------------------------------

  Scenario: Get all events
    When I GET /status/all
    Then the response status should be 200
    And the response should contain a list of events

  Scenario: Events contain name, location, and date
    When I GET /status/all
    Then each event in the response should have name, location, and eventDate fields

  Scenario: Events contain invitees list
    When I GET /status/all
    Then each event should have an invitees list

  # ---------------------------------------------------------------------------
  # Event details
  # ---------------------------------------------------------------------------

  Scenario: Get a specific event by ID
    Given event with ID 1 exists
    When I GET /status/1/
    Then the response status should be 200
    And the response should contain the event details

  Scenario: Event details include responses from invitees
    Given event with ID 1 has responses
    When I GET /status/1/
    Then the response should include the RSVP responses

  # ---------------------------------------------------------------------------
  # RSVP responses
  # ---------------------------------------------------------------------------

  Scenario: Response can be ATTENDING
    When an invitee responds with "Attending"
    Then the response should be stored with status ATTENDING

  Scenario: Response can be NOT_ATTENDING
    When an invitee responds with "Not attending"
    Then the response should be stored with status NOT_ATTENDING

  Scenario: Response can be MAYBE_ATTENDING
    When an invitee responds with "Maybe"
    Then the response should be stored with status MAYBE_ATTENDING

  Scenario: Default response is NOT_RESPONDED
    Given an invitee has not yet responded
    Then their response status should be "No response yet"

  # ---------------------------------------------------------------------------
  # Response enum values
  # ---------------------------------------------------------------------------

  Scenario: ResponseEnum has correct labels
    Then ATTENDING should have label "Attending"
    And NOT_ATTENDING should have label "Not attending"
    And MAYBE_ATTENDING should have label "Maybe"
    And NOT_RESPONDED should have label "No response yet"

  # ---------------------------------------------------------------------------
  # Content negotiation
  # ---------------------------------------------------------------------------

  Scenario: Status endpoint supports XML format
    When I GET /status/all with Accept header "application/xml"
    Then the response Content-Type should contain "xml"

  Scenario: Status endpoint supports JSON format
    When I GET /status/all with Accept header "application/json"
    Then the response Content-Type should contain "json"

  # ---------------------------------------------------------------------------
  # Event entity structure
  # ---------------------------------------------------------------------------

  Scenario: Event entity has auto-generated ID
    When a new event is created
    Then the event ID should be auto-generated

  Scenario: Event has many-to-many relationship with Person (invitees)
    Given an event with invitees
    Then the event's getInvitees should return the list of invited persons

  Scenario: Event has one-to-many relationship with Response
    Given an event with responses
    Then the event's getResponses should return the list of RSVP responses

  Scenario: Event has a many-to-one relationship with Person (owner)
    Given an event with an owner
    Then the event's getOwner should return the organizing person

  # ---------------------------------------------------------------------------
  # Error handling
  # ---------------------------------------------------------------------------

  Scenario: Request for non-existent event returns null
    When I GET /status/99999/
    Then the response body should be empty or null
