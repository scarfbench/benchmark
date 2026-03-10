Feature: File Upload Servlet
  As a user
  I want to upload files to a specified destination directory
  So that files are saved on the server via a multipart form submission

  Background:
    Given the file upload application is running

  # ---------------------------------------------------------------------------
  # Upload form
  # ---------------------------------------------------------------------------

  Scenario: Upload form displays file and destination fields
    When I view the upload page
    Then I should see a file input field
    And I should see a destination directory input field
    And I should see an Upload button

  # ---------------------------------------------------------------------------
  # Successful upload
  # ---------------------------------------------------------------------------

  Scenario: Upload a file to a valid destination
    Given a writable destination directory "/tmp/uploads" exists
    When I upload a file named "test.txt" to destination "/tmp/uploads"
    Then the response should contain "New file test.txt created at /tmp/uploads"
    And the file should exist at "/tmp/uploads/test.txt"

  Scenario: Upload reports the file name in the response
    When I upload a file named "document.pdf" to a valid destination
    Then the response should contain "document.pdf"

  Scenario: Upload reports the destination path in the response
    When I upload a file to destination "/tmp/uploads"
    Then the response should contain "/tmp/uploads"

  # ---------------------------------------------------------------------------
  # Missing destination parameter
  # ---------------------------------------------------------------------------

  Scenario: Upload without a destination parameter shows an error message
    When I upload a file without specifying a destination
    Then the response should contain "Please specify a destination directory"

  Scenario: Upload with blank destination shows an error message
    When I upload a file with destination ""
    Then the response should contain "Please specify a destination directory"

  # ---------------------------------------------------------------------------
  # File not specified or invalid destination
  # ---------------------------------------------------------------------------

  Scenario: Upload to a nonexistent destination returns an error
    When I upload a file named "test.txt" to destination "/nonexistent/path"
    Then the response should contain "trying to upload a file to a protected or nonexistent location"
    And the response should contain "ERROR:"

  Scenario: Upload without selecting a file returns an error
    When I submit the form without selecting a file to destination "/tmp/uploads"
    Then the response should contain an error message

  # ---------------------------------------------------------------------------
  # Multipart configuration
  # ---------------------------------------------------------------------------

  Scenario: Servlet accepts multipart form data
    When I send a POST request with multipart/form-data to /upload
    Then the servlet should process the multipart request

  Scenario: File name is extracted from content-disposition header
    When I upload a file with content-disposition containing filename "myfile.txt"
    Then the saved file name should be "myfile.txt"

  # ---------------------------------------------------------------------------
  # Response format
  # ---------------------------------------------------------------------------

  Scenario: Response content type is text/html
    When I upload a file
    Then the response Content-Type should be "text/html;charset=UTF-8"

  Scenario: Both GET and POST methods process the upload
    When I send a GET request with file and destination parameters
    Then the upload should be processed
    When I send a POST request with file and destination parameters
    Then the upload should be processed
