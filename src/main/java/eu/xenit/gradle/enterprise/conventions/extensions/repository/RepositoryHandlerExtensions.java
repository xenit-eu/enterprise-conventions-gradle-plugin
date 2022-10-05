package eu.xenit.gradle.enterprise.conventions.extensions.repository;

import eu.xenit.gradle.enterprise.conventions.api.PluginApi;
import eu.xenit.gradle.enterprise.conventions.api.PublicApi;
import eu.xenit.gradle.enterprise.conventions.internal.ArtifactoryCredentialsUtil;
import eu.xenit.gradle.enterprise.conventions.internal.PropertyReader;
import eu.xenit.gradle.enterprise.conventions.internal.StringConstants;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.HasConvention;
import org.gradle.api.internal.model.InstantiatorBackedObjectFactory;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.internal.reflect.DirectInstantiator;

/**
 * Convention that is applied to the project's {@link RepositoryHandler} to add xenitPrivate() and xenitPrivateSnapshots() repositories.
 */
public class RepositoryHandlerExtensions {
    private static final Logger LOGGER = Logging.getLogger(RepositoryHandlerExtensions.class);

    public static final String XENIT_SNAPSHOTS_URL = StringConstants.XENIT_BASE_URL + "libs-snapshot-local";
    public static final String XENIT_RELEASE_URL = StringConstants.XENIT_BASE_URL + "libs-release-local";
    private final RepositoryHandler repositoryHandler;
    private final PropertyReader propertyReader;

    protected static final Action<? super MavenArtifactRepository> EMPTY_ACTION = repository -> {
    };

    @Inject
    public RepositoryHandlerExtensions(RepositoryHandler repositoryHandler, PropertyReader propertyReader) {
        this.repositoryHandler = repositoryHandler;
        this.propertyReader = propertyReader;
    }

    private Action<? super PasswordCredentials> configureRepoCredentials(String prefix) {
        String usernameProp = prefix+".username";
        String passwordProp = prefix+".password";

        if(propertyReader.hasProperty(usernameProp) && propertyReader.hasProperty(passwordProp)) {
            return passwordCredentials -> {
                passwordCredentials.setUsername(propertyReader.property(usernameProp).toString());
                passwordCredentials.setPassword(propertyReader.property(passwordProp).toString());
            };
        }
        return passwordCredentials -> {};
    }

    @PublicApi
    public MavenArtifactRepository xenit() {
        return xenit(EMPTY_ACTION);
    }

    @PublicApi
    public MavenArtifactRepository xenit(Action<? super MavenArtifactRepository> action) {
        return repositoryHandler.maven(repository -> {
            repository.setName("Xenit");
            repository.setUrl(StringConstants.XENIT_REPO_URL);
            repository.credentials(configureRepoCredentials("eu.xenit.repo"));
            repository.mavenContent(contentDescriptor -> {
                contentDescriptor.releasesOnly();
            });
            action.execute(repository);
        });
    }

    @PublicApi
    public MavenArtifactRepository xenitSnapshots() {
        return xenitSnapshots(EMPTY_ACTION);
    }

    @PublicApi
    public MavenArtifactRepository xenitSnapshots(Action<? super MavenArtifactRepository> action) {
        return repositoryHandler.maven(repository -> {
            repository.setName("XenitSnapshots");
            repository.setUrl(StringConstants.XENIT_REPO_URL);
            repository.credentials(configureRepoCredentials("eu.xenit.repo"));
            repository.mavenContent(contentDescriptor -> {
                contentDescriptor.snapshotsOnly();
            });
            action.execute(repository);
        });
    }

    @PublicApi
    @Deprecated(since = "0.3.0", forRemoval = true)
    public MavenArtifactRepository xenitPrivate() {
        return xenitPrivate(EMPTY_ACTION);
    }

    @PublicApi
    @Deprecated(since = "0.3.0", forRemoval = true)
    public MavenArtifactRepository xenitPrivate(Action<? super MavenArtifactRepository> action) {
        LOGGER.warn("Using the xenitPrivate() repository is deprecated, as the artifact repository will be shutdown. If artifacts are available, use the xenit() repository instead.");
        return repositoryHandler.maven(repository -> {
            repository.setName("XenitPrivate");
            repository.setUrl(XENIT_RELEASE_URL);
            repository.credentials(ArtifactoryCredentialsUtil.configureArtifactoryCredentials(propertyReader));
            action.execute(repository);
        });
    }

    @PublicApi
    @Deprecated(since = "0.3.0", forRemoval = true)
    public MavenArtifactRepository xenitPrivateSnapshots() {
        return xenitPrivateSnapshots(EMPTY_ACTION);
    }

    @PublicApi
    @Deprecated(since = "0.3.0", forRemoval = true)
    public MavenArtifactRepository xenitPrivateSnapshots(Action<? super MavenArtifactRepository> action) {
        LOGGER.warn("Using the xenitPrivateSnapshots() repository is deprecated, as the artifact repository will be shutdown. If artifacts are available, use the xenitSnapshots() repository instead.");
        return repositoryHandler.maven(repository -> {
            repository.setName("XenitPrivateSnapshots");
            repository.setUrl(XENIT_SNAPSHOTS_URL);
            repository.credentials(ArtifactoryCredentialsUtil.configureArtifactoryCredentials(propertyReader));
            action.execute(repository);
        });
    }

    @PublicApi
    public MavenArtifactRepository sonatypeSnapshots() {
        return sonatypeSnapshots(EMPTY_ACTION);
    }

    @PublicApi
    public MavenArtifactRepository sonatypeSnapshots(Action<? super MavenArtifactRepository> action) {
        Set<MavenArtifactRepository> repositories = new HashSet<>();
        for (String snapshotsUrl : StringConstants.SONATYPE_SNAPSHOTS_URLS) {
            repositories.add(repositoryHandler.maven(repository -> {
                repository.setName("SonatypeSnapshots-"+repositories.size());
                repository.setUrl(snapshotsUrl);
                action.execute(repository);
            }));
        }
        return new MultiMavenArtifactRepository(repositories);
    }

    static void apply(RepositoryHandler repositoryHandler, Project project) {
        apply(repositoryHandler, PropertyReader.from(project), project.getObjects());
    }

    static void apply(RepositoryHandler repositoryHandler, Settings settings) {
        apply(repositoryHandler, PropertyReader.from(settings), new InstantiatorBackedObjectFactory(DirectInstantiator.INSTANCE));
    }

    private static void apply(RepositoryHandler repositoryHandler, PropertyReader reader, ObjectFactory objectFactory) {
        RepositoryHandlerExtensions repositoryHandlerExtensions = objectFactory.newInstance(RepositoryHandlerExtensions.class, repositoryHandler, reader);
        ((HasConvention) repositoryHandler).getConvention().getPlugins()
                .put(RepositoryHandlerExtensions.class.getCanonicalName(), repositoryHandlerExtensions);
    }

    @PluginApi
    public static RepositoryHandlerExtensions get(RepositoryHandler handler) {
        return ((HasConvention) handler).getConvention().getPlugin(RepositoryHandlerExtensions.class);
    }
}
