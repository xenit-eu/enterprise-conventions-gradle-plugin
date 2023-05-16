package eu.xenit.gradle.enterprise.conventions.publish;

import eu.xenit.gradle.enterprise.conventions.internal.StringConstants;
import eu.xenit.gradle.enterprise.conventions.violations.ViolationHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

public class PrivatePublishPlugin extends AbstractPublishPlugin {

    @Override
    protected void validatePublishRepository(ViolationHandler violationHandler, MavenArtifactRepository repository) {
        // Publishing to local file repositories is always allowed
        if ("file".equals(repository.getUrl().getScheme())) {
            return;
        }

        // Allow private artifactory repository
        if (repository.getUrl().toString().startsWith(StringConstants.XENIT_BASE_URL) || repository.getUrl().toString().equals(StringConstants.XENIT_REPO_PUBLISH_URL)) {
            return;
        }

        violationHandler.handleViolation(new BlockedPublishRepositoryException(repository.getUrl(),
                "Only publishing to Xenit private or to local repository is allowed."));
    }
}
