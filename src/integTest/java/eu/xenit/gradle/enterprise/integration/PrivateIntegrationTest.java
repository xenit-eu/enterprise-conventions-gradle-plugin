package eu.xenit.gradle.enterprise.integration;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.junit.Test;

public class PrivateIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void blockedRepositories() throws IOException {
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("private/blockedRepositories"))
                .withArguments("--stacktrace", "--info")
                .buildAndFail();

        assertTrue(buildResult.getOutput().contains("Repository https://jcenter.bintray.com/ is blocked"));
    }

    @Test
    public void rewriteToInternalRepositoryWithoutCredentials() throws IOException {
        BuildResult buildResult = createGradleRunner(
                integrationTests.resolve("private/rewriteToInternalRepositoryWithoutCredentials"))
                .withArguments("--info")
                .build();

        assertTrue(buildResult.getOutput().contains("Xenit Artifactory credentials were not provided."));
    }

    @Test
    public void rewriteToInternalRepository() throws IOException {
        createGradleRunner(integrationTests.resolve("private/rewriteToInternalRepository")).build();
    }
}
