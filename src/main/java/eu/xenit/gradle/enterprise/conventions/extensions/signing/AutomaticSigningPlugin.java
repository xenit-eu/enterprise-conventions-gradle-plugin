package eu.xenit.gradle.enterprise.conventions.extensions.signing;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;
import org.gradle.plugins.signing.SigningExtension;
import org.gradle.plugins.signing.SigningPlugin;

public class AutomaticSigningPlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = "eu.xenit.enterprise.ext.signing";
    private static final Logger LOGGER = Logging.getLogger(AutomaticSigningPlugin.class);
    private final static String SIGNING_PRIVATE_KEY_ENV = "SIGNING_PRIVATE_KEY";
    private final static String SIGNING_PASSWORD_ENV = "SIGNING_PASSWORD";

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

        // When SIGNING_PRIVATE_KEY and SIGNING_PASSWORD environment variables are present, sign using an in-memory PGP key
        String privateKey = System.getenv(SIGNING_PRIVATE_KEY_ENV);
        String password = System.getenv(SIGNING_PASSWORD_ENV);
        if (privateKey != null && password != null) {
            LOGGER.debug("Found private key and password from environment variables {} & {}", SIGNING_PRIVATE_KEY_ENV,
                    SIGNING_PASSWORD_ENV);
            signing.useInMemoryPgpKeys(privateKey, password);
        }
        signing.setRequired(project.provider(() -> isSigningRequired(project)));
    }

    private boolean isSigningRequired(Project project) {
        TaskExecutionGraph taskGraph = project.getGradle().getTaskGraph();
        // Only set signing to required when non-mavenlocal repositories are being published to.
        return project.getTasks().withType(PublishToMavenRepository.class).stream().anyMatch(taskGraph::hasTask);
    }
}
