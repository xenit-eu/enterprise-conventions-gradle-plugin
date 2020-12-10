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
    public void otherRepositories() throws IOException {
        createGradleRunner(integrationTests.resolve("repository/oss/otherRepositories")).build();
    }

    @Test
    public void otherRepositoriesWithPrivateInit() throws IOException {
        createGradleRunner(integrationTests.resolve("repository/oss/otherRepositoriesWithPrivateInit"))
                .withTestKitDir(testProjectDir.getRoot().toPath().resolve("gradleHome").toFile())
                .build();
    }

    @Test
    public void otherRepositoriesWithPrivateInitWithoutCredentials() throws IOException {
        createGradleRunner(
                integrationTests.resolve("repository/oss/otherRepositoriesWithPrivateInitWithoutCredentials"))
                .withTestKitDir(testProjectDir.getRoot().toPath().resolve("gradleHome").toFile())
                .build();
    }
}
