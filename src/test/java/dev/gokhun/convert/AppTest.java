package dev.gokhun.convert;

import org.junit.jupiter.api.Test;

class AppTest {
    @Test
    void appHasAGreeting() {
        App classUnderTest = new App();
        App.main("-i", "src/test/resources/input.json", "-o", "src/test/resources/output.yaml");
        App.main(
                "-i",
                "src/test/resources/output.yaml",
                "-o",
                "src/test/resources/output.properties");
        App.main(
                "-i",
                "src/test/resources/output.properties",
                "-o",
                "src/test/resources/output.toml");
    }
}
