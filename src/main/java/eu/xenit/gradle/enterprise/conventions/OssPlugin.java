package eu.xenit.gradle.enterprise.conventions;

import eu.xenit.gradle.enterprise.conventions.api.PluginApi;
import eu.xenit.gradle.enterprise.conventions.api.PublicApi;
import eu.xenit.gradle.enterprise.conventions.extensions.repository.RepositoryExtensionsPlugin;
import eu.xenit.gradle.enterprise.conventions.publish.OssPublishPlugin;
import eu.xenit.gradle.enterprise.conventions.repository.OssRepositoryPlugin;
import org.gradle.api.Project;

@PublicApi
public class OssPlugin extends BasePlugin {

    @PluginApi
    public static final String PLUGIN_ID = "eu.xenit.enterprise-conventions.oss";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(RepositoryExtensionsPlugin.class);
        project.getPluginManager().apply(OssRepositoryPlugin.class);
        project.getPluginManager().apply(OssPublishPlugin.class);
    }
}
