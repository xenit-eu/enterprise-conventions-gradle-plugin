package eu.xenit.gradle.enterprise.conventions.publish;

import eu.xenit.gradle.enterprise.conventions.repository.BlockedRepositoryException;
import eu.xenit.gradle.enterprise.conventions.violations.ViolationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.credentials.Credentials;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;
import org.gradle.internal.artifacts.repositories.AuthenticationSupportedInternal;
import org.gradle.plugins.signing.SigningExtension;
import org.gradle.plugins.signing.SigningPlugin;
import org.gradle.util.GradleVersion;

public class OssPublishPlugin extends AbstractPublishPlugin {

    private Logger LOGGER = Logging.getLogger(OssPublishPlugin.class);

    @Override
    protected void validatePublishRepository(ViolationHandler violationHandler, MavenArtifactRepository repository) {
        if (repository.getUrl().getScheme().equals("http")) {
            boolean hasCredentials = checkHasCredentials(repository);
            if (hasCredentials) {
                violationHandler.handleViolation(new BlockedRepositoryException(repository.getUrl(),
                        "Publishing to HTTP repositories with credentials is not allowed."));
            }
        }
    }

    private boolean checkHasCredentials(MavenArtifactRepository repository) {
        try {
            return checkHasCredentials0(repository);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.warn(
                    "Failed to determine if repository has credentials and is HTTP. Continuing as if no credentials are configured.");
            return false;
        }
    }

    private boolean checkHasCredentials0(MavenArtifactRepository repository)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (repository instanceof AuthenticationSupportedInternal) {
            Method method = repository.getClass().getMethod("getConfiguredCredentials");
            // Internals have changed starting from Gradle 6.6
            if (GradleVersion.current().compareTo(GradleVersion.version("6.6")) < 0) {
                return method.invoke(repository) != null;
            } else {
                Property<Credentials> credentialsProperty = (Property<Credentials>) method.invoke(repository);
                return credentialsProperty.isPresent();
            }
        }

        return true;
    }

    @Override
    protected void configurePublication(Project project, PublishingExtension publishing) {
        project.getPlugins().apply(SigningPlugin.class);
        configureSigning(project);
    }

    private void configureSigning(Project project) {
        SigningExtension signing = project.getExtensions().getByType(SigningExtension.class);
        PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
        signing.sign(publishing.getPublications());

        // When SIGNING_PRIVATE_KEY and SIGNING_PASSWORD environment variables are present, sign using an in-memory PGP key
        String privateKey = System.getenv("SIGNING_PRIVATE_KEY");
        String password = System.getenv("SIGNING_PASSWORD");
        if (privateKey != null && password != null) {
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
