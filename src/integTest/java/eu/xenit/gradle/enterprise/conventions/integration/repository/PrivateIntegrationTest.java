package eu.xenit.gradle.enterprise.conventions.integration.repository;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

import eu.xenit.gradle.enterprise.conventions.integration.AbstractIntegrationTest;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.util.GradleVersion;
import org.junit.Test;

public class PrivateIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void cloudsmithInternalRepository() throws IOException {
        createGradleRunner(integrationTests.resolve("repository/private/cloudsmithInternalRepository")).build();
    }

    @Test
    public void cloudsmithInternalRepositorySettings() throws IOException {
        assumeTrue("Repository management in settings is supported from Gradle 6.8", GradleVersion.version(gradleVersion).compareTo(GradleVersion.version("6.8")) >= 0);
        createGradleRunner(integrationTests.resolve("repository/private/cloudsmithInternalRepositorySettings")).build();
    }

    @Test
    public void internalRepository() throws IOException {
        createGradleRunner(integrationTests.resolve("repository/private/internalRepository")).build();
    }
}
