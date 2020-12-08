package eu.xenit.gradle.enterprise.conventions.internal.artifactory;

import java.util.List;

public interface ArtifactoryClient {

    List<ArtifactoryRepositorySpec> getRepositories();
}
