package eu.xenit.gradle.enterprise.conventions.integration.signing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import eu.xenit.gradle.enterprise.conventions.integration.AbstractIntegrationTest;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.util.GradleVersion;
import org.junit.Test;

public class OssIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void requiredWithoutConfiguration() throws IOException {
        BuildResult buildResult = createGradleRunner(
                integrationTests.resolve("signing/oss/shared"))
                .withArguments("publish")
                .buildAndFail();

        assertTrue(buildResult.getOutput().contains("No signing configuration is enabled and signing is required."));
    }

    @Test
    public void toMavenLocal() throws IOException {
        BuildResult buildResult = createGradleRunner(
                integrationTests.resolve("signing/oss/shared"))
                .withArguments("publishToMavenLocal")
                .build();

        assertEquals(TaskOutcome.SKIPPED, buildResult.task(":signMavenJavaPublication").getOutcome());
    }

    @Test
    public void withConfiguration() throws IOException {
        BuildResult buildResult = createGradleRunner(
                integrationTests.resolve("signing/oss/shared"))
                .withArguments("publish",
                        "-Psigning.keyId=32C2FC7D",
                        "-Psigning.password=This is a fake key",
                        "-Psigning.secretKeyRingFile=secring.gpg")
                .build();

        assertEquals(TaskOutcome.SUCCESS, buildResult.task(":signMavenJavaPublication").getOutcome());
    }

    @Test
    public void failWhenLeakingCredentials() throws IOException {
        assumeTrue("Gradle version is less than 6.5",
                GradleVersion.version(gradleVersion).compareTo(GradleVersion.version("6.5")) < 0);
        BuildResult buildResult = createGradleRunner(
                integrationTests.resolve("signing/oss/shared"))
                .withArguments("publish", "--debug", "-Psigning.gnupg.keyName=32C2FC7D")
                .buildAndFail();

        assertTrue(buildResult.getOutput()
                .contains("Signing tasks can not be used when INFO or DEBUG logging is enabled on Gradle < 6.5."));

    }

    @Test
    public void dontLeakCredentials() throws IOException {
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
