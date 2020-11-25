package eu.xenit.gradle.enterprise.repository;

import eu.xenit.gradle.enterprise.violations.ViolationHandler;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

/**
 * Maintains a list of explicitly allowed and blocked maven repositories.
 * <p>
 * Explicitly blocked repositories will result in an exception when they are configured.
 * Explicitly allowed repositories are used as a base for allowed repositories in {@link PrivateRepositoryPlugin}
 */
public class OssRepositoryPlugin extends AbstractRepositoryPlugin {


    @Override
    protected boolean validateRepository(MavenArtifactRepository repository,
            Project project, ViolationHandler violationHandler) {
        if (repository.getUrl().toString().startsWith(StringConstants.XENIT_BASE_URL)) {
            violationHandler.handleViolation(new BlockedRepositoryException(repository.getUrl(),
                    "Xenit internal artifactory can not be used in OSS projects."));
            return false;
        }
        return super.validateRepository(repository, project, violationHandler);
    }
}
