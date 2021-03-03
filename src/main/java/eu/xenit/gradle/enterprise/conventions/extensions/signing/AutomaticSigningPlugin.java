package eu.xenit.gradle.enterprise.conventions.extensions.signing;

import eu.xenit.gradle.enterprise.conventions.api.PluginApi;
import eu.xenit.gradle.enterprise.conventions.api.PublicApi;
import eu.xenit.gradle.enterprise.conventions.extensions.signing.internal.DefaultSigningMethodConfiguration;
import eu.xenit.gradle.enterprise.conventions.extensions.signing.internal.GnupgSigningMethodConfiguration;
import eu.xenit.gradle.enterprise.conventions.extensions.signing.internal.InMemorySigningMethodConfiguration;
import eu.xenit.gradle.enterprise.conventions.extensions.signing.internal.SelectingSigningMethodConfiguration;
import eu.xenit.gradle.enterprise.conventions.extensions.signing.internal.SigningMethodConfiguration;
import eu.xenit.gradle.enterprise.conventions.violations.ViolationHandler;
import java.util.Arrays;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
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
import org.gradle.util.GradleVersion;

@PublicApi
public class AutomaticSigningPlugin implements Plugin<Project> {

    @PluginApi
    public static final String PLUGIN_ID = "eu.xenit.enterprise.ext.signing";
    private static final Logger LOGGER = Logging.getLogger(AutomaticSigningPlugin.class);

    @Override
    public void apply(Project project) {
        project.getPlugins().withType(MavenPublishPlugin.class, mavenPublishPlugin -> {
            project.getPlugins().withType(SigningPlugin.class, signingPlugin -> {
                configureSigning(project);
                checkSigningSecurity(project);
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

    /**
     * Check for  GHSA-ww7h-4fx5-8c2j security issue
     * {@link https://github.com/gradle/gradle/security/advisories/GHSA-ww7h-4fx5-8c2j}
     */
    private void checkSigningSecurity(Project project) {
        ViolationHandler violationHandler = ViolationHandler.fromProject(project, "signing");
        if (GradleVersion.current().compareTo(GradleVersion.version("6.5")) >= 0) {
            return;
        }
        if (!LOGGER.isInfoEnabled()) {
            return;
        }
        project.getGradle().getTaskGraph().whenReady(taskExecutionGraph -> {
            if (taskExecutionGraph.getAllTasks()
                    .stream()
                    .anyMatch(task -> task instanceof Sign && ((Sign) task).getSignatory().getClass().getSimpleName()
                            .equals("GnupgSignatory"))) {
                violationHandler.handleViolation(new GradleException(
                        "Signing tasks can not be used when INFO or DEBUG logging is enabled on Gradle < 6.5.\n" +
                                "For details, see security advisory: https://github.com/gradle/gradle/security/advisories/GHSA-ww7h-4fx5-8c2j"));
            }
        });
    }

    private static class CheckSigningConfiguration implements Action<Task> {

        private final Sign sign;
        private final SigningMethodConfiguration signingKeyConfiguration;

        public CheckSigningConfiguration(Sign sign, SigningMethodConfiguration signingKeyConfiguration) {
            this.sign = sign;
            this.signingKeyConfiguration = signingKeyConfiguration;
        }

        @Override
        public void execute(Task task) {
            if (sign.getSignatory() == null) {
                throw new InvalidUserDataException(
                        "No signing configuration is enabled and signing is required. Provide " +
                                signingKeyConfiguration.getRequiredConfigs());
            }
        }
    }
}
