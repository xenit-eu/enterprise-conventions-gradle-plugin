package eu.xenit.gradle.enterprise.conventions;

import eu.xenit.gradle.enterprise.conventions.api.PluginApi;
import eu.xenit.gradle.enterprise.conventions.api.PublicApi;
import eu.xenit.gradle.enterprise.conventions.extensions.dockerimagelabels.DockerImageLabelsPlugin;
import eu.xenit.gradle.enterprise.conventions.extensions.repository.RepositoryExtensionsPlugin;
import eu.xenit.gradle.enterprise.conventions.publish.PrivatePublishPlugin;
import eu.xenit.gradle.enterprise.conventions.repository.RepositoryPlugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;

@PublicApi
public class PrivatePlugin extends BasePlugin {

    @PluginApi
    public static final String PLUGIN_ID = "eu.xenit.enterprise-conventions.private";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(RepositoryExtensionsPlugin.class);
        project.getPluginManager().apply(RepositoryPlugin.class);
        project.getPluginManager().apply(PrivatePublishPlugin.class);
        project.getPluginManager().apply(DockerImageLabelsPlugin.class);
    }

    @Override
    public void apply(Settings settings) {
        super.apply(settings);
        settings.getPluginManager().apply(RepositoryExtensionsPlugin.class);
    }
}
