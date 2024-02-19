package eu.xenit.gradle.enterprise.conventions.integration.dockerimagelabels;

import static org.junit.Assume.assumeThat;

import eu.xenit.gradle.enterprise.conventions.integration.AbstractIntegrationTest;
import eu.xenit.gradle.enterprise.conventions.integration.GradleVersionCompatibilityMatcher;
import eu.xenit.gradle.enterprise.conventions.integration.JavaVersionCompatibilityMatcher;
import java.io.IOException;
import java.util.Map;
import org.gradle.api.JavaVersion;
import org.gradle.util.GradleVersion;
import org.junit.Before;
import org.junit.Test;

public class DockerImageLabelsIntegrationTest extends AbstractIntegrationTest {

    private static final Map<String, String> GHA_ENV = Map.of(
            "GITHUB_ACTIONS", "true",
            "GITHUB_SERVER_URL", "https://github.example",
            "GITHUB_REPOSITORY", "test/example",
            "GITHUB_SHA", "af554b096e332a30dc90d9e77f42b9fbf1589201"
    );

    @Before
    public void checkGradleVersion() {
        assumeThat(GradleVersion.version(gradleVersion), new GradleVersionCompatibilityMatcher(GradleVersion.version("6.1")));
    }

    @Test
    public void springBootPlugin() throws IOException {
        assumeThat(JavaVersion.current(), new JavaVersionCompatibilityMatcher(JavaVersion.VERSION_17));

        createGradleRunner(integrationTests.resolve("dockerimagelabels/springBootPlugin"))
                .withEnvironment(GHA_ENV)
                .withArguments("check")
                .build();
    }

    @Test
    public void dockerAlfrescoPlugin() throws IOException {
        createGradleRunner(integrationTests.resolve("dockerimagelabels/dockerAlfrescoPlugin"))
                .withEnvironment(GHA_ENV)
                .withArguments("check")
                .build();
    }


}
