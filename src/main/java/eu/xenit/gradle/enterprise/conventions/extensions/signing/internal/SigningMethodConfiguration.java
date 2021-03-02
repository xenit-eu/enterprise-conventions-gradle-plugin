package eu.xenit.gradle.enterprise.conventions.extensions.signing.internal;

import org.gradle.plugins.signing.SigningExtension;

public interface SigningMethodConfiguration {

    default String getName() {
        return getClass().getSimpleName();
    }

    boolean isEnabled();

    void configureSigning(SigningExtension extension);

    String getRequiredConfigs();

    String getOptionalConfigs();
}
