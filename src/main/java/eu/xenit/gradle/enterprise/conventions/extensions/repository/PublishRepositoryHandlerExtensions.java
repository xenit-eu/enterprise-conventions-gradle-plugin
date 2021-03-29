package eu.xenit.gradle.enterprise.conventions.extensions.repository;

import de.marcphilipp.gradle.nexus.InitializeNexusStagingRepository;
import de.marcphilipp.gradle.nexus.NexusPublishExtension;
import de.marcphilipp.gradle.nexus.NexusRepository;
import eu.xenit.gradle.enterprise.conventions.api.PluginApi;
import eu.xenit.gradle.enterprise.conventions.api.PublicApi;
import java.util.HashMap;
import javax.inject.Inject;
import kotlin.text.StringsKt;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.HasConvention;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;
import org.gradle.api.tasks.TaskProvider;

public class PublishRepositoryHandlerExtensions {

    private static final Action<? super MavenArtifactRepository> EMPTY_ACTION = repository -> {
    };
    private final Project project;
    private final RepositoryHandler repositoryHandler;

    @Inject
    public PublishRepositoryHandlerExtensions(Project project, RepositoryHandler repositoryHandler) {
        this.project = project;
        this.repositoryHandler = repositoryHandler;
    }

    @PublicApi
    public MavenArtifactRepository sonatypeMavenCentral() {
        return sonatypeMavenCentral(EMPTY_ACTION);
    }

    @PublicApi
    public MavenArtifactRepository sonatypeMavenCentral(Action<? super MavenArtifactRepository> action) {
        // Create a publish extension for the InitializeNexusStagingRepository task
        // Always use staging, as staging is required for publishing to sonatype releases
        NexusPublishExtension nexusPublishExtension = project.getObjects()
                .newInstance(NexusPublishExtension.class, project);
        nexusPublishExtension.getUseStaging().set(true);

        // Create sonatype artifact
        NexusRepository nexusRepository = nexusPublishExtension.getRepositories().sonatype();
        MavenArtifactRepository mavenRepository = repositoryHandler.maven(repo -> {
            repo.setName(nexusRepository.getName());
            repo.setUrl(nexusRepository.getNexusUrl());
            action.execute(repo);
        });

        // Configure nexus repository username & password from maven repository username & password
        nexusRepository.getUsername().set(project.provider(() -> mavenRepository.getCredentials().getUsername()));
        nexusRepository.getPassword().set(project.provider(() -> mavenRepository.getCredentials().getPassword()));

        // Create task to create staging repositories
        TaskProvider<InitializeNexusStagingRepository> initializeStaging = project.getTasks()
                .register(
                        "initialize" + StringsKt.capitalize(nexusRepository.getName()) + "StagingRepository",
                        InitializeNexusStagingRepository.class,
                        project.getObjects(),
                        nexusPublishExtension,
                        nexusRepository,
                        new HashMap<>() // Only used once
                );

        // Make publish task depend on creating the staging repository
        project.getTasks().withType(PublishToMavenRepository.class).configureEach(publishTask -> {
            if (publishTask.getRepository().equals(mavenRepository)) {
                publishTask.dependsOn(initializeStaging);
            }
        });

        return mavenRepository;
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
