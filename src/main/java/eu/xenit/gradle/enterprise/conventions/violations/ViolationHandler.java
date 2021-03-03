package eu.xenit.gradle.enterprise.conventions.violations;

import eu.xenit.gradle.enterprise.conventions.api.PluginApi;
import eu.xenit.gradle.enterprise.conventions.internal.StringConstants;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.gradle.api.Project;

@PluginApi
public interface ViolationHandler {

    @PluginApi
    void handleViolation(@Nonnull RuntimeException violation);

    @PluginApi
    @Nonnull
    static ViolationHandler fromProject(@Nonnull Project project, @Nonnull String category) {
        Objects.requireNonNull(project, "project");
        final String propertyName = StringConstants.GRADLE_PROPERTIES_PREFIX + ".violations";
        final String categoryPropertyName = propertyName + "." + Objects.requireNonNull(category, "category");
        ViolationEnforceLevel enforceLevel = Optional.ofNullable(project.findProperty(categoryPropertyName))
                .or(() -> Optional.ofNullable(project.findProperty(propertyName)))
                .map(Object::toString)
                .map(String::toUpperCase)
                .map(v -> {
                    try {
                        return ViolationEnforceLevel.valueOf(v);
                    } catch (IllegalArgumentException e) {
                        String validValues = Arrays.stream(ViolationEnforceLevel.values())
                                .map(Enum::name)
                                .map(String::toLowerCase)
                                .collect(Collectors.joining(", "));
                        throw new IllegalArgumentException(
                                "Invalid value of property " + propertyName + ": " + v.toLowerCase() + " is not one of "
                                        + validValues, e);
                    }
                })
                .orElse(ViolationEnforceLevel.ENFORCE);

        return enforceLevel.createEnforcerForCategory(category);
    }
}
