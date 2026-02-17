package eu.xenit.gradle.enterprise.conventions.integration.mavencentral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import eu.xenit.gradle.enterprise.conventions.integration.AbstractIntegrationTest;
import java.io.File;
import java.io.IOException;
import lombok.SneakyThrows;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class MavenCentralPublishIntegrationTest extends AbstractIntegrationTest {

    @SneakyThrows
    private void setupGitRepo(File projectDir) {
        exec(projectDir, "git", "init", "--initial-branch", "main");
        exec(projectDir, "git", "config", "user.email", "test@example.com");
        exec(projectDir, "git", "config", "user.name", "Testuser");
        exec(projectDir, "git", "add", "-A");
        exec(projectDir, "git", "commit", "-m", "Initial commit");
        exec(projectDir, "git", "remote", "add", "origin", "https://github.com/xenit-eu/enterprise-conventions-gradle-plugin");
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void publishRelease(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        var gradleRunner = createGradleRunner(integrationTests.resolve("mavencentral/nmcpPublish"));
        var projectDir = gradleRunner.getProjectDir();
        setupGitRepo(projectDir);
        BuildResult buildResult = gradleRunner
                .withArguments("publish", "-PmavenCentralPublishUsername=test", "-PmavenCentralPublishPassword=test", "--stacktrace", "-i")
                .build();

        assertNull(buildResult.task(":publishAggregationToCentralSnapshots")); // task was not executed
        assertEquals(TaskOutcome.SUCCESS, buildResult.task(":publishAggregationToCentralPortal").getOutcome());
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void publishSnapshot(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        var gradleRunner = createGradleRunner(integrationTests.resolve("mavencentral/nmcpPublish"));
        var projectDir = gradleRunner.getProjectDir();
        setupGitRepo(projectDir);
        BuildResult buildResult = gradleRunner
                .withArguments("publish", "-PmavenCentralPublishUsername=test", "-PmavenCentralPublishPassword=test", "-Pversion=0.1-SNAPSHOT", "--stacktrace", "-i")
                .build();

        assertNull(buildResult.task(":publishAggregationToCentralPortal")); // task was not executed
        assertEquals(TaskOutcome.SUCCESS, buildResult.task(":publishAggregationToCentralSnapshots").getOutcome());
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void worksWithoutCredentialsProvided(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        createGradleRunner(integrationTests.resolve("mavencentral/nmcpPublish"))
                .withArguments("jar")
                .build();
    }

    private void exec(File directory, String... args) throws IOException, InterruptedException {

        var exitCode = new ProcessBuilder()
                .command(args)
                .directory(directory)
                .inheritIO()
                .start()
                .waitFor();

        if(exitCode != 0) {
            throw new IOException(String.format("Process failed: %s Exit code %d", String.join(" ", args), exitCode));
        }
    }

}
