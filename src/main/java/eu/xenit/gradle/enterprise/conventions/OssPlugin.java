package eu.xenit.gradle.enterprise.conventions;

import eu.xenit.gradle.enterprise.conventions.api.PluginApi;
import eu.xenit.gradle.enterprise.conventions.api.PublicApi;
import eu.xenit.gradle.enterprise.conventions.extensions.dockerimagelabels.DockerImageLabelsPlugin;
import eu.xenit.gradle.enterprise.conventions.extensions.dockermultiarch.DockerMultiArchPlugin;
import eu.xenit.gradle.enterprise.conventions.extensions.mavencentral.publish.MavenCentralPublishPlugin;
import eu.xenit.gradle.enterprise.conventions.extensions.mavencentral.requirements.MavenCentralRequirementsCheckPlugin;
import eu.xenit.gradle.enterprise.conventions.extensions.signing.AutomaticSigningPlugin;
import org.gradle.api.Project;

@PublicApi
public class OssPlugin extends BasePlugin {

    @PluginApi
    public static final String PLUGIN_ID = "eu.xenit.enterprise-conventions.oss";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(MavenCentralRequirementsCheckPlugin.class);
        project.getPluginManager().apply(MavenCentralPublishPlugin.class);
        project.getPluginManager().apply(AutomaticSigningPlugin.class);
        project.getPluginManager().apply(DockerImageLabelsPlugin.class);
        project.getPluginManager().apply(DockerMultiArchPlugin.class);
    }
}
