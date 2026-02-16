package eu.xenit.gradle.enterprise.conventions.integration.mavencentral;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.xenit.gradle.enterprise.conventions.integration.AbstractIntegrationTest;
import eu.xenit.gradle.enterprise.conventions.integration.GradleVersionCompatibilityMatcher;
import java.io.File;
import java.io.IOException;
import lombok.SneakyThrows;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.util.GradleVersion;
import org.hamcrest.CoreMatchers;
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
        var gradleRunner = createGradleRunner(integrationTests.resolve("mavencentral/jreleaserPublish"));
        var projectDir = gradleRunner.getProjectDir();
        setupGitRepo(projectDir);
        BuildResult buildResult = gradleRunner
                .withArguments("publish", "-PmavenCentralPublishUsername=test", "-PmavenCentralPublishPassword=test")
                .build();

        assertEquals(buildResult.task(":publishMavenJavaPublicationToCentralSnapshotsRepository").getOutcome(), TaskOutcome.SKIPPED);
        assertEquals(buildResult.task(":publishMavenJavaPublicationToJReleaserStagingRepository").getOutcome(), TaskOutcome.SUCCESS);
        assertEquals(buildResult.task(":jreleaserDeploy").getOutcome(), TaskOutcome.SUCCESS);
        assertThat(buildResult.getOutput(), CoreMatchers.allOf(
                CoreMatchers.containsString("JReleaser succeeded"),
                CoreMatchers.containsString("Deploying all staged artifacts")
        ));
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void publishSnapshot(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        var gradleRunner = createGradleRunner(integrationTests.resolve("mavencentral/jreleaserPublish"));
        var projectDir = gradleRunner.getProjectDir();
        setupGitRepo(projectDir);
        BuildResult buildResult = gradleRunner
                .withArguments("publish", "-PmavenCentralPublishUsername=test", "-PmavenCentralPublishPassword=test", "-Pversion=0.1-SNAPSHOT")
                .build();

        assertEquals(buildResult.task(":publishMavenJavaPublicationToCentralSnapshotsRepository").getOutcome(), TaskOutcome.SUCCESS);
        assertEquals(buildResult.task(":publishMavenJavaPublicationToJReleaserStagingRepository").getOutcome(), TaskOutcome.SKIPPED);
        assertThat(buildResult.getOutput(), CoreMatchers.containsString("Deploying is not enabled. Skipping"));
    }

    @ParameterizedTest(name = "Gradle v{0}")
    @MethodSource("gradleVersions")
    public void worksWithoutCredentialsProvided(String gradleVersion) throws IOException {
        this.gradleVersion = gradleVersion;
        createGradleRunner(integrationTests.resolve("mavencentral/jreleaserPublish"))
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
