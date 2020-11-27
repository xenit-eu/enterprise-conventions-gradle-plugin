package eu.xenit.gradle.enterprise;

import eu.xenit.gradle.enterprise.repository.PrivateRepositoryReplacementPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.invocation.Gradle;

public class PrivateInitPlugin implements Plugin<Gradle> {

    public static final String PLUGIN_ID = "eu.xenit.enterprise.private.init";

    @Override
    public void apply(Gradle gradle) {
        gradle.allprojects(project -> {
            project.getPluginManager().apply(PrivateRepositoryReplacementPlugin.class);
        });
    }
}
