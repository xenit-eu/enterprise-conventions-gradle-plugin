package eu.xenit.gradle.enterprise.integration.repository;

import static org.junit.Assert.assertTrue;

import eu.xenit.gradle.enterprise.integration.AbstractMixedIntegrationTest;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.junit.Test;

public class MixedIntegrationTest extends AbstractMixedIntegrationTest {

    @Test
    public void blockedRepositories() throws IOException {
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("repository/mixed/blockedRepositories"))
                .withArguments("--stacktrace", "--info")
                .buildAndFail();

        assertTrue(buildResult.getOutput().contains("Repository https://jcenter.bintray.com/ is blocked"));
    }

    @Test
    public void httpRepositories() throws IOException {
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("repository/mixed/httpRepositories"))
                .withArguments("--stacktrace", "--info")
                .buildAndFail();

        assertTrue(buildResult.getOutput().contains("HTTPS is required for repositories"));
    }

    @Test
    public void sonatypeRepository() throws IOException {
        createGradleRunner(integrationTests.resolve("repository/mixed/sonatypeRepository")).build();
    }

    @Test
    public void localRepository() throws IOException {
        createGradleRunner(integrationTests.resolve("repository/mixed/localRepository")).build();
    }
}
