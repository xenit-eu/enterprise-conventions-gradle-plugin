package eu.xenit.gradle.enterprise.repository;

import eu.xenit.gradle.enterprise.internal.ArtifactoryCredentialsUtil;
import eu.xenit.gradle.enterprise.internal.StringConstants;
import eu.xenit.gradle.enterprise.internal.artifactory.ArtifactoryClient;
import eu.xenit.gradle.enterprise.internal.artifactory.ArtifactoryHttpClient;
import eu.xenit.gradle.enterprise.internal.artifactory.ArtifactoryRepositorySpec;
import eu.xenit.gradle.enterprise.internal.artifactory.ArtifactoryRepositorySpec.RepositoryType;
import eu.xenit.gradle.enterprise.internal.artifactory.CachingArtifactoryClient;
import eu.xenit.gradle.enterprise.internal.artifactory.NullArtifactoryClient;
import eu.xenit.gradle.enterprise.violations.ViolationHandler;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
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
    private final CacheRepository cacheRepository;

    private ArtifactoryClient artifactoryClient;

    @Inject
    public PrivateRepositoryReplacementPlugin(CacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    @Override
    public void apply(Project project) {
        URI baseURI = URI.create(StringConstants.XENIT_BASE_URL);
        if (ArtifactoryCredentialsUtil.hasArtifactoryCredentials(project)) {
            HttpClient httpClient = HttpClient.newBuilder()
                    .version(Version.HTTP_1_1)
                    .authenticator(new ArtifactoryHttpAuthenticator(baseURI, project))
                    .build();
            this.artifactoryClient = new CachingArtifactoryClient(new ArtifactoryHttpClient(baseURI, httpClient),
                    cacheRepository, baseURI.toString(),
                    project.getGradle().getStartParameter().isOffline());
        } else {
            this.artifactoryClient = new NullArtifactoryClient();
        }
        super.apply(project);
    }

    private Map<URI, String> getReplacements() {
        Map<URI, String> replacements = new HashMap<>();

        List<ArtifactoryRepositorySpec> repositories = artifactoryClient.getRepositories();

        for (ArtifactoryRepositorySpec repository : repositories) {
            if (repository.getType() == RepositoryType.REMOTE) {
                String remoteUrl = repository.getUrl();
                while (remoteUrl.endsWith("/")) {
                    remoteUrl = remoteUrl.substring(0, remoteUrl.length() - 1);
                }
                replacements.put(URI.create(remoteUrl), repository.getKey());
            }
        }

        withEndingSlash(replacements);
        return replacements;
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

        String replacement = getReplacements().get(repository.getUrl());

        // If we have replacements, we have artifactory credentials (as that's how we get the list of replacements in the first place)
        if (replacement != null) {
            LOGGER.debug("Replacing repository {} with enterprise repository", repository.getUrl());
            repository.setUrl(URI.create(StringConstants.XENIT_BASE_URL + replacement));
            repository.credentials(ArtifactoryCredentialsUtil.configureArtifactoryCredentials(project));
            return ValidationResult.ALLOWED;
        }

        return ValidationResult.NEUTRAL;
    }

    private static class ArtifactoryHttpAuthenticator extends Authenticator {

        private final URI baseURI;
        private final Project project;

        public ArtifactoryHttpAuthenticator(URI baseURI, Project project) {
            this.baseURI = baseURI;
            this.project = project;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            if (ArtifactoryCredentialsUtil.hasArtifactoryCredentials(project) && getRequestingHost()
                    .equals(baseURI.getHost())) {
                return new PasswordAuthentication(
                        project.property(ArtifactoryCredentialsUtil.USERNAME_PROPERTY).toString(),
                        project.property(ArtifactoryCredentialsUtil.PASSWORD_PROPERTY).toString().toCharArray()
                );
            }
            return null;
        }
    }
}
