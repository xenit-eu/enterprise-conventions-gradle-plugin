package eu.xenit.gradle.enterprise.publish;

import eu.xenit.gradle.enterprise.repository.BlockedRepositoryException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.internal.artifacts.repositories.AuthenticationSupportedInternal;
import org.gradle.plugins.signing.SigningExtension;
import org.gradle.plugins.signing.SigningPlugin;

public class OssPublishPlugin extends AbstractPublishPlugin {

    @Override
    protected void validatePublishRepository(MavenArtifactRepository repository) {
        if (repository.getUrl().getScheme().equals("http")) {
            boolean hasCredentials = true;
            if (repository instanceof AuthenticationSupportedInternal) {
                hasCredentials = ((AuthenticationSupportedInternal) repository).getConfiguredCredentials() != null;
            }
            if (hasCredentials) {
                throw new BlockedRepositoryException(repository.getUrl(),
                        "Publishing to HTTP repositories with credentials is not allowed.");
            }
        }
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
    }
}
