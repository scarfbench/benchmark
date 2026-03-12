Feature: Jakarta EE Petclinic Application
  As a veterinary clinic administrator
  I want to manage owners, pets, visits, veterinarians, and specialties
  So that I can run a pet clinic with full CRUD operations and a read-only REST API

  Background:
    Given the Petclinic application is running
    And the database is initialized with sample data

  # ---------------------------------------------------------------------------
  # Homepage and navigation
  # ---------------------------------------------------------------------------

  Scenario: Homepage loads successfully
    When I navigate to /petclinic/home.jsf
    Then the page title should contain "Petclinic"
    And the navigation menu should contain links: Home, Owner, Pet Type, Veterinarian, Specialty

  Scenario: Index page redirects to home
    When I navigate to /petclinic/index.html
    Then I should be redirected to /petclinic/home.jsf

  Scenario: Homepage displays owner search panel
    When I view the homepage
    Then I should see a search input field
    And I should see a submit button

  Scenario: Homepage displays welcome panel
    When I view the homepage
    Then the page body should be visible

  Scenario: Navigation menu persists across all pages
    When I visit home, owner, petType, veterinarian, and specialty pages
    Then the navigation menu should be present on each page

  Scenario: Information page is accessible
    When I click the Information link
    Then I should be navigated to /petclinic/info.jsf

  Scenario: Language selector is available on every page
    When I view any page
    Then I should see a language dropdown with a save button

  # ---------------------------------------------------------------------------
  # Owner management — listing
  # ---------------------------------------------------------------------------

  Scenario: Owner page displays a paginated list
    When I navigate to /petclinic/owner.jsf
    Then I should see a data table of owners with columns: firstName, lastName

  Scenario: Owner page has search functionality
    When I view the owner page
    Then I should see a search input field

  Scenario: Owner search filters results
    When I search for "Duke" on the owner page
    Then only owners matching "Duke" should be displayed

  Scenario: Clear search resets the list
    When I search for "Duke" and then click clear
    Then all owners should be displayed again

  # ---------------------------------------------------------------------------
  # Owner management — CRUD
  # ---------------------------------------------------------------------------

  Scenario: Create a new owner with all valid fields
    When I click "Add New" on the owner page
    And I fill in:
      | firstName   | Duke              |
      | lastName    | Java              |
      | address     | 123 Main St       |
      | houseNumber | 42                |
      | city        | Springfield       |
      | zipCode     | 12345             |
      | phoneNumber | +49 1234 56789012 |
      | email       | duke@example.com  |
    And I click Save
    Then the owner should be created successfully
    And I should see the owner in the list

  Scenario: View owner details
    Given owner "Duke Java" exists
    When I click on "Duke Java" in the owner list
    Then I should see all owner fields displayed
    And I should see the owner's pets and their visits

  Scenario: Edit an existing owner
    Given owner "Duke Java" exists
    When I click Edit on the owner details page
    And I change lastName to "Jakarta"
    And I click Save
    Then the owner's lastName should be "Jakarta"

  Scenario: Delete an owner cascades to pets and visits
    Given owner "Duke Java" exists with pets and visits
    When I delete the owner
    Then the owner should be removed
    And all of the owner's pets should be removed
    And all visits for those pets should be removed

  # ---------------------------------------------------------------------------
  # Owner validation
  # ---------------------------------------------------------------------------

  Scenario: Owner firstName is required
    When I try to create an owner without firstName
    Then a validation error should be displayed

  Scenario: Owner lastName is required
    When I try to create an owner without lastName
    Then a validation error should be displayed

  Scenario: Owner address is required
    When I try to create an owner without address
    Then a validation error should be displayed

  Scenario: Owner houseNumber is required
    When I try to create an owner without houseNumber
    Then a validation error should be displayed

  Scenario: Owner city is required
    When I try to create an owner without city
    Then a validation error should be displayed

  Scenario: Owner zipCode must match pattern
    When I enter zipCode "!!invalid!!"
    Then a validation error for invalid zipCode should be displayed

  Scenario: Valid zipCode formats are accepted
    When I enter zipCode "12345"
    Then the zipCode should pass validation

  Scenario: Owner phoneNumber must match international format
    When I enter phoneNumber "not-a-phone"
    Then a validation error for invalid phoneNumber should be displayed

  Scenario: Valid phone number format is accepted
    When I enter phoneNumber "+49 1234 56789012"
    Then the phoneNumber should pass validation

  Scenario: Owner email must be a valid email
    When I enter email "not-an-email"
    Then a validation error for invalid email should be displayed

  Scenario: Owner email must be unique
    Given owner with email "duke@example.com" exists
    When I try to create another owner with email "duke@example.com"
    Then the creation should fail due to uniqueness constraint

  # ---------------------------------------------------------------------------
  # Pet management
  # ---------------------------------------------------------------------------

  Scenario: Add a pet to an owner
    Given owner "Duke Java" exists
    When I click "Add Pet" on the owner details page
    And I fill in pet name "Rex", birthDate "2020-01-15", and select petType "Dog"
    And I click Save
    Then the pet should be added to the owner

  Scenario: Edit a pet
    Given owner "Duke Java" has pet "Rex"
    When I click Edit on pet "Rex"
    And I change the name to "Rex Jr"
    And I click Save
    Then the pet name should be "Rex Jr"

  Scenario: Pet requires name
    When I try to add a pet without a name
    Then a validation error should be displayed

  Scenario: Pet requires birthDate
    When I try to add a pet without a birthDate
    Then a validation error should be displayed

  Scenario: Pet requires a petType
    When I try to add a pet without selecting a petType
    Then a validation error should be displayed

  # ---------------------------------------------------------------------------
  # Visit management
  # ---------------------------------------------------------------------------

  Scenario: Add a visit to a pet
    Given owner "Duke Java" has pet "Rex"
    When I click "Add Visit" on pet "Rex"
    And I fill in visit date "2024-03-01" and description "Annual checkup"
    And I click Save
    Then the visit should be added to pet "Rex"

  Scenario: Visit requires a date
    When I try to add a visit without a date
    Then a validation error should be displayed

  Scenario: Visit requires a description
    When I try to add a visit without a description
    Then a validation error should be displayed

  # ---------------------------------------------------------------------------
  # PetType management
  # ---------------------------------------------------------------------------

  Scenario: PetType page displays a list
    When I navigate to /petclinic/petType.jsf
    Then I should see a data table of pet types

  Scenario: Create a new pet type
    When I click "Add New" on the pet type page
    And I enter name "Hamster"
    And I click Save
    Then "Hamster" should appear in the pet types list

  Scenario: PetType name must be at least 3 characters
    When I try to create a pet type with name "AB"
    Then a validation error should be displayed

  Scenario: PetType name must be unique
    Given petType "Dog" exists
    When I try to create a petType with name "Dog"
    Then the creation should fail due to uniqueness constraint

  Scenario: Edit a pet type
    Given petType "Hamster" exists
    When I edit the name to "Syrian Hamster"
    Then the petType name should be updated

  Scenario: Delete a pet type
    Given petType "Hamster" exists
    When I delete the petType
    Then "Hamster" should no longer appear in the list

  Scenario: PetType page has search functionality
    When I search for "Dog" on the petType page
    Then only pet types matching "Dog" should be shown

  # ---------------------------------------------------------------------------
  # Veterinarian management
  # ---------------------------------------------------------------------------

  Scenario: Veterinarian page displays a list
    When I navigate to /petclinic/veterinarian.jsf
    Then I should see a data table of vets ordered by lastName, firstName

  Scenario: Create a new vet
    When I click "Add New" on the vet page
    And I enter firstName "James" and lastName "Carter"
    And I click Save
    Then vet "James Carter" should appear in the list

  Scenario: Create a vet with specialties via PickList
    When I create a new vet
    Then I should see a dual-list PickList for specialty selection
    When I move "Radiology" from available to assigned
    And I click Save
    Then the vet should have specialty "Radiology"

  Scenario: Edit a vet's specialties
    Given vet "James Carter" has specialty "Radiology"
    When I edit vet "James Carter" and add specialty "Surgery"
    Then the vet should have specialties "Radiology" and "Surgery"

  Scenario: Vet firstName is required
    When I try to create a vet without firstName
    Then a validation error should be displayed

  Scenario: Vet lastName is required
    When I try to create a vet without lastName
    Then a validation error should be displayed

  Scenario: Vet name combination must be unique
    Given vet "James Carter" exists
    When I try to create another vet with firstName "James" and lastName "Carter"
    Then the creation should fail due to uniqueness constraint

  Scenario: Delete a vet
    When I delete vet "James Carter"
    Then the vet should be removed from the list

  Scenario: Vet page has search functionality
    When I search for "Carter" on the vet page
    Then only vets matching "Carter" should be shown

  # ---------------------------------------------------------------------------
  # Specialty management
  # ---------------------------------------------------------------------------

  Scenario: Specialty page displays a list
    When I navigate to /petclinic/specialty.jsf
    Then I should see a data table of specialties ordered by name

  Scenario: Create a new specialty
    When I click "Add New" on the specialty page
    And I enter name "Radiology"
    And I click Save
    Then "Radiology" should appear in the specialties list

  Scenario: Specialty name must be at least 4 characters
    When I try to create a specialty with name "ABC"
    Then a validation error should be displayed

  Scenario: Specialty name must be unique
    Given specialty "Surgery" exists
    When I try to create a specialty with name "Surgery"
    Then the creation should fail due to uniqueness constraint

  Scenario: Edit a specialty
    Given specialty "Radiology" exists
    When I edit the name to "Diagnostic Radiology"
    Then the specialty name should be updated

  Scenario: Delete a specialty
    When I delete specialty "Radiology"
    Then it should be removed from the list

  Scenario: Specialty page has search functionality
    When I search for "Surgery" on the specialty page
    Then only specialties matching "Surgery" should be shown

  # ---------------------------------------------------------------------------
  # REST API — Specialty
  # ---------------------------------------------------------------------------

  Scenario: GET /rest/specialty/list returns all specialties as JSON
    When I GET /petclinic/rest/specialty/list
    Then the response status should be 200
    And the response should be JSON containing a list of specialties

  Scenario: GET /rest/specialty/{id} returns a specific specialty
    Given specialty with ID 1 exists
    When I GET /petclinic/rest/specialty/1
    Then the response should contain id, uuid, and name

  Scenario: GET /rest/specialty/list+xml returns XML format
    When I GET /petclinic/rest/specialty/list+xml
    Then the response Content-Type should contain "xml"

  # ---------------------------------------------------------------------------
  # REST API — Vet
  # ---------------------------------------------------------------------------

  Scenario: GET /rest/vet/list returns all vets as JSON
    When I GET /petclinic/rest/vet/list
    Then the response status should be 200
    And each vet should have id, uuid, firstName, lastName, specialtyList

  Scenario: GET /rest/vet/{id} returns a specific vet
    Given vet with ID 1 exists
    When I GET /petclinic/rest/vet/1
    Then the response should contain vet details with specialties

  Scenario: GET /rest/vet/list+xml returns XML format
    When I GET /petclinic/rest/vet/list+xml
    Then the response Content-Type should contain "xml"

  # ---------------------------------------------------------------------------
  # REST API — PetType
  # ---------------------------------------------------------------------------

  Scenario: GET /rest/petType/list returns all pet types as JSON
    When I GET /petclinic/rest/petType/list
    Then the response status should be 200
    And each petType should have id, uuid, and name

  Scenario: GET /rest/petType/{id} returns a specific pet type
    Given petType with ID 1 exists
    When I GET /petclinic/rest/petType/1
    Then the response should contain petType details

  Scenario: GET /rest/petType/list+xml returns XML format
    When I GET /petclinic/rest/petType/list+xml
    Then the response Content-Type should contain "xml"

  # ---------------------------------------------------------------------------
  # REST API — Owner
  # ---------------------------------------------------------------------------

  Scenario: GET /rest/owner/list returns all owners as JSON
    When I GET /petclinic/rest/owner/list
    Then the response status should be 200
    And each owner should have id, uuid, firstName, lastName, address, city, zipCode, phoneNumber, petList

  Scenario: GET /rest/owner/{id} returns a specific owner with pets
    Given owner with ID 1 exists
    When I GET /petclinic/rest/owner/1
    Then the response should contain owner details including embedded petList

  Scenario: GET /rest/owner/list+xml returns XML format
    When I GET /petclinic/rest/owner/list+xml
    Then the response Content-Type should contain "xml"

  # ---------------------------------------------------------------------------
  # REST API — Pet
  # ---------------------------------------------------------------------------

  Scenario: GET /rest/pet/list returns all pets as JSON
    When I GET /petclinic/rest/pet/list
    Then the response status should be 200
    And each pet should have id, uuid, name, birthDate, petType, visitList

  Scenario: GET /rest/pet/{id} returns a specific pet with visits
    Given pet with ID 1 exists
    When I GET /petclinic/rest/pet/1
    Then the response should contain pet details including embedded visitList

  Scenario: GET /rest/pet/list+xml returns XML format
    When I GET /petclinic/rest/pet/list+xml
    Then the response Content-Type should contain "xml"

  # ---------------------------------------------------------------------------
  # REST API — Visit
  # ---------------------------------------------------------------------------

  Scenario: GET /rest/visit/list returns all visits as JSON
    When I GET /petclinic/rest/visit/list
    Then the response status should be 200
    And each visit should have id, uuid, date, description

  Scenario: GET /rest/visit/{id} returns a specific visit
    Given visit with ID 1 exists
    When I GET /petclinic/rest/visit/1
    Then the response should contain visit details

  Scenario: GET /rest/visit/list+xml returns XML format
    When I GET /petclinic/rest/visit/list+xml
    Then the response Content-Type should contain "xml"

  # ---------------------------------------------------------------------------
  # Entity structure and constraints
  # ---------------------------------------------------------------------------

  Scenario: All entities have auto-generated IDs
    When a new entity is created
    Then the ID should be auto-generated via a sequence

  Scenario: All entities have a UUID field
    When a new entity is created
    Then a unique UUID should be assigned

  Scenario: Pet has many-to-one to PetType (eager)
    Given a pet exists
    Then the pet should have an associated petType loaded eagerly

  Scenario: Pet has many-to-one to Owner (eager)
    Given a pet exists
    Then the pet should have an associated owner loaded eagerly

  Scenario: Visit has many-to-one to Pet (eager)
    Given a visit exists
    Then the visit should have an associated pet loaded eagerly

  Scenario: Vet has many-to-many with Specialty (eager, via join table)
    Given a vet with specialties exists
    Then the vet's specialties should be loaded eagerly via the vet_specialties join table

  # ---------------------------------------------------------------------------
  # Search functionality
  # ---------------------------------------------------------------------------

  Scenario: All searchable entities maintain a searchindex field
    Then Owner, Vet, PetType, and Specialty entities should have a searchindex text field

  Scenario: Search uses LIKE pattern matching
    When I search for "urg" on any entity page
    Then entities with searchindex containing "urg" should be returned

  # ---------------------------------------------------------------------------
  # REST API is read-only
  # ---------------------------------------------------------------------------

  Scenario: REST API only supports GET methods
    When I POST to /petclinic/rest/owner/list
    Then the response status should be 405 Method Not Allowed

  Scenario: REST API does not support PUT
    When I PUT to /petclinic/rest/owner/1
    Then the response status should be 405 Method Not Allowed

  Scenario: REST API does not support DELETE
    When I DELETE /petclinic/rest/owner/1
    Then the response status should be 405 Method Not Allowed
