package eu.xenit.gradle.enterprise.conventions.internal.artifactory;

import java.util.Collections;
import java.util.List;

public class NullArtifactoryClient implements ArtifactoryClient {

    @Override
    public List<ArtifactoryRepositorySpec> getRepositories() {
        return Collections.emptyList();
    }
}
