package eu.xenit.gradle.enterprise.repository;

import eu.xenit.gradle.enterprise.violations.ViolationHandler;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

/**
 * Replaces specific public and private repositories with an internal artifactory proxy.
 * <p>
 * All not explicitly allowlisted or replaced repositories will be blocked by policy.
 */
public class PrivateRepositoryPlugin extends PrivateRepositoryReplacementPlugin {

    @Override
    protected boolean validateRepository(MavenArtifactRepository repository, Project project,
            ViolationHandler violationHandler) {
        boolean isValidated = super.validateRepository(repository, project, violationHandler);
        if (isValidated) {
            return true;
        }
        violationHandler.handleViolation(new BlockedRepositoryException(repository.getUrl(),
                "Repository is not explicitly allowed or replaced."));
        return false;
    }
}
