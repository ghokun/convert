Feature: Conversion options

  Scenario: Can convert input files to output files
    Given temporary directory exists for keeping output files
    When convert is run with given input and output arguments
      | --input                | --output                |
      | files/input/mini1.json | mini1_actual.yaml       |
      | files/input/mini1.json | mini1_actual.properties |
      | files/input/mini1.json | mini1_actual.toml       |
    Then following files contain same content
      | actual                  | expected                      |
      | mini1_actual.yaml       | files/output/mini1.yaml       |
      | mini1_actual.properties | files/output/mini1.properties |
      | mini1_actual.toml       | files/output/mini1.toml       |