package eu.xenit.gradle.enterprise.conventions.repository;

import eu.xenit.gradle.enterprise.conventions.internal.StringConstants;
import eu.xenit.gradle.enterprise.conventions.violations.ViolationHandler;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

/**
 * Maintains a list of explicitly allowed and blocked maven repositories.
 * <p>
 * Explicitly blocked repositories will result in an exception when they are configured.
 */
public class OssRepositoryPlugin extends RepositoryPlugin {

    @Override
    protected ValidationResult validateRepository(MavenArtifactRepository repository,
            Project project, ViolationHandler violationHandler) {
        if (repository.getUrl().toString().startsWith(StringConstants.XENIT_BASE_URL)) {
            violationHandler.handleViolation(new BlockedRepositoryException(repository.getUrl(),
                    "Xenit internal artifactory can not be used in OSS projects."));
            return ValidationResult.BLOCKED;
        } else if(repository.getUrl().toString().startsWith(StringConstants.XENIT_REPO_BASE_URL)) {
            violationHandler.handleViolation(new BlockedRepositoryException(repository.getUrl(), "Xenit private repository can not be used in OSS projects."));
            return ValidationResult.BLOCKED;
        }
        return super.validateRepository(repository, project, violationHandler);
    }
}
