package eu.xenit.gradle.enterprise.conventions.extensions.repository;

import de.marcphilipp.gradle.nexus.NexusRepository;
import java.net.URI;
import java.util.Set;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.ActionConfiguration;
import org.gradle.api.artifacts.ComponentMetadataSupplier;
import org.gradle.api.artifacts.ComponentMetadataVersionLister;
import org.gradle.api.artifacts.repositories.AuthenticationContainer;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenRepositoryContentDescriptor;
import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.gradle.api.artifacts.repositories.RepositoryContentDescriptor;
import org.gradle.api.credentials.Credentials;
import org.gradle.api.model.ObjectFactory;

/**
 * Fake {@link MavenArtifactRepository} that delegates credential setting functionality to {@link NexusRepository} so
 * publishing to maven central can happen reliably while keeping the same feel as configuring other repositories to publish to.
 */
public class SonatypeMavenCentralPublishRepository implements MavenArtifactRepository {

    public class LimitedMavenArtifactRepositoryException extends UnsupportedOperationException {

        public LimitedMavenArtifactRepositoryException() {
            super("The repository " + SonatypeMavenCentralPublishRepository.this.getName()
                    + " only supports configuring credentials.");
        }
    }

    private final NexusRepository nexusRepository;
    private final PasswordCredentials passwordCredentials;

    @Inject
    public SonatypeMavenCentralPublishRepository(ObjectFactory objectFactory, NexusRepository nexusRepository) {
        this.nexusRepository = nexusRepository;
        this.passwordCredentials = objectFactory.newInstance(WrappedPasswordCredentials.class, nexusRepository);
    }

    @Override
    public URI getUrl() {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public void setUrl(URI uri) {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public void setUrl(Object o) {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public boolean isAllowInsecureProtocol() {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public void setAllowInsecureProtocol(boolean b) {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public Set<URI> getArtifactUrls() {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public void artifactUrls(Object... objects) {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public void setArtifactUrls(Set<URI> set) {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public void setArtifactUrls(Iterable<?> iterable) {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public void metadataSources(Action<? super MetadataSources> action) {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public void mavenContent(Action<? super MavenRepositoryContentDescriptor> action) {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public String getName() {
        return nexusRepository.getName();
    }

    @Override
    public void setName(String s) {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public void content(Action<? super RepositoryContentDescriptor> action) {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public PasswordCredentials getCredentials() {
        return this.passwordCredentials;
    }

    @Override
    public <T extends Credentials> T getCredentials(Class<T> aClass) {
        if (aClass.equals(PasswordCredentials.class)) {
            return (T) getCredentials();
        } else {
            throw new LimitedMavenArtifactRepositoryException();
        }
    }

    @Override
    public void credentials(Action<? super PasswordCredentials> action) {
        action.execute(getCredentials());
    }

    @Override
    public <T extends Credentials> void credentials(Class<T> aClass, Action<? super T> action) {
        action.execute(getCredentials(aClass));
    }

    @Override
    public void authentication(Action<? super AuthenticationContainer> action) {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public AuthenticationContainer getAuthentication() {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public void setMetadataSupplier(Class<? extends ComponentMetadataSupplier> aClass) {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public void setMetadataSupplier(Class<? extends ComponentMetadataSupplier> aClass,
            Action<? super ActionConfiguration> action) {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public void setComponentVersionsLister(Class<? extends ComponentMetadataVersionLister> aClass) {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public void setComponentVersionsLister(Class<? extends ComponentMetadataVersionLister> aClass,
            Action<? super ActionConfiguration> action) {
        throw new LimitedMavenArtifactRepositoryException();
    }

    public static class WrappedPasswordCredentials implements PasswordCredentials {

        private final NexusRepository nexusRepository;

        @Inject
        public WrappedPasswordCredentials(NexusRepository nexusRepository) {
            this.nexusRepository = nexusRepository;
        }

        @Override
        public String getUsername() {
            return nexusRepository.getUsername().get();
        }

        @Override
        public void setUsername(String s) {
            nexusRepository.getUsername().set(s);
        }

        @Override
        public String getPassword() {
            return nexusRepository.getPassword().get();
        }

        @Override
        public void setPassword(String s) {
            nexusRepository.getPassword().set(s);
        }
    }
}
