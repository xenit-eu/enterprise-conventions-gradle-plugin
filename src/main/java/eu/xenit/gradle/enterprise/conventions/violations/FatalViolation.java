package eu.xenit.gradle.enterprise.conventions.violations;

import eu.xenit.gradle.enterprise.conventions.api.PluginApi;

@PluginApi
public final class FatalViolation extends RuntimeException {

    FatalViolation(String category, RuntimeException violation) {
        super("Policy violation [" + category + "]: " + violation.getMessage(), violation);
    }
}
