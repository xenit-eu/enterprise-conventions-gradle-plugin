package eu.xenit.gradle.enterprise.conventions;

import eu.xenit.gradle.enterprise.conventions.api.PluginApi;
import eu.xenit.gradle.enterprise.conventions.api.PublicApi;
import eu.xenit.gradle.enterprise.conventions.extensions.repository.RepositoryExtensionsPlugin;
import eu.xenit.gradle.enterprise.conventions.publish.PrivatePublishPlugin;
import eu.xenit.gradle.enterprise.conventions.repository.PrivateRepositoryPlugin;
import org.gradle.api.Project;

@PublicApi
public class PrivatePlugin extends BasePlugin {

    @PluginApi
    public static final String PLUGIN_ID = "eu.xenit.enterprise-conventions.private";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(RepositoryExtensionsPlugin.class);
        project.getPluginManager().apply(PrivateRepositoryPlugin.class);
        project.getPluginManager().apply(PrivatePublishPlugin.class);
    }
}
