package eu.xenit.gradle.enterprise.conventions.integration;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.internal.DefaultGradleRunner;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.io.TempDir;

public abstract class AbstractIntegrationTest {

    protected final Path integrationTests;

    {
        try {
            integrationTests = Paths.get(AbstractIntegrationTest.class.getResource("x").toURI()).getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Stream<String> gradleVersions() {
        if (Boolean.getBoolean("eu.xenit.enterprise.conventions.integration.gradle-offline")) {
            return Stream.of(GradleVersion.current().getVersion());
        }
        String[] versions = new String[]{
                "9.0.0",
                "8.14.3",
        };
        List<String> list = Arrays.asList(versions);
        Collections.shuffle(list);
        return list.stream();
    }

    protected String gradleVersion;

    @TempDir
    protected Path testProjectDir;


    protected GradleRunner createGradleRunner(Path projectFolder) throws IOException {
        FileUtils.copyDirectory(projectFolder.toFile(), testProjectDir.toFile());

        GradleRunner gradleRunner = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir.toFile())
                .forwardOutput();

        if (!Boolean.getBoolean("eu.xenit.enterprise.integration.gradle-offline")) {
            gradleRunner.withGradleVersion(gradleVersion);
        }

        // Configure java commandline options so integration tests are run with coverage information
        String[] myCommandLine = ProcessHandle.current().info().arguments().get();
        List<String> agentOpts = Arrays.stream(myCommandLine)
                .filter(arg -> arg.startsWith("-javaagent"))
                .collect(Collectors.toList());
        agentOpts.add(String.format("-Deu.xenit.gradle.enterprise.conventions.integration.plugin-classpath=%s",
                gradleRunner.getPluginClasspath().stream().map(
                        File::toString).collect(Collectors.joining(":"))));
        ((DefaultGradleRunner) gradleRunner).withJvmArguments(agentOpts);

        return gradleRunner;
    }
}
