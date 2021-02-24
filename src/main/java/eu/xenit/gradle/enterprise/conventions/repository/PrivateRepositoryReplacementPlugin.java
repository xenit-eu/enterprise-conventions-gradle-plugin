package eu.xenit.gradle.enterprise.conventions.repository;

import eu.xenit.gradle.enterprise.conventions.internal.ArtifactoryCredentialsUtil;
import eu.xenit.gradle.enterprise.conventions.internal.StringConstants;
import eu.xenit.gradle.enterprise.conventions.internal.artifactory.ArtifactoryClient;
import eu.xenit.gradle.enterprise.conventions.internal.artifactory.ArtifactoryClientFacade;
import eu.xenit.gradle.enterprise.conventions.internal.artifactory.ArtifactoryRepositorySpec;
import eu.xenit.gradle.enterprise.conventions.internal.artifactory.ArtifactoryRepositorySpec.RepositoryType;
import eu.xenit.gradle.enterprise.conventions.violations.ViolationHandler;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.cache.CacheRepository;

/**
 * Replaces specific public and private repositories with an internal artifactory proxy.
 */
public class PrivateRepositoryReplacementPlugin extends AbstractRepositoryPlugin {

    private static final Logger LOGGER = Logging.getLogger(PrivateRepositoryPlugin.class);
    private volatile static Map<URI, URI> replacementsCache = null;
    private static final Object REPLACEMENTS_CACHE_LOCK = new Object();
    private final CacheRepository cacheRepository;

    protected ArtifactoryClient artifactoryClient;

    @Inject
    public PrivateRepositoryReplacementPlugin(CacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    @Override
    public void apply(Project project) {
        artifactoryClient = new ArtifactoryClientFacade(project, cacheRepository);
        super.apply(project);
    }

    private Map<URI, URI> getReplacements() {
        if (replacementsCache == null) {
            synchronized (REPLACEMENTS_CACHE_LOCK) {
                if (replacementsCache == null) {
                    Map<URI, URI> replacements = new HashMap<>();

                    List<ArtifactoryRepositorySpec> repositories = artifactoryClient.getRepositories();

                    for (ArtifactoryRepositorySpec repository : repositories) {
                        if (repository.getType() == RepositoryType.REMOTE) {
                            String remoteUrl = repository.getUrl();
                            while (remoteUrl.endsWith("/")) {
                                remoteUrl = remoteUrl.substring(0, remoteUrl.length() - 1);
                            }
                            replacements.put(URI.create(remoteUrl), URI.create(repository.getProxyUrl()));
                        }
                    }

                    withEndingSlash(replacements);
                    replacementsCache = replacements;
                }
            }
        }
        return replacementsCache;
    }

    @Override
    protected ValidationResult validateRepository(MavenArtifactRepository repository, Project project,
            ViolationHandler violationHandler) {
        if (repository.getUrl().toString().startsWith(StringConstants.XENIT_BASE_URL)) {
            LOGGER.debug("Allowing enterprise repository: {}", repository.getUrl());
            return ValidationResult.ALLOWED;
        }

        ValidationResult validationResult = super.validateRepository(repository, project, violationHandler);
        if (validationResult.isFinal()) {
            return validationResult;
        }

        URI replacement = getReplacements().get(repository.getUrl());

        // If we have replacements, we have artifactory credentials (as that's how we get the list of replacements in the first place)
        if (replacement != null) {
            LOGGER.debug("Replacing repository {} with enterprise repository {}", repository.getUrl(), replacement);
            repository.setUrl(replacement);
            repository.credentials(ArtifactoryCredentialsUtil.configureArtifactoryCredentials(project));
            return ValidationResult.ALLOWED;
        }

        return ValidationResult.NEUTRAL;
    }

}
