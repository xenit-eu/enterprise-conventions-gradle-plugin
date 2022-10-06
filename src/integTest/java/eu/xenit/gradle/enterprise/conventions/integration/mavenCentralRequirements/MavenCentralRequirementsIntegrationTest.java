package eu.xenit.gradle.enterprise.conventions.integration.mavenCentralRequirements;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import eu.xenit.gradle.enterprise.conventions.integration.AbstractIntegrationTest;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.junit.Test;

public class MavenCentralRequirementsIntegrationTest extends AbstractIntegrationTest {
    @Test
    public void withoutSigning() throws IOException {
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("mavenCentralRequirements/withoutSigning"))
                .withArguments("publish")
                .buildAndFail();
        assertTrue(buildResult.getOutput().contains("Artifact 'integration-test-1.0.jar' must be signed, but signature 'integration-test-1.0.jar.asc' is missing."));
        assertTrue(buildResult.getOutput().contains("Artifact 'integration-test-1.0.pom' must be signed, but signature 'integration-test-1.0.pom.asc' is missing."));
        assertTrue(buildResult.getOutput().contains("Artifact 'integration-test-1.0-sources.jar' must be signed, but signature 'integration-test-1.0-sources.jar.asc' is missing."));
    }

    @Test
    public void withoutSources() throws IOException {
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("mavenCentralRequirements/withoutSources"))
                .withArguments("publish")
                .buildAndFail();
        assertTrue(buildResult.getOutput().contains("Publication is missing required artifact 'integration-test-1.0-sources.jar'"));
        assertTrue(buildResult.getOutput().contains("Publication is missing required artifact 'integration-test-1.0-javadoc.jar'"));
    }

    @Test
    public void pomOnlyWithoutSources() throws IOException {
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("mavenCentralRequirements/pomOnlyWithoutSources"))
                .withArguments("publish")
                .buildAndFail();
        assertFalse(buildResult.getOutput().contains("Publication is missing required artifact"));
    }

    @Test
    public void withoutPom() throws IOException {
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("mavenCentralRequirements/withoutPom"))
                .withArguments("publish")
                .buildAndFail();
        assertTrue(buildResult.getOutput().contains("Property 'developers' is required but is empty"));
        assertTrue(buildResult.getOutput().contains("Property 'description' is required but is absent"));
    }

    @Test
    public void everythingOk() throws IOException {
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("mavenCentralRequirements/everythingOk"))
                .withArguments("publish")
                .buildAndFail();
        assertFalse(buildResult.getOutput().contains("Policy violation"));
    }
}
