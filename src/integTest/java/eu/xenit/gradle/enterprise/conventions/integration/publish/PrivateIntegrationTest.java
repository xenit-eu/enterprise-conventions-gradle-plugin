package eu.xenit.gradle.enterprise.conventions.integration.publish;

import static org.junit.Assert.assertTrue;

import eu.xenit.gradle.enterprise.conventions.integration.AbstractIntegrationTest;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.junit.Test;

public class PrivateIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void internalRepository() throws IOException {
        createGradleRunner(integrationTests.resolve("publish/private/internalRepository")).build();
    }

    @Test
    public void publicRepository() throws IOException {
        BuildResult buildResult = createGradleRunner(
                integrationTests.resolve("publish/private/publicRepository")).buildAndFail();

        assertTrue(buildResult.getOutput()
                .contains("Only publishing to Xenit private or to local repository is allowed"));
    }
}
