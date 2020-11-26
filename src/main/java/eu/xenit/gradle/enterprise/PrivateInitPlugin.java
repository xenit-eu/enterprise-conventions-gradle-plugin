package eu.xenit.gradle.enterprise;

import eu.xenit.gradle.enterprise.repository.PrivateRepositoryReplacementPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.invocation.Gradle;

public class PrivateInitPlugin implements Plugin<Gradle> {

    @Override
    public void apply(Gradle gradle) {
        gradle.allprojects(project -> {
            project.getPluginManager().apply(PrivateRepositoryReplacementPlugin.class);
        });
    }
}
