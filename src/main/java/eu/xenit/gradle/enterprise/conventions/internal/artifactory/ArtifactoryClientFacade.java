package eu.xenit.gradle.enterprise.conventions.internal.artifactory;

import eu.xenit.gradle.enterprise.conventions.internal.ArtifactoryCredentialsUtil;
import eu.xenit.gradle.enterprise.conventions.internal.StringConstants;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.util.List;
import org.gradle.api.Project;
import org.gradle.cache.CacheRepository;

public class ArtifactoryClientFacade implements ArtifactoryClient {

    private final ArtifactoryClient delegate;

    public ArtifactoryClientFacade(Project project, CacheRepository cacheRepository) {
        URI baseURI = URI.create(StringConstants.XENIT_BASE_URL);
        if (ArtifactoryCredentialsUtil.hasArtifactoryCredentials(project)) {
            HttpClient httpClient = HttpClient.newBuilder()
                    .version(Version.HTTP_1_1)
                    .authenticator(new ArtifactoryHttpAuthenticator(baseURI, project))
                    .build();
            delegate = new CachingArtifactoryClient(new ArtifactoryHttpClient(baseURI, httpClient),
                    cacheRepository, baseURI.toString(),
                    project.getGradle().getStartParameter().isOffline());
        } else {
            delegate = new NullArtifactoryClient();
        }

    }

    @Override
    public List<ArtifactoryRepositorySpec> getRepositories() {
        return delegate.getRepositories();
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
