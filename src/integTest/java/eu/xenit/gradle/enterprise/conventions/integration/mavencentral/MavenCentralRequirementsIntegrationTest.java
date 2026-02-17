package eu.xenit.gradle.enterprise.conventions.integration.mavencentral;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.xenit.gradle.enterprise.conventions.integration.AbstractIntegrationTest;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class MavenCentralRequirementsIntegrationTest extends AbstractIntegrationTest {
    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void withoutSigning(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("mavencentral/withoutSigning"))
                .withArguments("publish")
                .buildAndFail();
        assertTrue(buildResult.getOutput().contains("Publication 'mavenJava': Artifact 'integration-test-1.0.jar' must be signed, but signature 'integration-test-1.0.jar.asc' is missing."));
        assertTrue(buildResult.getOutput().contains("Publication 'mavenJava': Artifact 'integration-test-1.0.pom' must be signed, but signature 'integration-test-1.0.pom.asc' is missing."));
        assertTrue(buildResult.getOutput().contains("Publication 'mavenJava': Artifact 'integration-test-1.0-sources.jar' must be signed, but signature 'integration-test-1.0-sources.jar.asc' is missing."));
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void withoutSources(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("mavencentral/withoutSources"))
                .withArguments("publish")
                .buildAndFail();
        assertTrue(buildResult.getOutput().contains("Publication 'mavenJava': missing required artifact 'integration-test-1.0-sources.jar'"));
        assertTrue(buildResult.getOutput().contains("Publication 'mavenJava': missing required artifact 'integration-test-1.0-javadoc.jar'"));
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void pomOnlyWithoutSources(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("mavencentral/pomOnlyWithoutSources"))
                .withArguments("publish")
                .buildAndFail();
        assertFalse(buildResult.getOutput().contains("Publication 'mavenJava': missing required artifact"));
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void withoutPom(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("mavencentral/withoutPom"))
                .withArguments("publish")
                .buildAndFail();
        assertTrue(buildResult.getOutput().contains("Publication 'mavenJava': POM property 'developers' is required but is empty"));
        assertTrue(buildResult.getOutput().contains("Publication 'mavenJava': POM property 'description' is required but is absent"));
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void withoutPomTaskCheck(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("mavencentral/withoutPom"))
                .withArguments("checkMavenCentralRequirements")
                .buildAndFail();
        assertTrue(buildResult.getOutput().contains("Publication 'mavenJava': POM property 'developers' is required but is empty"));
        assertTrue(buildResult.getOutput().contains("Publication 'mavenJava': POM property 'description' is required but is absent"));
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void everythingOk(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        BuildResult buildResult = createGradleRunner(integrationTests.resolve("mavencentral/nmcpPublish"))
                .withArguments("publish")
                .buildAndFail();
        assertFalse(buildResult.getOutput().contains("Policy violation"));
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void everythingOkTaskCheck(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        createGradleRunner(integrationTests.resolve("mavencentral/nmcpPublish"))
                .withArguments("checkMavenCentralRequirements", "--stacktrace")
                .build();
    }
}
