package eu.xenit.gradle.enterprise.conventions.integration.publish;

import static org.junit.Assert.assertTrue;

import eu.xenit.gradle.enterprise.conventions.integration.AbstractIntegrationTest;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.junit.Test;

public class OssIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void httpRepository() throws IOException {
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("publish/oss/httpRepository"))
                .buildAndFail();
        assertTrue(buildResult.getOutput().contains("Publishing to HTTP repositories is not allowed"));
    }

    @Test
    public void httpsRepositoryWithCredentials() throws IOException {
        createGradleRunner(integrationTests.resolve("publish/oss/httpsRepositoryWithCredentials")).build();
    }

    @Test
    public void mavenCentral() throws IOException {
        createGradleRunner(integrationTests.resolve("publish/oss/mavenCentral")).build();
    }

    @Test
    public void mavenCentralNewUrl() throws IOException {
        createGradleRunner(integrationTests.resolve("publish/oss/mavenCentralNewUrl")).build();
    }

    @Test
    public void sonatypeSnapshots() throws IOException {
        createGradleRunner(integrationTests.resolve("publish/oss/sonatypeSnapshots")).build();
    }

}
