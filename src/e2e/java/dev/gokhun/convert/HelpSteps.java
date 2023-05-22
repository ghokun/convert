package dev.gokhun.convert;

import static dev.gokhun.convert.ProcessHelper.runCommand;
import static org.assertj.core.api.Assertions.assertThat;

import dev.gokhun.convert.ProcessHelper.ProcessResult;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public final class HelpSteps {
    private static final String HELP_OUTPUT =
            """
Usage: convert [-hV] [--deduplicate-keys] [--indent-yaml] [--minimize-yaml-quotes] [--pretty] -f=<input> -o=<output>
               [-s=<csvSeparator>]
Converts one file type to another.
  -h, --help               Show this help message and exit.
  -V, --version            Print version information and exit.
  -f, -i, --from, --input=<input>
                           File to convert from.
  -o, -t, --to, --output=<output>
                           File to convert into.
  -s, --csv-separator=<csvSeparator>
                           Character(s) to separate CSV columns. Default value is ','.
      --pretty             Prettify output if possible. Default is false and output is minimized.
      --indent-yaml        Indents YAML array fields. Default is true.
      --minimize-yaml-quotes
                           Minimizes YAML quotes if possible. Default is true.
      --deduplicate-keys   Think csv but as json. Only available on conversions where the initial source is array. Keys
                             are arrays and values are arrays of arrays.
            """;
    private ProcessResult result;

    @When("convert is run without arguments")
    public void convertIsRunWithoutArguments() {
        result = runCommand("convert");
    }

    @When("convert is run with -h option")
    public void convertIsRunWithHOption() {
        result = runCommand("convert", "-h");
    }

    @Then("missing options are shown")
    public void missingOptionsAreShown() {
        assertThat(result.exitCode()).isEqualTo(2);
        assertThat(result.output()).isNotEmpty();
        assertThat(result.output())
                .containsIgnoringNewLines("Missing required options: '--input=<input>', '--output=<output>'");
    }

    @And("help output is shown")
    public void helpOutputIsShown() {
        assertThat(result.output()).containsIgnoringNewLines(HELP_OUTPUT);
    }
}
