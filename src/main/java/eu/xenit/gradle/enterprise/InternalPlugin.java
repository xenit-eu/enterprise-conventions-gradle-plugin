package eu.xenit.gradle.enterprise;

import eu.xenit.gradle.enterprise.repository.InternalRepositoryPlugin;
import org.gradle.api.Project;

public class InternalPlugin extends BasePlugin {

    public static final String PLUGIN_ID = "eu.xenit.enterprise.internal";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(InternalRepositoryPlugin.class);
    }
}
