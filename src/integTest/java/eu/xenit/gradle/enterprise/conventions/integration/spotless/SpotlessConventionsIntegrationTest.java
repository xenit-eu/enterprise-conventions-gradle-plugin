package eu.xenit.gradle.enterprise.conventions.integration.spotless;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import eu.xenit.gradle.enterprise.conventions.integration.AbstractIntegrationTest;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SpotlessConventionsIntegrationTest extends AbstractIntegrationTest {

    // Spotless names the expandWildcardImports step in lowercase, unlike the method that adds it.
    private static final String DEFAULTS = "removeUnusedImports,expandwildcardimports";

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    void appliesDefaultsWhenTheProjectHasNoSpotlessConfiguration(String gradleVersion) throws IOException {
        assumeSupportedBySpotless(gradleVersion);
        this.gradleVersion = gradleVersion;
        verifySteps("spotless/noSpotlessConfig", DEFAULTS);
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    void mergesDefaultsIntoTheProjectsOwnConfiguration(String gradleVersion) throws IOException {
        assumeSupportedBySpotless(gradleVersion);
        this.gradleVersion = gradleVersion;
        // The defaults are appended after what the project configured, never in front of it.
        verifySteps("spotless/javaConfig", "importOrder," + DEFAULTS);
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    void leavesADefaultStepAloneWhenTheProjectDeclaresItItself(String gradleVersion) throws IOException {
        assumeSupportedBySpotless(gradleVersion);
        this.gradleVersion = gradleVersion;
        // Spotless rejects a duplicate step name outright, so this building at all is part of the assertion.
        verifySteps("spotless/declaresDefaultItself", DEFAULTS);
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    void appliesDefaultsWhenTheProjectConfiguresOnlyANonJavaFormat(String gradleVersion) throws IOException {
        assumeSupportedBySpotless(gradleVersion);
        this.gradleVersion = gradleVersion;
        verifySteps("spotless/nonJavaConfigOnly", DEFAULTS);
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    void appliesNoDefaultStepThatIsOptedOutOf(String gradleVersion) throws IOException {
        assumeSupportedBySpotless(gradleVersion);
        this.gradleVersion = gradleVersion;
        verifySteps("spotless/stepOptOut", "removeUnusedImports");
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    void touchesNothingWhenTheConventionsAreDisabled(String gradleVersion) throws IOException {
        assumeSupportedBySpotless(gradleVersion);
        this.gradleVersion = gradleVersion;
        createGradleRunner(integrationTests.resolve("spotless/allOptOut"))
                .withArguments("verifyNoJavaFormat")
                .build();
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    void skipsDefaultStepsTheProjectsSpotlessVersionDoesNotHave(String gradleVersion) throws IOException {
        assumeSupportedBySpotless(gradleVersion);
        this.gradleVersion = gradleVersion;
        // Spotless 7 has no expandWildcardImports: it is skipped with a warning instead of breaking the build.
        BuildResult buildResult = verifySteps("spotless/olderSpotless", "removeUnusedImports");

        assertTrue(buildResult.getOutput().contains("Spotless step 'expandWildcardImports' is not supported"));
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    void skipsProjectsWithoutTheJavaPlugin(String gradleVersion) throws IOException {
        assumeSupportedBySpotless(gradleVersion);
        this.gradleVersion = gradleVersion;
        createGradleRunner(integrationTests.resolve("spotless/noJavaPlugin"))
                .withArguments("verifyNoJavaFormat")
                .build();
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    void reportsViolationWhenAppliedAfterTheJavaFormatIsConfigured(String gradleVersion) throws IOException {
        assumeSupportedBySpotless(gradleVersion);
        this.gradleVersion = gradleVersion;
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("spotless/appliedTooLate"))
                .withArguments("help")
                .buildAndFail();

        assertTrue(buildResult.getOutput().contains("Spotless conventions were not applied to project ':'"));
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    void violationForLateApplicationCanBeDisabled(String gradleVersion) throws IOException {
        assumeSupportedBySpotless(gradleVersion);
        this.gradleVersion = gradleVersion;
        createGradleRunner(integrationTests.resolve("spotless/appliedTooLate"))
                .withArguments("help", "-Peu.xenit.enterprise-conventions.violations.spotless=disable")
                .build();
    }

    private BuildResult verifySteps(String project, String expectedSteps) throws IOException {
        return createGradleRunner(integrationTests.resolve(project))
                .withArguments("verifySpotlessSteps", "-PexpectedSteps=" + expectedSteps)
                .build();
    }

    // The test projects use Spotless 8.10 (7.2 for olderSpotless), which requires Gradle >= 8.1.
    private static void assumeSupportedBySpotless(String gradleVersion) {
        assumeTrue(GradleVersion.version(gradleVersion).compareTo(GradleVersion.version("8.1")) >= 0,
                "Spotless 8 requires Gradle >= 8.1");
    }
}
