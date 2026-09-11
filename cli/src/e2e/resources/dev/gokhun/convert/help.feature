Feature: Help options

  Scenario: Convert is run without arguments
    When convert is run without arguments
    Then missing options are shown
    And help output is shown

  Scenario: Convert is run with -h option
    When convert is run with -h option
    Then help output is shown
