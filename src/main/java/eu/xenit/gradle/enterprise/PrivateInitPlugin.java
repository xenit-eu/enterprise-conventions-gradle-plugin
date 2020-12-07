package eu.xenit.gradle.enterprise;

import eu.xenit.gradle.enterprise.repository.PrivateRepositoryReplacementPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class PrivateInitPlugin implements Plugin<Gradle> {

    public static final String PLUGIN_ID = "eu.xenit.enterprise.private.init";
    public static final Logger LOGGER = Logging.getLogger(PrivateInitPlugin.class);

    @Override
    public void apply(Gradle gradle) {
        LOGGER.debug("Repository replacement plugin active from initscript");
        gradle.allprojects(project -> {
            project.getPluginManager().apply(PrivateRepositoryReplacementPlugin.class);
        });
    }
}
