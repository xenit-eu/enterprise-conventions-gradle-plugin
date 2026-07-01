package eu.xenit.gradle.enterprise.conventions.integration.dockermultiarch;

import eu.xenit.gradle.enterprise.conventions.integration.AbstractIntegrationTest;
import java.io.IOException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DockerMultiArchIntegrationTest extends AbstractIntegrationTest {

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void registersCombineTaskAndLeavesImageNameByDefault(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        createGradleRunner(integrationTests.resolve("dockermultiarch/springBootPlugin"))
                .withArguments("check")
                .build();
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void appliesArchSuffixWhenImagePlatformIsSet(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        createGradleRunner(integrationTests.resolve("dockermultiarch/springBootPlugin"))
                .withArguments("check", "-PimagePlatform=linux/amd64")
                .build();
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void isAutoAppliedByConventionsPlugin(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        createGradleRunner(integrationTests.resolve("dockermultiarch/autoApply"))
                .withArguments("check")
                .build();
    }
}
