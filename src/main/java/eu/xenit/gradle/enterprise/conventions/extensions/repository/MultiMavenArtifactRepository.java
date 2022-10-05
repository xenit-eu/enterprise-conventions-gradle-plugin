package eu.xenit.gradle.enterprise.conventions.extensions.repository;

import java.net.URI;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.gradle.api.Action;
import org.gradle.api.ActionConfiguration;
import org.gradle.api.artifacts.ComponentMetadataSupplier;
import org.gradle.api.artifacts.ComponentMetadataVersionLister;
import org.gradle.api.artifacts.repositories.AuthenticationContainer;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenRepositoryContentDescriptor;
import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.gradle.api.artifacts.repositories.RepositoryContentDescriptor;
import org.gradle.api.artifacts.repositories.UrlArtifactRepository;
import org.gradle.api.credentials.Credentials;

public class MultiMavenArtifactRepository implements MavenArtifactRepository {
    public class LimitedMavenArtifactRepositoryException extends UnsupportedOperationException {

        public LimitedMavenArtifactRepositoryException() {
            super("The repository " + MultiMavenArtifactRepository.this.getName()
                    + " does not support this operation.");
        }
    }

    private Set<? extends MavenArtifactRepository> wrappedRepositories;

    class WrappedPasswordCredentials implements PasswordCredentials {

        @Nullable
        @Override
        public String getUsername() {
            return findPrimary().getCredentials().getUsername();
        }

        @Override
        public void setUsername(@Nullable String s) {
            wrappedRepositories.forEach(repo -> repo.getCredentials().setUsername(s));
        }

        @Nullable
        @Override
        public String getPassword() {
            return findPrimary().getCredentials().getPassword();
        }

        @Override
        public void setPassword(@Nullable String s) {
            wrappedRepositories.forEach(repo -> repo.getCredentials().setPassword(s));
        }
    }

    public MultiMavenArtifactRepository(Set<? extends MavenArtifactRepository> wrappedRepositories) {
        this.wrappedRepositories = wrappedRepositories;
    }

    private MavenArtifactRepository findPrimary() {
        return this.wrappedRepositories.stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No wrapped repositories, can not retrieve primary repository"));
    }

    @Override
    public URI getUrl() {
        return findPrimary().getUrl();
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
        return wrappedRepositories.stream().anyMatch(UrlArtifactRepository::isAllowInsecureProtocol);
    }

    @Override
    public void setAllowInsecureProtocol(boolean b) {
        wrappedRepositories.forEach(repo -> repo.setAllowInsecureProtocol(b));
    }

    @Override
    public Set<URI> getArtifactUrls() {
        return wrappedRepositories.stream()
                .map(MavenArtifactRepository::getArtifactUrls)
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet());
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
        wrappedRepositories.forEach(repo -> repo.metadataSources(action));

    }

    @Override
    public MetadataSources getMetadataSources() {
        return findPrimary().getMetadataSources();
    }

    @Override
    public void mavenContent(Action<? super MavenRepositoryContentDescriptor> action) {
        wrappedRepositories.forEach(repo -> repo.mavenContent(action));
    }

    @Override
    public String getName() {
        return findPrimary().getName();
    }

    @Override
    public void setName(String s) {
        throw new LimitedMavenArtifactRepositoryException();
    }

    @Override
    public void content(Action<? super RepositoryContentDescriptor> action) {
        wrappedRepositories.forEach(repo -> repo.content(action));
    }

    @Override
    public PasswordCredentials getCredentials() {
        return new WrappedPasswordCredentials();
    }

    @Override
    public <T extends Credentials> T getCredentials(Class<T> aClass) {
        if(aClass != PasswordCredentials.class) {
            throw new LimitedMavenArtifactRepositoryException();
        }
        return (T)getCredentials();
    }

    @Override
    public void credentials(Action<? super PasswordCredentials> action) {
        wrappedRepositories.forEach(repo -> repo.credentials(action));
    }

    @Override
    public <T extends Credentials> void credentials(Class<T> aClass, Action<? super T> action) {
        wrappedRepositories.forEach(repo -> repo.credentials(aClass, action));
    }

    @Override
    public void credentials(Class<? extends Credentials> aClass) {
        throw new LimitedMavenArtifactRepositoryException();
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
        wrappedRepositories.forEach(repo -> repo.setMetadataSupplier(aClass));
    }

    @Override
    public void setMetadataSupplier(Class<? extends ComponentMetadataSupplier> aClass,
            Action<? super ActionConfiguration> action) {
        wrappedRepositories.forEach(repo -> repo.setMetadataSupplier(aClass, action));
    }

    @Override
    public void setComponentVersionsLister(Class<? extends ComponentMetadataVersionLister> aClass) {

        wrappedRepositories.forEach(repo -> repo.setComponentVersionsLister(aClass));
    }

    @Override
    public void setComponentVersionsLister(Class<? extends ComponentMetadataVersionLister> aClass,
            Action<? super ActionConfiguration> action) {
        wrappedRepositories.forEach(repo -> repo.setComponentVersionsLister(aClass, action));
    }
}
