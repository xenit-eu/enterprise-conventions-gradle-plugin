package eu.xenit.gradle.enterprise.conventions.repository;

import eu.xenit.gradle.enterprise.conventions.internal.StringConstants;
import eu.xenit.gradle.enterprise.conventions.internal.artifactory.ArtifactoryRepositorySpec;
import eu.xenit.gradle.enterprise.conventions.internal.artifactory.ArtifactoryRepositorySpec.RepositoryType;
import eu.xenit.gradle.enterprise.conventions.violations.ViolationHandler;
import java.util.List;
import javax.inject.Inject;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.cache.CacheRepository;

/**
 * Maintains a list of explicitly allowed and blocked maven repositories.
 * <p>
 * Explicitly blocked repositories will result in an exception when they are configured.
 * Explicitly allowed repositories are used as a base for allowed repositories in {@link PrivateRepositoryPlugin}
 */
public class OssRepositoryPlugin extends PrivateRepositoryReplacementPlugin {

    @Inject
    public OssRepositoryPlugin(CacheRepository cacheRepository) {
        super(cacheRepository);
    }

    private boolean isProxiedRepository(MavenArtifactRepository repository) {
        List<ArtifactoryRepositorySpec> repositories = artifactoryClient.getRepositories();
        for (ArtifactoryRepositorySpec repositorySpec : repositories) {
            if (repository.getUrl().toString().equals(repositorySpec.getProxyUrl())
                    && repositorySpec.getType() == RepositoryType.REMOTE) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected ValidationResult validateRepository(MavenArtifactRepository repository,
            Project project, ViolationHandler violationHandler) {
        if (repository.getUrl().toString().startsWith(StringConstants.XENIT_BASE_URL) && !isProxiedRepository(
                repository)) {
            violationHandler.handleViolation(new BlockedRepositoryException(repository.getUrl(),
                    "Xenit internal artifactory can not be used in OSS projects."));
            return ValidationResult.BLOCKED;
        }
        return super.validateRepository(repository, project, violationHandler);
    }
}
