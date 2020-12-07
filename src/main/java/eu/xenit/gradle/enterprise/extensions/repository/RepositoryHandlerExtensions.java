package eu.xenit.gradle.enterprise.extensions.repository;

import eu.xenit.gradle.enterprise.internal.ArtifactoryCredentialsUtil;
import eu.xenit.gradle.enterprise.internal.StringConstants;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.HasConvention;

/**
 * Convention that is applied to the project's {@link RepositoryHandler} to add xenitPrivate() and xenitPrivateSnapshots() repositories.
 */
public class RepositoryHandlerExtensions {

    public static final String XENIT_SNAPSHOTS_URL = StringConstants.XENIT_BASE_URL + "libs-snapshot-local";
    public static final String XENIT_RELEASE_URL = StringConstants.XENIT_BASE_URL + "libs-release-local";
    private final RepositoryHandler repositoryHandler;
    private final Project project;

    protected static final Action<? super MavenArtifactRepository> EMPTY_ACTION = repository -> {
    };

    @Inject
    public RepositoryHandlerExtensions(RepositoryHandler repositoryHandler, Project project) {
        this.repositoryHandler = repositoryHandler;
        this.project = project;
    }

    public MavenArtifactRepository xenitPrivate() {
        return xenitPrivate(EMPTY_ACTION);
    }

    public MavenArtifactRepository xenitPrivate(Action<? super MavenArtifactRepository> action) {
        return repositoryHandler.maven(repository -> {
            repository.setName("XenitPrivate");
            repository.setUrl(XENIT_RELEASE_URL);
            repository.credentials(ArtifactoryCredentialsUtil.configureArtifactoryCredentials(project));
            action.execute(repository);
        });
    }

    public MavenArtifactRepository xenitPrivateSnapshots() {
        return xenitPrivateSnapshots(EMPTY_ACTION);
    }

    public MavenArtifactRepository xenitPrivateSnapshots(Action<? super MavenArtifactRepository> action) {
        return repositoryHandler.maven(repository -> {
            repository.setName("XenitPrivateSnapshots");
            repository.setUrl(XENIT_SNAPSHOTS_URL);
            repository.credentials(ArtifactoryCredentialsUtil.configureArtifactoryCredentials(project));
            action.execute(repository);
        });
    }

    public MavenArtifactRepository sonatypeSnapshots() {
        return sonatypeSnapshots(EMPTY_ACTION);
    }

    public MavenArtifactRepository sonatypeSnapshots(Action<? super MavenArtifactRepository> action) {
        return repositoryHandler.maven(repository -> {
            repository.setName("SonatypeSnapshots");
            repository.setUrl(StringConstants.SONATYPE_SNAPSHOTS_URL);
            action.execute(repository);
        });
    }

    public static void apply(RepositoryHandler repositoryHandler, Project project) {
        RepositoryHandlerExtensions repositoryHandlerExtensions = project.getObjects()
                .newInstance(RepositoryHandlerExtensions.class, repositoryHandler, project);
        ((HasConvention) repositoryHandler).getConvention().getPlugins()
                .put(RepositoryHandlerExtensions.class.getCanonicalName(), repositoryHandlerExtensions);
    }
}
