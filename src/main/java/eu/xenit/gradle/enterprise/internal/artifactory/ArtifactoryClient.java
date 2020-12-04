package eu.xenit.gradle.enterprise.internal.artifactory;

import java.util.List;

public interface ArtifactoryClient {

    List<ArtifactoryRepositorySpec> getRepositories();
}
