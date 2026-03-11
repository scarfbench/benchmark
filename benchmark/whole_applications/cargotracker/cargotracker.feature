Feature: Eclipse Cargo Tracker — DDD Shipping Application
  As a shipping company operator
  I want to book, route, track, and handle cargo shipments
  So that I can manage the full lifecycle of cargo from origin to destination

  Background:
    Given the Cargo Tracker application is running
    And the database is seeded with sample locations, voyages, and cargos

  # ---------------------------------------------------------------------------
  # Sample data expectations
  # ---------------------------------------------------------------------------

  Scenario: Pre-loaded locations include 13 world ports
    Then the system should contain locations including:
      | unLocode | name       |
      | CNHKG    | Hong Kong  |
      | AUMEL    | Melbourne  |
      | SESTO    | Stockholm  |
      | FIHEL    | Helsinki   |
      | USCHI    | Chicago    |
      | JNTKO    | Tokyo      |
      | DEHAM    | Hamburg    |
      | CNSHA    | Shanghai   |
      | NLRTM    | Rotterdam  |
      | SEGOT    | Gothenburg |
      | CNHGH    | Hangzhou   |
      | USNYC    | New York   |
      | USDAL    | Dallas     |

  Scenario: Pre-loaded voyages include ship, train, and airplane routes
    Then the system should contain voyages including:
      | voyageNumber | description                                    |
      | 0100S        | Hong Kong to Melbourne to New York (ship)      |
      | 0200T        | New York to Chicago to Dallas (train)          |
      | 0300A        | Dallas to Hamburg to Stockholm to Helsinki (air)|
      | 0400S        | Helsinki to Rotterdam to Shanghai to HK (ship) |

  Scenario: Four sample cargos are pre-loaded
    Then the following cargos should exist:
      | trackingId | origin | destination | routingStatus | transportStatus |
      | ABC123     | CNHKG  | FIHEL       | ROUTED        | IN_PORT         |
      | JKL567     | CNHGH  | SESTO       | ROUTED        | IN_PORT         |
      | DEF789     | CNHKG  | AUMEL       | NOT_ROUTED    | NOT_RECEIVED    |
      | MNO456     | USNYC  | USDAL       | ROUTED        | CLAIMED         |

  Scenario: Cargo JKL567 is misdirected
    When I look up cargo "JKL567"
    Then the cargo should be marked as misdirected

  Scenario: Cargo MNO456 has completed its full lifecycle
    When I look up cargo "MNO456"
    Then the transport status should be "CLAIMED"
    And the cargo should be unloaded at its destination

  # ---------------------------------------------------------------------------
  # Booking — new cargo
  # ---------------------------------------------------------------------------

  Scenario: Book a new cargo with valid origin, destination, and deadline
    When I book a new cargo from "CNHKG" to "FIHEL" with arrival deadline 30 days from now
    Then the cargo should be created successfully
    And a unique tracking ID should be generated
    And the routing status should be "NOT_ROUTED"

  Scenario: Booking requires origin and destination to be different
    When I try to book a cargo from "CNHKG" to "CNHKG"
    Then the booking should be rejected because origin equals destination

  Scenario: Booking requires the arrival deadline to be in the future
    When I try to book a cargo with an arrival deadline in the past
    Then the booking should be rejected because deadline is not in the future

  Scenario: Booking requires a valid origin location
    When I try to book a cargo from an unknown location
    Then the booking should fail with a validation error

  Scenario: Booking requires a valid destination location
    When I try to book a cargo to an unknown location
    Then the booking should fail with a validation error

  # ---------------------------------------------------------------------------
  # Booking — JSF flow (multi-step wizard)
  # ---------------------------------------------------------------------------

  Scenario: Booking flow starts with origin selection
    When I navigate to the booking flow
    Then I should see a dropdown of all 13 locations for origin selection

  Scenario: Destination dropdown excludes the selected origin
    Given I selected "Hong Kong" as the origin
    When I proceed to the destination step
    Then the destination dropdown should not include "Hong Kong"

  Scenario: Arrival deadline must be at least 1 day from today
    Given I selected origin and destination
    When I enter an arrival deadline of today
    Then the Book button should be disabled

  Scenario: Completing the booking flow creates a cargo and redirects to dashboard
    When I complete the booking flow with valid origin, destination, and deadline
    Then a new cargo should appear on the admin dashboard
    And I should be redirected to the dashboard page

  # ---------------------------------------------------------------------------
  # Routing
  # ---------------------------------------------------------------------------

  Scenario: Request possible routes for an unrouted cargo
    Given cargo "DEF789" is not routed
    When I request possible routes for "DEF789"
    Then I should receive a list of candidate itineraries
    And each itinerary should have one or more legs

  Scenario: Each itinerary leg has voyage, load/unload locations, and times
    When I view a candidate itinerary
    Then each leg should specify a voyage number
    And each leg should specify load location, unload location, load time, and unload time

  Scenario: Assign an itinerary to a cargo
    Given cargo "DEF789" is not routed
    And candidate itineraries are available
    When I select and assign an itinerary to "DEF789"
    Then the routing status should become "ROUTED"

  Scenario: Only itineraries satisfying the route specification are offered
    Given a cargo with origin "CNHKG" and destination "AUMEL" and a deadline
    When I request possible routes
    Then every candidate itinerary should start from "CNHKG"
    And every candidate itinerary should end at "AUMEL"
    And every candidate itinerary should arrive before the deadline

  Scenario: No routes found displays a message
    Given a cargo with an impossible route specification
    When I request possible routes
    Then a "No routes found" message should be displayed

  # ---------------------------------------------------------------------------
  # Change destination
  # ---------------------------------------------------------------------------

  Scenario: Change the destination of an unrouted cargo
    Given cargo "DEF789" has destination "AUMEL"
    When I change the destination to "SESTO"
    Then the destination should be updated to "SESTO"
    And the origin should remain unchanged

  Scenario: Changing destination may cause misrouting
    Given a routed cargo with destination "FIHEL"
    When I change the destination to "AUMEL"
    Then the routing status should become "MISROUTED"

  Scenario: New destination cannot be the same as origin
    Given a cargo with origin "CNHKG"
    When I try to change destination to "CNHKG"
    Then the change should be rejected

  # ---------------------------------------------------------------------------
  # Change deadline
  # ---------------------------------------------------------------------------

  Scenario: Change the arrival deadline to a future date
    Given a cargo with an existing deadline
    When I change the deadline to a date 60 days from now
    Then the deadline should be updated

  Scenario: Changing deadline to the past is rejected
    When I try to change the deadline to a past date
    Then the change should be rejected because deadline must be in the future

  Scenario: Changing deadline may cause misrouting
    Given a routed cargo whose itinerary arrives in 20 days
    When I change the deadline to 10 days from now
    Then the routing status should become "MISROUTED"

  # ---------------------------------------------------------------------------
  # Handling events — registration
  # ---------------------------------------------------------------------------

  Scenario: Register a RECEIVE event at origin
    Given cargo "ABC123" exists
    When I register a RECEIVE event at "CNHKG"
    Then the handling event should be recorded
    And the transport status should be "IN_PORT"

  Scenario: Register a LOAD event with a voyage
    Given cargo "ABC123" exists
    When I register a LOAD event at "CNHKG" on voyage "0100S"
    Then the transport status should be "ONBOARD_CARRIER"

  Scenario: Register an UNLOAD event with a voyage
    Given cargo "ABC123" is onboard voyage "0100S"
    When I register an UNLOAD event at "USNYC" from voyage "0100S"
    Then the transport status should be "IN_PORT"

  Scenario: Register a CUSTOMS event
    Given cargo is in port
    When I register a CUSTOMS event
    Then the transport status should remain "IN_PORT"
    And CUSTOMS is always considered an expected event

  Scenario: Register a CLAIM event at destination
    Given cargo has arrived at its destination
    When I register a CLAIM event at the destination
    Then the transport status should be "CLAIMED"

  Scenario: LOAD event requires a voyage number
    When I try to register a LOAD event without a voyage number
    Then the registration should fail with a validation error

  Scenario: UNLOAD event requires a voyage number
    When I try to register an UNLOAD event without a voyage number
    Then the registration should fail with a validation error

  Scenario: RECEIVE event must not have a voyage number
    When I try to register a RECEIVE event with a voyage number
    Then the registration should fail

  Scenario: CLAIM event must not have a voyage number
    When I try to register a CLAIM event with a voyage number
    Then the registration should fail

  # ---------------------------------------------------------------------------
  # Handling events — REST API
  # ---------------------------------------------------------------------------

  Scenario: Submit a handling report via REST
    When I POST to /rest/handling/reports with:
      | completionTime | 2024-03-01 12:00 |
      | trackingId     | ABC123           |
      | eventType      | UNLOAD           |
      | unLocode       | USNYC            |
      | voyageNumber   | 0100S            |
    Then the response status should be 200 or 204

  Scenario: Handling report with invalid tracking ID is rejected
    When I POST a handling report with trackingId "XX"
    Then the response should indicate a validation error for trackingId (min 4 chars)

  Scenario: Handling report with invalid UN locode is rejected
    When I POST a handling report with unLocode "INVALID"
    Then the response should indicate a validation error for unLocode (exactly 5 chars)

  Scenario: Handling report with invalid event type is rejected
    When I POST a handling report with eventType "UNKNOWN"
    Then the response should indicate a validation error

  # ---------------------------------------------------------------------------
  # Misdirection detection
  # ---------------------------------------------------------------------------

  Scenario: Cargo loaded on wrong voyage becomes misdirected
    Given cargo "JKL567" was loaded onto voyage "0100S" at "USNYC" instead of the expected voyage
    Then cargo "JKL567" should be marked as misdirected

  Scenario: RECEIVE at wrong location causes misdirection
    Given a cargo with first leg loading at "CNHKG"
    When a RECEIVE event is registered at "USNYC"
    Then the cargo should be misdirected

  Scenario: UNLOAD at unexpected location causes misdirection
    Given a cargo expected to unload at "USNYC" on voyage "0100S"
    When an UNLOAD event is registered at "DEHAM" from voyage "0100S"
    Then the cargo should be misdirected

  Scenario: CUSTOMS event is never misdirected
    When a CUSTOMS event is registered at any location
    Then the cargo should NOT be misdirected due to that event

  # ---------------------------------------------------------------------------
  # Delivery state derivation
  # ---------------------------------------------------------------------------

  Scenario: Transport status is NOT_RECEIVED when no events exist
    Given a cargo with no handling events
    Then the transport status should be "NOT_RECEIVED"

  Scenario: Transport status is ONBOARD_CARRIER after LOAD
    Given the last handling event is LOAD
    Then the transport status should be "ONBOARD_CARRIER"

  Scenario: Transport status is IN_PORT after UNLOAD
    Given the last handling event is UNLOAD
    Then the transport status should be "IN_PORT"

  Scenario: Transport status is IN_PORT after RECEIVE
    Given the last handling event is RECEIVE
    Then the transport status should be "IN_PORT"

  Scenario: Transport status is IN_PORT after CUSTOMS
    Given the last handling event is CUSTOMS
    Then the transport status should be "IN_PORT"

  Scenario: Transport status is CLAIMED after CLAIM
    Given the last handling event is CLAIM
    Then the transport status should be "CLAIMED"

  # ---------------------------------------------------------------------------
  # Next expected activity
  # ---------------------------------------------------------------------------

  Scenario: After RECEIVE, next expected is LOAD onto first leg
    Given a routed cargo that was just received at its origin
    Then the next expected activity should be LOAD on the first leg's voyage at the first leg's load location

  Scenario: After LOAD, next expected is UNLOAD from that voyage
    Given a cargo loaded onto a voyage
    Then the next expected activity should be UNLOAD from that voyage at the leg's unload location

  Scenario: After UNLOAD at intermediate port, next expected is LOAD onto next leg
    Given a cargo unloaded at an intermediate port
    Then the next expected activity should be LOAD on the next leg's voyage

  Scenario: After UNLOAD at final destination, next expected is CLAIM
    Given a cargo unloaded at its final destination
    Then the next expected activity should be CLAIM at that location

  Scenario: After CLAIM, no next expected activity
    Given a cargo that has been claimed
    Then there should be no next expected activity

  # ---------------------------------------------------------------------------
  # ETA calculation
  # ---------------------------------------------------------------------------

  Scenario: ETA is calculated for routed, non-misdirected cargo
    Given a routed cargo that is not misdirected
    Then the ETA should equal the final leg's unload time

  Scenario: ETA is not available for misdirected cargo
    Given a misdirected cargo
    Then the ETA should not be available

  Scenario: ETA is not available for unrouted cargo
    Given an unrouted cargo
    Then the ETA should not be available

  # ---------------------------------------------------------------------------
  # Rerouting after misdirection
  # ---------------------------------------------------------------------------

  Scenario: Misdirected cargo shows reroute option
    Given cargo "JKL567" is misdirected
    When I view cargo details for "JKL567"
    Then a "Reroute" button should be available

  Scenario: Rerouting a misdirected cargo assigns a new itinerary
    Given cargo "JKL567" is misdirected
    When I select a new itinerary from the reroute page
    Then the cargo should become "ROUTED" again
    And the cargo should no longer be misdirected

  # ---------------------------------------------------------------------------
  # Public tracking interface
  # ---------------------------------------------------------------------------

  Scenario: Track a cargo by tracking ID
    When I enter tracking ID "ABC123" on the public tracking page
    Then I should see the cargo's current status
    And I should see the destination
    And I should see the handling history

  Scenario: Track a non-existent cargo shows not found
    When I enter tracking ID "ZZZZZ" on the public tracking page
    Then I should see "Cargo not found"

  Scenario: Tracking page shows misdirection warning
    When I track cargo "JKL567"
    Then I should see a misdirection warning

  Scenario: Tracking page displays handling event history
    When I track cargo "ABC123"
    Then I should see a chronological list of handling events
    And each event should show type, location, and time

  Scenario: Tracking page shows ETA for routed cargo
    When I track a routed, non-misdirected cargo
    Then the ETA should be displayed

  Scenario: Tracking page includes a map showing cargo location
    When I track a cargo
    Then a map iframe should be rendered showing the cargo's position

  # ---------------------------------------------------------------------------
  # Admin dashboard
  # ---------------------------------------------------------------------------

  Scenario: Dashboard shows three cargo tables
    When I view the admin dashboard
    Then I should see a "Routed" cargo table
    And I should see a "Not Routed" cargo table
    And I should see a "Claimed" cargo table

  Scenario: Routed cargo table shows tracking ID, origin, destination, status, deadline
    When I view the routed cargo table
    Then each row should display tracking ID, origin, destination, last known location, status, and deadline

  Scenario: Not routed cargo table links to route page
    When I click a tracking ID in the not-routed table
    Then I should be navigated to the route selection page

  Scenario: Cargo details page shows itinerary
    When I view the details for routed cargo "ABC123"
    Then I should see the itinerary with voyage, load/unload locations, and times

  Scenario: Cargo details page shows misrouted warning with reroute button
    Given cargo is misrouted
    When I view the cargo details
    Then I should see "Misrouted cargo!" with a "Reroute" button

  # ---------------------------------------------------------------------------
  # Admin tracking with autocomplete
  # ---------------------------------------------------------------------------

  Scenario: Admin tracking offers autocomplete for tracking IDs
    When I start typing a tracking ID in the admin tracking page
    Then I should see autocomplete suggestions for matching cargos

  # ---------------------------------------------------------------------------
  # Live map (SSE)
  # ---------------------------------------------------------------------------

  Scenario: Live map connects via SSE to receive real-time updates
    When I open the admin live map page
    Then a Server-Sent Events connection should be established to /rest/cargo

  Scenario: SSE sends current cargo positions on connect
    When an SSE client connects to /rest/cargo
    Then it should receive the current state of all active cargos

  Scenario: SSE event payload contains tracking, routing, and location info
    When I receive an SSE event
    Then it should contain trackingId, routingStatus, transportStatus, misdirected, origin, and lastKnownLocation

  # ---------------------------------------------------------------------------
  # Event Logger (mobile interface)
  # ---------------------------------------------------------------------------

  Scenario: Event Logger is a multi-step wizard
    When I open the Event Logger
    Then I should see a wizard with steps for tracking ID, location, event type, voyage, date, and confirmation

  Scenario: Event Logger tracking ID dropdown shows routed unclaimed cargos
    When I open the Event Logger
    Then the tracking ID dropdown should list only routed, unclaimed cargos

  Scenario: Event Logger requires voyage for LOAD and UNLOAD events
    When I select event type "LOAD" and leave voyage empty
    Then the wizard should display an error about voyage being required

  Scenario: Event Logger does not require voyage for RECEIVE, CLAIM, CUSTOMS
    When I select event type "RECEIVE"
    Then the voyage field should not be required

  Scenario: Event Logger submits event asynchronously via JMS
    When I submit the event logger form
    Then the event registration should be queued via JMS for async processing

  # ---------------------------------------------------------------------------
  # File-based bulk event processing
  # ---------------------------------------------------------------------------

  Scenario: CSV files in upload directory are processed every 2 minutes
    Given a CSV file with handling events is placed in /tmp/uploads
    Then the batch scheduler should process it within 2 minutes

  Scenario: CSV file has 5 columns per row
    Then each CSV row should contain: completionTime, trackingId, voyageNumber, unLocode, eventType

  Scenario: Successfully processed CSV files are deleted
    Given a valid CSV file has been processed
    Then the file should be removed from the upload directory

  Scenario: Failed CSV rows are moved to the failed directory
    Given a CSV file contains an invalid row
    Then the invalid row should be recorded in /tmp/failed

  # ---------------------------------------------------------------------------
  # JMS event-driven architecture
  # ---------------------------------------------------------------------------

  Scenario: Handling event triggers cargo inspection via JMS
    When a handling event is registered
    Then a message should be sent to the CargoHandledQueue
    And the cargo inspection service should examine the cargo

  Scenario: Misdirected cargo fires a misdirection event
    When a cargo is inspected and found misdirected
    Then a message should be sent to the MisdirectedCargoQueue

  Scenario: Cargo arrived at destination fires a delivery event
    When a cargo is unloaded at its final destination
    Then a message should be sent to the DeliveredCargoQueue

  # ---------------------------------------------------------------------------
  # Domain value object constraints
  # ---------------------------------------------------------------------------

  Scenario: TrackingId must be at least 4 characters
    When I create a tracking ID with fewer than 4 characters
    Then a validation error should occur

  Scenario: UnLocode must be exactly 5 alphanumeric characters
    When I create a UN location code that is not exactly 5 characters
    Then a validation error should occur

  Scenario: RouteSpecification requires origin different from destination
    When I create a route specification where origin equals destination
    Then a validation error should occur

  Scenario: Itinerary has an EMPTY_ITINERARY null object
    When a cargo has no assigned itinerary
    Then the itinerary should be the EMPTY_ITINERARY instance

  Scenario: Location.UNKNOWN has code "XXXXX"
    Then the unknown location sentinel should have UN locode "XXXXX"

  Scenario: Voyage.NONE is the null-object voyage
    Then the null-object voyage should exist for events not requiring a voyage

  # ---------------------------------------------------------------------------
  # Cargo full lifecycle scenario
  # ---------------------------------------------------------------------------

  Scenario: Full cargo lifecycle from booking to claim
    Given I book a new cargo from "CNHKG" to "FIHEL" with a valid deadline
    And I assign a route with legs:
      | voyage | from  | to    |
      | 0100S  | CNHKG | USNYC |
      | 0200T  | USNYC | USDAL |
      | 0300A  | USDAL | FIHEL |
    When I register RECEIVE at "CNHKG"
    Then transport status should be "IN_PORT" and next expected is LOAD on 0100S
    When I register LOAD at "CNHKG" on voyage "0100S"
    Then transport status should be "ONBOARD_CARRIER"
    When I register UNLOAD at "USNYC" from voyage "0100S"
    Then transport status should be "IN_PORT" and next expected is LOAD on 0200T
    When I register LOAD at "USNYC" on voyage "0200T"
    Then transport status should be "ONBOARD_CARRIER"
    When I register UNLOAD at "USDAL" from voyage "0200T"
    Then transport status should be "IN_PORT" and next expected is LOAD on 0300A
    When I register LOAD at "USDAL" on voyage "0300A"
    Then transport status should be "ONBOARD_CARRIER"
    When I register UNLOAD at "FIHEL" from voyage "0300A"
    Then transport status should be "IN_PORT" and next expected is CLAIM
    When I register CUSTOMS at "FIHEL"
    Then transport status should remain "IN_PORT"
    When I register CLAIM at "FIHEL"
    Then transport status should be "CLAIMED"
    And there should be no next expected activity

  # ---------------------------------------------------------------------------
  # External routing service (Pathfinder)
  # ---------------------------------------------------------------------------

  Scenario: Routing service calls external graph traversal API
    When the system computes routes for a cargo
    Then it should call the Pathfinder API with origin and destination UN locodes

  Scenario: Transit paths are converted to Itinerary objects
    When the Pathfinder returns transit paths
    Then each transit path should be converted to an Itinerary with Legs

  Scenario: Only specification-satisfying itineraries are returned
    When the Pathfinder returns multiple transit paths
    Then only itineraries matching the route specification should be offered to the user
