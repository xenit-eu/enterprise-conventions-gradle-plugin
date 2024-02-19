package eu.xenit.gradle.enterprise.conventions;

import eu.xenit.gradle.enterprise.conventions.internal.MultipleApplicationTargetsPlugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;

public abstract class BasePlugin implements MultipleApplicationTargetsPlugin {

    public abstract void apply(Project project);

    public void apply(Settings settings) {
        settings.getGradle().allprojects(project -> {
            project.getPluginManager().apply(getClass());
        });
    }
}
