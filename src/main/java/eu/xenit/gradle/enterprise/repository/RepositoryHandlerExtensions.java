package eu.xenit.gradle.enterprise.repository;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.Convention;

/**
 * Convention that is applied to the project's {@link RepositoryHandler} to add xenit() and xenitSnapshots() repositories.
 */
public class RepositoryHandlerExtensions {

    private final RepositoryHandler repositoryHandler;
    private final Project project;

    private static final Action<? super MavenArtifactRepository> EMPTY_ACTION = repository -> {
    };

    public RepositoryHandlerExtensions(RepositoryHandler repositoryHandler, Project project) {
        this.repositoryHandler = repositoryHandler;
        this.project = project;
    }

    public MavenArtifactRepository xenit() {
        return xenit(EMPTY_ACTION);
    }

    public MavenArtifactRepository xenit(Action<? super MavenArtifactRepository> action) {
        return repositoryHandler.maven(repository -> {
            repository.setName("Xenit Releases");
            repository.setUrl("https://artifactory.xenit.eu/artifactory/libs-release-local");
            repository.credentials(CredentialsUtil.configureArtifactoryCredentials(project));
            action.execute(repository);
        });
    }

    public MavenArtifactRepository xenitSnapshots() {
        return xenitSnapshots(EMPTY_ACTION);
    }

    public MavenArtifactRepository xenitSnapshots(Action<? super MavenArtifactRepository> action) {
        return repositoryHandler.maven(repository -> {
            repository.setName("Xenit Snapshots");
            repository.setUrl("https://artifactory.xenit.eu/artifactory/libs-snapshot-local");
            repository.credentials(CredentialsUtil.configureArtifactoryCredentials(project));
            action.execute(repository);
        });
    }

    public static void apply(RepositoryHandler repositoryHandler, Project project) {
        ((Convention) repositoryHandler).add(RepositoryHandlerExtensions.class, "eu.xenit.enterprise.repository",
                new RepositoryHandlerExtensions(repositoryHandler, project));
    }
}
