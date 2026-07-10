package eu.xenit.gradle.enterprise.conventions;

import eu.xenit.gradle.enterprise.conventions.api.PluginApi;
import eu.xenit.gradle.enterprise.conventions.api.PublicApi;
import eu.xenit.gradle.enterprise.conventions.extensions.dockerimagelabels.DockerImageLabelsPlugin;
import eu.xenit.gradle.enterprise.conventions.extensions.dockermultiarch.DockerMultiArchPlugin;
import org.gradle.api.Project;

@PublicApi
public class PrivatePlugin extends BasePlugin {

    @PluginApi
    public static final String PLUGIN_ID = "eu.xenit.enterprise-conventions.private";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(DockerImageLabelsPlugin.class);
        project.getPluginManager().apply(DockerMultiArchPlugin.class);
    }
}
