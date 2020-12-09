package eu.xenit.gradle.enterprise.conventions.internal.artifactory;

import java.io.Serializable;
import java.util.List;

class RepositoriesAndDate implements Serializable {

    public long getExpirySeconds() {
        return expirySeconds;
    }

    public void setExpirySeconds(long expirySeconds) {
        this.expirySeconds = expirySeconds;
    }

    public List<ArtifactoryRepositorySpec> getRepositories() {
        return repositories;
    }

    public void setRepositories(
            List<ArtifactoryRepositorySpec> repositories) {
        this.repositories = repositories;
    }

    private long expirySeconds;
    private List<ArtifactoryRepositorySpec> repositories;
}
