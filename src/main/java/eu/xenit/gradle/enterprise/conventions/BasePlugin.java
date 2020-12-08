package eu.xenit.gradle.enterprise.conventions;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;

abstract class BasePlugin implements Plugin<Object> {

    @Override
    public void apply(Object o) {
        if (o instanceof Project) {
            apply((Project) o);
        } else if (o instanceof Settings) {
            apply((Settings) o);
        } else {
            throw new IllegalArgumentException("This plugin can only be applied to a Project or Settings object.");
        }
    }

    public abstract void apply(Project project);

    public void apply(Settings settings) {
        settings.getGradle().allprojects(project -> {
            project.getPluginManager().apply(getClass());
        });
    }
}
