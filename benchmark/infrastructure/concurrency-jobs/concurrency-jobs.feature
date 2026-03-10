Feature: Job Submission REST Service with Token-Based Priority
  As a client application
  I want to submit jobs via a REST API with optional priority tokens
  So that authenticated requests are executed on a high-priority executor

  Background:
    Given the concurrency jobs service is running

  # ---------------------------------------------------------------------------
  # Token management
  # ---------------------------------------------------------------------------

  Scenario: Request a new API token
    When I GET /JobService/token
    Then the response status should be 200
    And the response body should contain a token starting with "123X5-"

  Scenario: Each token request returns a unique token
    When I GET /JobService/token twice
    Then each response should return a different token

  Scenario: Token is stored in the singleton TokenStore
    When I GET /JobService/token
    Then the returned token should be valid in the TokenStore

  # ---------------------------------------------------------------------------
  # Job submission with valid token (high priority)
  # ---------------------------------------------------------------------------

  Scenario: Submit a job with a valid token uses high-priority executor
    Given I have obtained a valid API token
    When I POST to /JobService/process with jobID 1 and X-REST-API-Key header set to the token
    Then the response status should be 200
    And the response body should contain "Job 1 successfully submitted."
    And the job should be submitted to the high-priority executor

  Scenario: High-priority job ID is prefixed with "HIGH-"
    Given I have a valid token
    When I submit job 42 with the token
    Then the job task should be created with ID "HIGH-42"

  # ---------------------------------------------------------------------------
  # Job submission without token (low priority)
  # ---------------------------------------------------------------------------

  Scenario: Submit a job without a token uses low-priority executor
    When I POST to /JobService/process with jobID 2 and no X-REST-API-Key header
    Then the response status should be 200
    And the response body should contain "Job 2 successfully submitted."
    And the job should be submitted to the low-priority executor

  Scenario: Low-priority job ID is prefixed with "LOW-"
    When I submit job 99 without a token
    Then the job task should be created with ID "LOW-99"

  # ---------------------------------------------------------------------------
  # Invalid token handling
  # ---------------------------------------------------------------------------

  Scenario: Submit a job with an invalid token falls back to low priority
    When I POST to /JobService/process with jobID 3 and X-REST-API-Key "invalid-token"
    Then the response status should be 200
    And the job should be submitted to the low-priority executor

  # ---------------------------------------------------------------------------
  # Job execution
  # ---------------------------------------------------------------------------

  Scenario: Submitted job runs asynchronously
    Given I submit a job
    Then the response should return immediately
    And the job should execute in the background for approximately 10 seconds

  Scenario: Job logs start and finish messages
    When a job with ID "HIGH-1" runs
    Then the log should contain "Task started HIGH-1"
    And the log should contain "Task finished HIGH-1"

  # ---------------------------------------------------------------------------
  # Executor rejection
  # ---------------------------------------------------------------------------

  Scenario: Service returns 503 when executor rejects the job
    Given the executor service is overloaded
    When I submit a job
    Then the response status should be 503
    And the response body should contain "NOT submitted"

  # ---------------------------------------------------------------------------
  # TokenStore concurrency
  # ---------------------------------------------------------------------------

  Scenario: TokenStore supports concurrent read access
    Given multiple tokens have been stored
    When multiple clients validate tokens concurrently
    Then each validation should return the correct result

  Scenario: TokenStore uses container-managed concurrency with write locks
    When a new token is added to the store
    Then the write operation should be serialized
