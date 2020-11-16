package eu.xenit.gradle.enterprise;

import eu.xenit.gradle.enterprise.repository.OssRepositoryPlugin;
import org.gradle.api.Project;

public class OssPlugin extends BasePlugin {

    public static final String PLUGIN_ID = "eu.xenit.enterprise.oss";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(OssRepositoryPlugin.class);
    }
}
