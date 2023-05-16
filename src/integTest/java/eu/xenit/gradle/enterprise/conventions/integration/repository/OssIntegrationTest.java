package eu.xenit.gradle.enterprise.conventions.integration.repository;

import static org.junit.Assert.assertTrue;

import eu.xenit.gradle.enterprise.conventions.integration.AbstractIntegrationTest;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.junit.Test;

public class OssIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void internalArtifactoryRepository() throws IOException {
        BuildResult buildResult = createGradleRunner(
                integrationTests.resolve("repository/oss/internalArtifactoryRepository"))
                .withArguments("--stacktrace", "--info")
                .buildAndFail();

        assertTrue(buildResult.getOutput().contains("Xenit internal artifactory can not be used in OSS projects"));
    }

    @Test
    public void cloudsmithInternalRepository() throws IOException {
        BuildResult buildResult = createGradleRunner(
                integrationTests.resolve("repository/oss/cloudsmithInternalRepository"))
                .withArguments("--stacktrace", "--info")
                .buildAndFail();

        assertTrue(buildResult.getOutput().contains("Xenit private repository can not be used in OSS projects"));
    }

    @Test
    public void otherRepositories() throws IOException {
        createGradleRunner(integrationTests.resolve("repository/oss/otherRepositories")).build();
    }
}
