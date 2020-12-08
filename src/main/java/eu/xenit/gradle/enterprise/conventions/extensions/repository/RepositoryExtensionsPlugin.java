package eu.xenit.gradle.enterprise.conventions.extensions.repository;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

public class RepositoryExtensionsPlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = "eu.xenit.enterprise.ext.repository";

    @Override
    public void apply(Project project) {
        RepositoryHandlerExtensions.apply(project.getRepositories(), project);
        RepositoryHandlerExtensions.apply(project.getBuildscript().getRepositories(), project);

        project.getPlugins().withType(MavenPublishPlugin.class, publishPlugin -> {
            PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
            RepositoryHandlerExtensions.apply(publishing.getRepositories(), project);
            PublishRepositoryHandlerExtensions.apply(publishing.getRepositories(), project);
        });
    }
}
