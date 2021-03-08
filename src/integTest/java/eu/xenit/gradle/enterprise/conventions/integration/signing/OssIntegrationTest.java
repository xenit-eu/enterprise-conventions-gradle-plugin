package eu.xenit.gradle.enterprise.conventions.integration.signing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import eu.xenit.gradle.enterprise.conventions.integration.AbstractIntegrationTest;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
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
}
