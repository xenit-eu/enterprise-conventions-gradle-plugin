package eu.xenit.gradle.enterprise.conventions.publish;

import eu.xenit.gradle.enterprise.conventions.repository.BlockedRepositoryException;
import eu.xenit.gradle.enterprise.conventions.violations.ViolationHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class OssPublishPlugin extends AbstractPublishPlugin {

    private final static Logger LOGGER = Logging.getLogger(OssPublishPlugin.class);

    @Override
    protected void validatePublishRepository(ViolationHandler violationHandler, MavenArtifactRepository repository) {
        if (repository.getUrl().getScheme().equals("http")) {
            violationHandler.handleViolation(new BlockedRepositoryException(repository.getUrl(),
                    "Publishing to HTTP repositories is not allowed."));
        }
    }

}
