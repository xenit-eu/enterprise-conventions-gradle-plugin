package eu.xenit.gradle.enterprise.conventions.internal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;

public interface MultipleApplicationTargetsPlugin extends Plugin<Object> {
    @Override
    default void apply(Object o) {
        if (o instanceof Project) {
            apply((Project) o);
        } else if (o instanceof Settings) {
            apply((Settings) o);
        } else {
            throw new IllegalArgumentException("This plugin can only be applied to a Project or Settings object.");
        }
    }

    void apply(Project project);

    void apply(Settings settings);
}
