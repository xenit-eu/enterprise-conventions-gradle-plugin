package eu.xenit.gradle.enterprise.repository;

import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

/**
 * Maintains a list of explicitly allowed and blocked maven repositories.
 * <p>
 * Explicitly blocked repositories will result in an exception when they are configured.
 * Explicitly allowed repositories are used as a base for allowed repositories in {@link InternalRepositoryPlugin}
 */
public class OssRepositoryPlugin extends AbstractRepositoryPlugin {


    @Override
    protected boolean validateRepository(MavenArtifactRepository repository,
            Project project) {
        if (repository.getUrl().getHost().equals("artifactory.xenit.eu")) {
            throw new BlockedRepositoryException(repository.getUrl(),
                    "Xenit internal artifactory can not be used in OSS projects.");
        }
        return super.validateRepository(repository, project);
    }
}
