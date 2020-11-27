package eu.xenit.gradle.enterprise.integration.repository;

import static org.junit.Assert.assertTrue;

import eu.xenit.gradle.enterprise.integration.AbstractIntegrationTest;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.junit.Test;

public class PrivateIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void otherRepositories() throws IOException {
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("repository/private/otherRepositories"))
                .withArguments("--stacktrace", "--info")
                .buildAndFail();
        assertTrue(buildResult.getOutput().contains("Repository https://example.com/m2/ is blocked"));
    }

    @Test
    public void internalRepository() throws IOException {
        createGradleRunner(integrationTests.resolve("repository/private/internalRepository")).build();
    }

    @Test
    public void rewriteToInternalRepositoryWithoutCredentials() throws IOException {
        BuildResult buildResult = createGradleRunner(
                integrationTests.resolve("repository/private/rewriteToInternalRepositoryWithoutCredentials"))
                .withArguments("--info")
                .buildAndFail();

        assertTrue(buildResult.getOutput().contains("Xenit Artifactory credentials were not provided."));
        assertTrue(buildResult.getOutput().contains("Repository is not explicitly allowed or replaced."));
    }

    @Test
    public void rewriteToInternalRepository() throws IOException {
        createGradleRunner(integrationTests.resolve("repository/private/rewriteToInternalRepository")).build();
    }
}
