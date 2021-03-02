package eu.xenit.gradle.enterprise.conventions.extensions.signing;

import eu.xenit.gradle.enterprise.conventions.extensions.signing.internal.DefaultSigningMethodConfiguration;
import eu.xenit.gradle.enterprise.conventions.extensions.signing.internal.GnupgSigningMethodConfiguration;
import eu.xenit.gradle.enterprise.conventions.extensions.signing.internal.InMemorySigningMethodConfiguration;
import eu.xenit.gradle.enterprise.conventions.extensions.signing.internal.SelectingSigningMethodConfiguration;
import eu.xenit.gradle.enterprise.conventions.extensions.signing.internal.SigningMethodConfiguration;
import java.util.Arrays;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;
import org.gradle.plugins.signing.Sign;
import org.gradle.plugins.signing.SigningExtension;
import org.gradle.plugins.signing.SigningPlugin;

public class AutomaticSigningPlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = "eu.xenit.enterprise.ext.signing";
    private static final Logger LOGGER = Logging.getLogger(AutomaticSigningPlugin.class);

    @Override
    public void apply(Project project) {
        project.getPlugins().withType(MavenPublishPlugin.class, mavenPublishPlugin -> {
            project.getPlugins().withType(SigningPlugin.class, signingPlugin -> {
                configureSigning(project);
            });
        });
    }

    private void configureSigning(Project project) {
        SigningExtension signing = project.getExtensions().getByType(SigningExtension.class);
        PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
        signing.sign(publishing.getPublications());

        SigningMethodConfiguration signingKeyConfiguration = new SelectingSigningMethodConfiguration(Arrays.asList(
                new InMemorySigningMethodConfiguration(),
                new DefaultSigningMethodConfiguration(project),
                new GnupgSigningMethodConfiguration(project)
        ));

        LOGGER.debug("Signing method {} (required {}, optional {})", signingKeyConfiguration.getName(),
                signingKeyConfiguration.getRequiredConfigs(), signingKeyConfiguration.getOptionalConfigs());

        if (signingKeyConfiguration.isEnabled()) {
            signingKeyConfiguration.configureSigning(signing);
        }

        project.getTasks().withType(Sign.class).configureEach(sign ->
                sign.doFirst("Check for missing signing configuration",
                        new CheckSigningConfiguration(sign, signingKeyConfiguration))
        );

        signing.setRequired(project.provider(() -> isSigningRequired(project)));
    }

    private boolean isSigningRequired(Project project) {
        TaskExecutionGraph taskGraph = project.getGradle().getTaskGraph();
        // Only set signing to required when non-mavenlocal repositories are being published to.
        return project.getTasks().withType(PublishToMavenRepository.class).stream()
                .anyMatch(taskGraph::hasTask);
    }
}
