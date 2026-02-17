package eu.xenit.gradle.enterprise.conventions.integration.dockerimagelabels;

import eu.xenit.gradle.enterprise.conventions.integration.AbstractIntegrationTest;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DockerImageLabelsIntegrationTest extends AbstractIntegrationTest {

    private static final Map<String, String> GHA_ENV = Map.of(
            "GITHUB_ACTIONS", "true",
            "GITHUB_SERVER_URL", "https://github.example",
            "GITHUB_REPOSITORY", "test/example",
            "GITHUB_SHA", "af554b096e332a30dc90d9e77f42b9fbf1589201"
    );

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void springBootPlugin(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        createGradleRunner(integrationTests.resolve("dockerimagelabels/springBootPlugin"))
                .withEnvironment(GHA_ENV)
                .withArguments("check")
                .build();
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void dockerAlfrescoPlugin(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        createGradleRunner(integrationTests.resolve("dockerimagelabels/dockerAlfrescoPlugin"))
                .withEnvironment(GHA_ENV)
                .withArguments("check")
                .build();
    }


}
