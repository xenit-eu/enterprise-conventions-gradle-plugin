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
    void appliesDefaultsBeforeTheProjectsOwnConfiguration(String gradleVersion) throws IOException {
        assumeSupportedBySpotless(gradleVersion);
        this.gradleVersion = gradleVersion;
        verifySteps("spotless/javaConfig", DEFAULTS + ",importOrder");
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    void letsTheProjectClearTheDefaultsAndDeclareItsOwnSteps(String gradleVersion) throws IOException {
        assumeSupportedBySpotless(gradleVersion);
        this.gradleVersion = gradleVersion;
        verifySteps("spotless/clearsSteps", "importOrder");
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    void failsWhenTheProjectRedeclaresADefaultStep(String gradleVersion) throws IOException {
        assumeSupportedBySpotless(gradleVersion);
        this.gradleVersion = gradleVersion;
        // Spotless rejects the duplicate itself; the project has to clear the steps or pick another name.
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("spotless/clashingStep"))
                .withArguments("realizeSpotlessJava")
                .buildAndFail();

        assertTrue(buildResult.getOutput().contains("Multiple steps with name 'removeUnusedImports'"));
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
    void failsOnDefaultStepsTheProjectsSpotlessVersionDoesNotHave(String gradleVersion) throws IOException {
        assumeSupportedBySpotless(gradleVersion);
        this.gradleVersion = gradleVersion;
        // Spotless 7 has no expandWildcardImports. Applying fewer defaults than prescribed is not an option,
        // so the build fails until the project upgrades Spotless.
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("spotless/olderSpotless"))
                .withArguments("verifySpotlessSteps", "-PexpectedSteps=removeUnusedImports")
                .buildAndFail();

        assertTrue(buildResult.getOutput()
                .contains("Spotless step 'expandWildcardImports' is not supported by the Spotless version"));
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
