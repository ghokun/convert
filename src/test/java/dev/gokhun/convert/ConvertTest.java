package dev.gokhun.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static picocli.CommandLine.ExitCode.OK;

import dev.gokhun.convert.Convert.ExecutionExceptionHandler;
import dev.gokhun.convert.Convert.SystemManager;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

final class ConvertTest {

    @Test
    @DisplayName("Should show help when no argument is provided")
    void invalid1() {
        var systemManager = new MockSystemManager();
        systemManager.exit(
                new CommandLine(new Convert())
                        .setOut(systemManager.getOut())
                        .setErr(systemManager.getErr())
                        .setExecutionExceptionHandler(new ExecutionExceptionHandler())
                        .execute());

        assertEquals(OK, systemManager.getExitStatus());
        assertEquals("", systemManager.getOutput());
        assertEquals(
                """
Missing required options: '--input=<input>', '--output=<output>'
Usage: convert [-hV] [--pretty] -i=<input> -o=<output> [-s=<separator>]
  -h, --help              Show this help message and exit.
  -V, --version           Print version information and exit.
  -i, --input=<input>     File to convert from
  -o, --output=<output>   File to convert into
  -s, --separator=<separator>
                          Character(s) to separate CSV columns. Default value is ','.
      --pretty            Prettify output if possible. Default is false and output is minimized.
""",
                systemManager.getError());
    }

    static final class MockSystemManager implements SystemManager {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        private final ByteArrayOutputStream err = new ByteArrayOutputStream();
        private int exitStatus;

        @Override
        public PrintWriter getOut() {
            return new PrintWriter(out);
        }

        @Override
        public PrintWriter getErr() {
            return new PrintWriter(err);
        }

        @Override
        public void exit(int status) {
            exitStatus = status;
        }

        public int getExitStatus() {
            return exitStatus;
        }

        public String getOutput() {
            return out.toString();
        }

        public String getError() {
            return err.toString();
        }
    }
}
