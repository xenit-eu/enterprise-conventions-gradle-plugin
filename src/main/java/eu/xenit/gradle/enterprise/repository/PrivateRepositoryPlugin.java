package eu.xenit.gradle.enterprise.repository;

import eu.xenit.gradle.enterprise.internal.ArtifactoryCredentialsUtil;
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
        if (ArtifactoryCredentialsUtil.hasArtifactoryCredentials(project)) {
            // Only block repositories from being used when we have Artifactory credentials
            // (AKA we are a Xenit developer)
            // Let other developers use whatever repositories they want, as they are not even
            // allowed to list the repositories we replace, they need a way to use those repositories
            // directly.
            violationHandler.handleViolation(new BlockedRepositoryException(repository.getUrl(),
                    "Repository is not explicitly allowed or replaced."));
            return ValidationResult.BLOCKED;
        } else {
            return ValidationResult.NEUTRAL;
        }
    }
}
