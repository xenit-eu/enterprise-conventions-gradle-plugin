package eu.xenit.gradle.enterprise.publish;

import eu.xenit.gradle.enterprise.repository.BlockedRepositoryException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.publish.PublishingExtension;

public class PrivatePublishPlugin extends AbstractPublishPlugin {

    @Override
    protected void validatePublishRepository(MavenArtifactRepository repository) {
        // Publishing to local file repositories is always allowed
        if ("file".equals(repository.getUrl().getScheme())) {
            return;
        }

        // Allow private artifactory repository
        if ("artifactory.xenit.eu".equals(repository.getUrl().getHost()) && "https"
                .equals(repository.getUrl().getScheme())) {
            return;
        }

        throw new BlockedRepositoryException(repository.getUrl(),
                "Only publishing to internal artifactory or to local repository is allowed.");
    }

    @Override
    protected void configurePublication(Project project, PublishingExtension publishing) {

    }
}
