package eu.xenit.gradle.enterprise.conventions;

import eu.xenit.gradle.enterprise.conventions.api.PluginApi;
import eu.xenit.gradle.enterprise.conventions.api.PublicApi;
import eu.xenit.gradle.enterprise.conventions.repository.PrivateRepositoryReplacementPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

@PublicApi
public class PrivateInitPlugin implements Plugin<Gradle> {

    @PluginApi
    public static final String PLUGIN_ID = "eu.xenit.enterprise-conventions.private.init";
    private static final Logger LOGGER = Logging.getLogger(PrivateInitPlugin.class);

    @Override
    public void apply(Gradle gradle) {
        LOGGER.debug("Repository replacement plugin active from initscript");
        gradle.allprojects(project -> {
            project.getPluginManager().apply(PrivateRepositoryReplacementPlugin.class);
        });
    }
}
