package eu.xenit.gradle.enterprise.repository;

import eu.xenit.gradle.enterprise.violations.ViolationHandler;
import javax.inject.Inject;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.cache.CacheRepository;

/**
 * Replaces specific public and private repositories with an internal artifactory proxy.
 * <p>
 * All not explicitly allowlisted or replaced repositories will be blocked by policy.
 */
public class PrivateRepositoryPlugin extends PrivateRepositoryReplacementPlugin {

    @Inject
    public PrivateRepositoryPlugin(CacheRepository cacheRepository) {
        super(cacheRepository);
    }

    @Override
    protected ValidationResult validateRepository(MavenArtifactRepository repository, Project project,
            ViolationHandler violationHandler) {
        ValidationResult validationResult = super.validateRepository(repository, project, violationHandler);
        if (validationResult.isFinal()) {
            return validationResult;
        }
        violationHandler.handleViolation(new BlockedRepositoryException(repository.getUrl(),
                "Repository is not explicitly allowed or replaced."));
        return ValidationResult.BLOCKED;
    }
}
