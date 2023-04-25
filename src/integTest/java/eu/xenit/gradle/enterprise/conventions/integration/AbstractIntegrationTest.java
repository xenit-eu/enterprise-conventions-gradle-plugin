package eu.xenit.gradle.enterprise.conventions.integration;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.internal.DefaultGradleRunner;
import org.gradle.util.GradleVersion;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public abstract class AbstractIntegrationTest {

    protected final Path integrationTests;

    {
        try {
            integrationTests = Paths.get(AbstractIntegrationTest.class.getResource("x").toURI()).getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Parameters(name = "Gradle v{0}")
    public static Collection<Object[]> testData() {
        if (Boolean.getBoolean("eu.xenit.enterprise.conventions.integration.gradle-offline")) {
            return Collections.singletonList(new Object[]{GradleVersion.current().getVersion()});
        }
        String[] gradleVersions = new String[]{
                "8.0.2",
                "7.6.1",
                "7.5.1",
                "7.0",
                "6.9.1",
                "6.8.1",
                "6.7.1",
                "6.6.1",
                "6.5.1",
                "6.4.1",
                "6.3",
                "6.2.2",
                "6.1.1",
                "6.0.1",
                "5.6.4",
                "5.5.1",
                "5.4.1",
                "5.3.1",
        };

        List<Object[]> parameters = new ArrayList<>();

        for (String gradleVersion : gradleVersions) {
            parameters.add(new Object[]{gradleVersion});
        }
        Collections.shuffle(parameters);
        return parameters;
    }

    @Parameter(0)
    public String gradleVersion;

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();


    protected GradleRunner createGradleRunner(Path projectFolder) throws IOException {
        FileUtils.copyDirectory(projectFolder.toFile(), testProjectDir.getRoot());

        GradleRunner gradleRunner = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir.getRoot())
                .forwardOutput();

        if (!Boolean.getBoolean("eu.xenit.enterprise.integration.gradle-offline")) {
            gradleRunner.withGradleVersion(gradleVersion);
        }

        // Configure java commandline options so integration tests are run with coverage information
        String[] myCommandLine = ProcessHandle.current().info().arguments().get();
        List<String> agentOpts = Arrays.stream(myCommandLine)
                .filter(arg -> arg.startsWith("-javaagent"))
                .map(agent -> agent.replace("build/", System.getProperty("user.dir") + "/build/"))
                .collect(Collectors.toList());
        // Override default artifactory URL with our test fake
        agentOpts.add(String
                .format("-Deu.xenit.gradle.enterprise.conventions.artifactory-override=http://%s:%s/artifactory/",
                        System.getProperty("artifactory.host"),
                        System.getProperty("artifactory.tcp.80")));
        agentOpts.add(String.format("-Deu.xenit.gradle.enterprise.conventions.integration.plugin-classpath=%s",
                gradleRunner.getPluginClasspath().stream().map(
                        File::toString).collect(Collectors.joining(":"))));
        ((DefaultGradleRunner) gradleRunner).withJvmArguments(agentOpts);

        return gradleRunner;
    }
}
