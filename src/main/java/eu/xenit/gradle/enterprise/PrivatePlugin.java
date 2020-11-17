package eu.xenit.gradle.enterprise;

import eu.xenit.gradle.enterprise.publish.PrivatePublishPlugin;
import eu.xenit.gradle.enterprise.repository.PrivateRepositoryPlugin;
import org.gradle.api.Project;

public class PrivatePlugin extends BasePlugin {

    public static final String PLUGIN_ID = "eu.xenit.enterprise.private";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(PrivateRepositoryPlugin.class);
        project.getPluginManager().apply(PrivatePublishPlugin.class);
    }
}
