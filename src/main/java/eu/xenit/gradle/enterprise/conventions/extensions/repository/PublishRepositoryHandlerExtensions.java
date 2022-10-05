package eu.xenit.gradle.enterprise.conventions.extensions.repository;

import de.marcphilipp.gradle.nexus.NexusPublishExtension;
import de.marcphilipp.gradle.nexus.NexusPublishPlugin;
import de.marcphilipp.gradle.nexus.NexusRepository;
import eu.xenit.gradle.enterprise.conventions.api.PluginApi;
import eu.xenit.gradle.enterprise.conventions.api.PublicApi;
import eu.xenit.gradle.enterprise.conventions.internal.PropertyReader;
import eu.xenit.gradle.enterprise.conventions.internal.StringConstants;
import javax.inject.Inject;
import kotlin.text.StringsKt;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.HasConvention;

public class PublishRepositoryHandlerExtensions extends RepositoryHandlerExtensions {

    private static final Action<? super MavenArtifactRepository> EMPTY_ACTION = repository -> {
    };

    private final RepositoryHandler repositoryHandler;
    private final Project project;

    @Inject
    public PublishRepositoryHandlerExtensions(Project project, RepositoryHandler repositoryHandler) {
        super(repositoryHandler, PropertyReader.from(project));
        this.repositoryHandler = repositoryHandler;
        this.project = project;
    }

    @PublicApi
    @Override
    public MavenArtifactRepository sonatypeSnapshots(Action<? super MavenArtifactRepository> action) {
        return repositoryHandler.maven(repository -> {
            repository.setName("SonatypeSnapshots");
            repository.setUrl(StringConstants.SONATYPE_SNAPSHOTS_URLS.get(0));
            action.execute(repository);
        });
    }

    @PublicApi
    public MavenArtifactRepository sonatypeMavenCentral() {
        return sonatypeMavenCentral(EMPTY_ACTION);
    }

    @PublicApi
    public MavenArtifactRepository sonatypeMavenCentral(Action<? super MavenArtifactRepository> action) {
        project.getPlugins().apply(NexusPublishPlugin.class);
        NexusPublishExtension nexusPublishExtension = project.getExtensions().getByType(NexusPublishExtension.class);

        // Get rid of tasks automatically added by the NexusPublishPlugin.
        // These do not contribute anything to the core functionality, which is creating a temporary repository
        // to publish releases to.
        // We did not ask for and do not want these additional tasks, because they may be confusing to users.
        // We can't just delete them, because that would cause problems with deferred configuration of that plugin
        // But we can at least disable them and move them to the other group
        nexusPublishExtension.getRepositories().whenObjectAdded(nexusRepository -> {
            project.getTasks().named("publishTo" + StringsKt.capitalize(nexusRepository.getName())).configure(task -> {
                task.setEnabled(false);
                task.setGroup(null);
            });
        });

        // Always use staging, as staging is required for publishing to sonatype releases
        nexusPublishExtension.getUseStaging().set(true);

        // Create sonatype artifact
        NexusRepository sonatypeNexus = nexusPublishExtension.getRepositories().sonatype();
        MavenArtifactRepository artifactRepository = project.getObjects()
                .newInstance(SonatypeMavenCentralPublishRepository.class, sonatypeNexus);

        action.execute(artifactRepository);

        return artifactRepository;
    }

    static void apply(RepositoryHandler repositoryHandler, Project project) {
        PublishRepositoryHandlerExtensions extensions = project.getObjects()
                .newInstance(PublishRepositoryHandlerExtensions.class, project, repositoryHandler);

        ((HasConvention) repositoryHandler).getConvention().getPlugins()
                .put(PublishRepositoryHandlerExtensions.class.getCanonicalName(), extensions);
    }

    @PluginApi
    public static PublishRepositoryHandlerExtensions get(RepositoryHandler handler) {
        return ((HasConvention) handler).getConvention().getPlugin(PublishRepositoryHandlerExtensions.class);
    }
}
