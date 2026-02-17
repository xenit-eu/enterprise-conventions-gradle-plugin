package eu.xenit.gradle.enterprise.conventions.integration.signing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.xenit.gradle.enterprise.conventions.integration.AbstractIntegrationTest;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class OssIntegrationTest extends AbstractIntegrationTest {

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void requiredWithoutConfiguration(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        BuildResult buildResult = createGradleRunner(
                integrationTests.resolve("signing/oss/shared"))
                .withArguments("publish")
                .buildAndFail();

        assertTrue(buildResult.getOutput().contains("No signing configuration is enabled and signing is required."));
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void toMavenLocal(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        BuildResult buildResult = createGradleRunner(
                integrationTests.resolve("signing/oss/shared"))
                .withArguments("publishToMavenLocal")
                .build();

        assertEquals(TaskOutcome.SKIPPED, buildResult.task(":signMavenJavaPublication").getOutcome());
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void withConfiguration(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        BuildResult buildResult = createGradleRunner(
                integrationTests.resolve("signing/oss/shared"))
                .withArguments("publish",
                        "-Psigning.keyId=32C2FC7D",
                        "-Psigning.password=This is a fake key",
                        "-Psigning.secretKeyRingFile=secring.gpg")
                .build();

        assertEquals(TaskOutcome.SUCCESS, buildResult.task(":signMavenJavaPublication").getOutcome());
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void dontLeakCredentials(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        BuildResult buildResult = createGradleRunner(
                integrationTests.resolve("signing/oss/shared"))
                .withArguments("publish", "--info",
                        // Set executable to false so the build is guaranteed to always fail, but not before maybe logging the commandline
                        "-Psigning.gnupg.executable=false",
                        "-Psigning.gnupg.keyName=32C2FC7D",
                        "-Psigning.gnupg.passphrase=This is a fake key"
                )
                .buildAndFail();

        // Ensure that no output ever contains the passphrase
        assertFalse(buildResult.getOutput().contains("This is a fake key"));
    }
}
