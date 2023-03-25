package dev.gokhun.convert;

import static dev.gokhun.convert.ProcessHelper.RESOURCES_DIR;
import static dev.gokhun.convert.ProcessHelper.runCommand;
import static org.assertj.core.api.Assertions.assertThat;

import dev.gokhun.convert.ProcessHelper.ProcessResult;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConversionSteps {
    private static final String INPUT = "--input";
    private static final String OUTPUT = "--output";
    private static final String ACTUAL = "actual";
    private static final String EXPECTED = "expected";
    private File tempDir;

    @Given("temporary directory exists for keeping output files")
    public void temporaryDirectoryExistsForKeepingOutputFiles() throws IOException {
        tempDir = Files.createTempDirectory("convert-e2e-").toFile();
        tempDir.deleteOnExit();
        assertThat(tempDir).exists().isDirectory();
    }

    @When("convert is run with given input and output arguments")
    public void convertIsRunWithGivenInputAndOutputArguments(DataTable dataTable) {
        dataTable
                .asMaps()
                .forEach(
                        row -> {
                            File outputFile = getFile(tempDir.getAbsolutePath(), row.get(OUTPUT));
                            ProcessResult result =
                                    runCommand(
                                            "convert",
                                            INPUT,
                                            row.get(INPUT),
                                            OUTPUT,
                                            outputFile.getAbsolutePath());
                            assertThat(result.exitCode()).isEqualTo(0);
                            assertThat(outputFile).exists().isFile();
                        });
    }

    @Then("following files contain same content")
    public void followingFilesContainSameContent(DataTable dataTable) {
        dataTable
                .asMaps()
                .forEach(
                        row ->
                                assertThat(getFile(tempDir.getAbsolutePath(), row.get(ACTUAL)))
                                        .exists()
                                        .isFile()
                                        .hasSameTextualContentAs(
                                                getFile(RESOURCES_DIR, row.get(EXPECTED))));
    }

    private static File getFile(String first, String... more) {
        return Path.of(first, more).toFile();
    }
}
