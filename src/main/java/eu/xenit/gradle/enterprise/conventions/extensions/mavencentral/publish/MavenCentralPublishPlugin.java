package eu.xenit.gradle.enterprise.conventions.extensions.mavencentral.publish;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class MavenCentralPublishPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getRootProject().getPluginManager().apply(MavenCentralPublishRootProjectPlugin.class);
        project.getRootProject().getPlugins().withType(MavenCentralPublishRootProjectPlugin.class, rootPlugin -> {
            rootPlugin.registerPublication(project);
        });
    }
}
