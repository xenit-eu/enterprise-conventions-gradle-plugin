package eu.xenit.gradle.enterprise.violations;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import org.gradle.api.Project;

public interface ViolationHandler {

    void handleViolation(RuntimeException violation);

    public static ViolationHandler fromProject(Project project) {
        final String propertyName = "eu.xenit.enterprise.violations";
        ViolationEnforceLevel enforceLevel = Optional.ofNullable(project.findProperty(propertyName))
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

        return enforceLevel.getEnforcer();
    }
}
