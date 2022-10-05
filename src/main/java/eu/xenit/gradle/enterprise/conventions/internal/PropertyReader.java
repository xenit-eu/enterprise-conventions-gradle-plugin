package eu.xenit.gradle.enterprise.conventions.internal;

import java.util.Objects;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;

@FunctionalInterface
public interface PropertyReader {
    default Object property(String prop) {
        return Objects.requireNonNull(findProperty(prop), "Property "+prop);
    }

    default boolean hasProperty(String prop) {
        return findProperty(prop) != null;
    }

    Object findProperty(String prop);

    static PropertyReader from(Project project) {
        return new PropertyReader() {
            @Override
            public Object property(String prop) {
                return project.property(prop);
            }

            @Override
            public boolean hasProperty(String prop) {
                return project.hasProperty(prop);
            }

            @Override
            public Object findProperty(String prop) {
                return project.findProperty(prop);
            }
        };
    }

    static PropertyReader from(Settings settings) {
        return new PropertyReader() {
            @Override
            public Object findProperty(String prop) {
                return settings.getProviders().gradleProperty(prop).forUseAtConfigurationTime().getOrNull();
            }
        };
    }
}
