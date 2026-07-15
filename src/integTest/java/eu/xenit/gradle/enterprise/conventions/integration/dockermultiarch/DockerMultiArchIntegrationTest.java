package eu.xenit.gradle.enterprise.conventions.integration.dockermultiarch;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import eu.xenit.gradle.enterprise.conventions.integration.AbstractIntegrationTest;
import java.io.IOException;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class DockerMultiArchIntegrationTest extends AbstractIntegrationTest {

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    void derivesPerArchTasksFromBootBuildImage(String gradleVersion) throws IOException {
        assumeSupportedBySpringBoot(gradleVersion);
        this.gradleVersion = gradleVersion;
        createGradleRunner(integrationTests.resolve("dockermultiarch/springBootPlugin"))
                .withArguments("check")
                .build();
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    void keepsUnsuffixedTagsWithAbsentTagsProperty(String gradleVersion) throws IOException {
        assumeSupportedBySpringBoot(gradleVersion);
        this.gradleVersion = gradleVersion;
        createGradleRunner(integrationTests.resolve("dockermultiarch/springBootPlugin"))
                .withArguments("check", "-PreleaseIdiom")
                .build();
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    void toleratesSpringBootPluginAppliedBeforeJavaPlugin(String gradleVersion) throws IOException {
        assumeSupportedBySpringBoot(gradleVersion);
        this.gradleVersion = gradleVersion;
        createGradleRunner(integrationTests.resolve("dockermultiarch/bootBeforeJava"))
                .withArguments("check")
                .build();
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    void isAutoAppliedByConventionsPlugin(String gradleVersion) throws IOException {
        assumeSupportedBySpringBoot(gradleVersion);
        this.gradleVersion = gradleVersion;
        createGradleRunner(integrationTests.resolve("dockermultiarch/autoApply"))
                .withArguments("check")
                .build();
    }

    // The test projects use Spring Boot 3.5 (the extension needs its imagePlatform support, added in 3.4),
    // and the Spring Boot 3.4+ Gradle plugin requires at least Gradle 8.4.
    private static void assumeSupportedBySpringBoot(String gradleVersion) {
        assumeTrue(GradleVersion.version(gradleVersion).compareTo(GradleVersion.version("8.4")) >= 0,
                "Spring Boot 3.5 requires Gradle >= 8.4");
    }
}
