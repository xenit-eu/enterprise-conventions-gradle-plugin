package eu.xenit.gradle.enterprise.conventions.extensions.signing.internal;

import java.util.Arrays;
import org.gradle.api.Project;

abstract class AbstractPropertyConfiguredSigningMethodConfiguration implements SigningMethodConfiguration {

    protected final Project project;
    private final String[] requiredProps;
    private final String[] optionalProps;

    AbstractPropertyConfiguredSigningMethodConfiguration(Project project, String[] requiredProps,
            String[] optionalProps) {
        this.project = project;
        this.requiredProps = requiredProps;
        this.optionalProps = optionalProps;
    }

    @Override
    public boolean isEnabled() {
        return Arrays.stream(requiredProps).allMatch(project::hasProperty);
    }

    @Override
    public String getRequiredConfigs() {
        if (requiredProps.length == 0) {
            return "";
        }
        return "properties " + Arrays.toString(requiredProps);
    }

    @Override
    public String getOptionalConfigs() {
        if (optionalProps.length == 0) {
            return "";
        }
        return "properties " + Arrays.toString(optionalProps);
    }
}
