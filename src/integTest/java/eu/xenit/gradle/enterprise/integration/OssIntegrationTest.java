package eu.xenit.gradle.enterprise.integration;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.junit.Test;

public class OssIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void blockedRepositories() throws IOException {
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("oss/blockedRepositories"))
                .withArguments("--stacktrace", "--info")
                .buildAndFail();

        assertTrue(buildResult.getOutput().contains("Repository https://jcenter.bintray.com/ is blocked"));
    }

    @Test
    public void internalArtifactoryRepository() throws IOException {
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("oss/internalArtifactoryRepository"))
                .withArguments("--stacktrace", "--info")
                .buildAndFail();

        assertTrue(buildResult.getOutput().contains("Xenit internal artifactory can not be used in OSS projects"));
    }

    @Test
    public void sonatypeRepository() throws IOException {
        createGradleRunner(integrationTests.resolve("oss/sonatypeRepository")).build();
    }

    @Test
    public void localRepository() throws IOException {
        createGradleRunner(integrationTests.resolve("oss/localRepository")).build();
    }
}
