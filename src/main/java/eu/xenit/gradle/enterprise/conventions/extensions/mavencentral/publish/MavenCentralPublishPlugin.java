package eu.xenit.gradle.enterprise.conventions.extensions.mavencentral.publish;

import eu.xenit.gradle.enterprise.conventions.api.PublicApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

@PublicApi
public class MavenCentralPublishPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().withType(MavenPublishPlugin.class, _plugin -> {
            project.getPluginManager().apply("com.gradleup.nmcp");
        });
        project.getRootProject().getPluginManager().apply(MavenCentralPublishRootProjectPlugin.class);
        project.getRootProject().getPlugins().withType(MavenCentralPublishRootProjectPlugin.class, rootPlugin -> {
            rootPlugin.registerPublication(project);
        });
    }
}
