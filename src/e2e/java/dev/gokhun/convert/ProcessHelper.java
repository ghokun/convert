package dev.gokhun.convert;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableList;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

final class ProcessHelper {
    private static final String USER_HOME = "user.home";
    private static final String OS_NAME = "os.name";
    private static final File HOME_DIR = new File(System.getProperty(USER_HOME));

    private ProcessHelper() {}

    static ProcessResult runCommand(String command, String... args) {
        ProcessBuilder processBuilder =
                new ProcessBuilder()
                        .redirectErrorStream(true)
                        .command(
                                ImmutableList.<String>builder()
                                        .add(buildCommand(command))
                                        .add(args)
                                        .build())
                        .directory(HOME_DIR);
        try {
            Process process = processBuilder.start();
            return new ProcessResult(
                    process.waitFor(), clearAnsiFormatting(readOutput(process.getInputStream())));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    record ProcessResult(int exitCode, String output) {}

    private static String readOutput(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String[] buildCommand(String command) {
        return isWindows()
                ? new String[] {"cmd", "/c", command + ".exe"}
                : new String[] {"sh", "-c", command};
    }

    private static boolean isWindows() {
        return System.getProperty(OS_NAME).toLowerCase(Locale.ENGLISH).startsWith("windows");
    }

    private static String clearAnsiFormatting(String coloredText) {
        return coloredText.replaceAll("\\e\\[[\\d;]*[^\\d;]", "");
    }
}
