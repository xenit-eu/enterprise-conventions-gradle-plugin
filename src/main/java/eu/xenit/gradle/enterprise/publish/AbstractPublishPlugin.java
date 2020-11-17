package eu.xenit.gradle.enterprise.publish;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

abstract class AbstractPublishPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().withType(MavenPublishPlugin.class, publishPlugin -> {
            PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
            PublishRepositoryHandlerExtensions.apply(publishing.getRepositories(), project);
            configurePublication(project, publishing);
            validatePublishRepositories(project, publishing);
        });

    }

    private void validatePublishRepositories(Project project, PublishingExtension publishing) {
        publishing.getRepositories().all(repository -> {
            if (repository instanceof MavenArtifactRepository) {
                validatePublishRepository((MavenArtifactRepository) repository);
            }
        });

    }

    protected abstract void validatePublishRepository(MavenArtifactRepository repository);

    protected abstract void configurePublication(Project project, PublishingExtension publishing);
}
