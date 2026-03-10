Feature: Task Creator with Managed Executors
  As a user
  I want to create and manage tasks of different types (IMMEDIATE, DELAYED, PERIODIC)
  So that tasks execute on managed executor services and report their progress

  Background:
    Given the task creator application is running

  # ---------------------------------------------------------------------------
  # Task creation form
  # ---------------------------------------------------------------------------

  Scenario: Task form displays task name and type fields
    When I view the task creator page
    Then I should see a task name input field
    And I should see a task type selector with IMMEDIATE, DELAYED, and PERIODIC options
    And the default task type should be "IMMEDIATE"

  # ---------------------------------------------------------------------------
  # Immediate tasks
  # ---------------------------------------------------------------------------

  Scenario: Submit an IMMEDIATE task
    When I enter task name "TaskA" with type "IMMEDIATE"
    And I submit the task
    Then the task should start executing immediately
    And the log should contain "IMMEDIATE Task TaskA started"
    And the log should contain "IMMEDIATE Task TaskA finished"

  Scenario: IMMEDIATE task runs for approximately 1.5 seconds
    When I submit an IMMEDIATE task named "QuickTask"
    Then the task should finish approximately 1.5 seconds after starting

  # ---------------------------------------------------------------------------
  # Delayed tasks
  # ---------------------------------------------------------------------------

  Scenario: Submit a DELAYED task
    When I enter task name "TaskB" with type "DELAYED"
    And I submit the task
    Then the log should contain "DELAYED Task TaskB submitted"
    And after the delay the log should contain "DELAYED Task TaskB started"
    And the log should eventually contain "DELAYED Task TaskB finished"

  Scenario: DELAYED task logs submission time before execution
    When I submit a DELAYED task named "DelayedOne"
    Then the "submitted" log entry should appear before the "started" entry

  # ---------------------------------------------------------------------------
  # Periodic tasks
  # ---------------------------------------------------------------------------

  Scenario: Submit a PERIODIC task
    When I enter task name "TaskC" with type "PERIODIC"
    And I submit the task
    Then the task should execute repeatedly
    And the log should contain "PERIODIC Task TaskC started run #1"
    And the log should contain "PERIODIC Task TaskC finished run #1"

  Scenario: PERIODIC task increments run counter
    Given a PERIODIC task "Repeater" has been running
    Then the log should show increasing run numbers like "run #1", "run #2", "run #3"

  Scenario: Duplicate periodic task name is not submitted
    Given a PERIODIC task named "UniqueTask" is already running
    When I submit another task named "UniqueTask"
    Then the new task should not be created

  # ---------------------------------------------------------------------------
  # Cancel periodic tasks
  # ---------------------------------------------------------------------------

  Scenario: Cancel a running periodic task
    Given a PERIODIC task "TaskC" is running
    When I select "TaskC" from the periodic tasks list
    And I click Cancel Task
    Then "TaskC" should stop executing

  Scenario: Periodic tasks list shows active periodic tasks
    Given PERIODIC tasks "Alpha" and "Beta" are running
    When I view the periodic tasks list
    Then the list should contain "Alpha" and "Beta"

  # ---------------------------------------------------------------------------
  # Log management
  # ---------------------------------------------------------------------------

  Scenario: Task messages are displayed in the log area
    When I submit an IMMEDIATE task
    Then the task messages area should display timestamped log entries

  Scenario: Clear the task log
    Given the log contains task messages
    When I click Clear Log
    Then the task messages area should be empty

  # ---------------------------------------------------------------------------
  # Task log format
  # ---------------------------------------------------------------------------

  Scenario: Log entries include timestamp, type, name, and details
    When a task named "TestTask" of type "IMMEDIATE" runs
    Then the log entry should match format "HH:mm:ss - IMMEDIATE Task TestTask started"

  Scenario: Log entries are posted to the /taskinfo REST endpoint
    When a task runs
    Then the task should POST its status to the info web service endpoint
