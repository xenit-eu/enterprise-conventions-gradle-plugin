package eu.xenit.gradle.enterprise;

import eu.xenit.gradle.enterprise.repository.InternalRepositoryReplacementPlugin;
import eu.xenit.gradle.enterprise.repository.RepositoryHandlerExtensions;
import org.gradle.api.Project;

public class InternalPlugin extends BasePlugin {

    public static final String PLUGIN_ID = "eu.xenit.enterprise.internal";

    @Override
    public void apply(Project project) {
        project.getPlugins().withType(OssPlugin.class, action -> {
            throw new IllegalStateException(
                    "The " + PLUGIN_ID + " plugin can not be applied together with " + OssPlugin.PLUGIN_ID);
        });

        project.getPluginManager().apply(InternalRepositoryReplacementPlugin.class);
        RepositoryHandlerExtensions.apply(project.getRepositories(), project);
        RepositoryHandlerExtensions.apply(project.getBuildscript().getRepositories(), project);
    }
}
