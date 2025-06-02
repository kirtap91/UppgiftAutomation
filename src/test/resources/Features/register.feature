Feature: User Registration on Basketball England

  Scenario: Create user – all fields valid
    Given I am on the registration page
    When I fill in all mandatory fields correctly
    And I accept the terms and conditions
    And I click Register
    Then the account should be created and I should see a confirmation message

  Scenario: Create user – missing last name
    Given I am on the registration page
    When I fill in all fields except last name
    And I accept the terms and conditions
    And I click Register
    Then I should see an error message about missing last name

  Scenario: Create user – passwords do not match
    Given I am on the registration page
    When I fill in the password and confirmation with different values
    And I accept the terms and conditions
    And I click Register
    Then I should see an error message about password mismatch

  Scenario: Create user – terms and conditions not accepted
    Given I am on the registration page
    When I fill in all mandatory fields correctly
    And I do not accept the terms and conditions
    And I click Register
    Then I should see an error message about accepting terms and conditions

  Scenario Outline: Invalid registration scenarios
    Given I am on the registration page
    When I fill in "<field>" with "<value>"
    And I accept the terms and conditions
    And I click Register
    Then I should see error message "<expectedError>"

    Examples:
      | field           | value     | expectedError                                       |
      | last name       | (empty)   | Last Name is required                               |
      | confirmPassword | wrongPass | Password did not match                              |
