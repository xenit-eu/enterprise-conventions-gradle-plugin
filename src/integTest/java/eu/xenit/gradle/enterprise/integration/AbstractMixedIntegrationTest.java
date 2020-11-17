package eu.xenit.gradle.enterprise.integration;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.LineIterator;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public abstract class AbstractMixedIntegrationTest extends AbstractIntegrationTest {

    @Parameters(name = "Gradle v{0} - {1}")
    public static Collection<Object[]> testData() {
        Collection<Object[]> versions = AbstractIntegrationTest.testData();

        String[] variants = {
                "private",
                "oss"
        };

        List<Object[]> parameters = new ArrayList<>();

        for (Object[] gradleVersion : versions) {
            for (String variant : variants) {
                parameters.add(new Object[]{gradleVersion[0], variant});
            }
        }
        return parameters;
    }

    @Parameter(1)
    public String variant;

    protected GradleRunner createGradleRunner(Path projectFolder) throws IOException {
        GradleRunner baseRunner = super.createGradleRunner(projectFolder);

        Path buildGradle = testProjectDir.getRoot().toPath().resolve("build.gradle");

        boolean hasPlugins = false;
        boolean hasInjected = false;
        List<String> buildGradleLines = new ArrayList<>();
        try (LineIterator lineIterator = new LineIterator(Files.newBufferedReader(buildGradle))) {
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                if (line.matches("plugins\\s*\\{")) {
                    hasPlugins = true;
                } else if (!hasInjected && (line.contains("}") || !line.matches("\\s*id\\s+[\"']"))) {
                    hasInjected = true;
                    if (!hasPlugins) {
                        buildGradleLines.add("plugins {");
                    }
                    buildGradleLines.add("id 'eu.xenit.enterprise." + variant + "'");
                    if (!hasPlugins) {
                        buildGradleLines.add("}");
                    }
                }
                buildGradleLines.add(line);
            }
        }

        try (BufferedWriter buildGradleWriter = Files.newBufferedWriter(buildGradle)) {
            for (String line : buildGradleLines) {
                buildGradleWriter.write(line + "\n");
            }
        }

        return baseRunner;
    }
}
