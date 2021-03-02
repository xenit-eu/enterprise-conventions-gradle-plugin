package eu.xenit.gradle.enterprise.conventions.extensions.signing.internal;

import org.gradle.api.Project;
import org.gradle.plugins.signing.SigningExtension;

public class DefaultSigningMethodConfiguration extends AbstractPropertyConfiguredSigningMethodConfiguration {

    private static final String[] SIGNING_PROPS_PGP = new String[]{
            "signing.keyId",
            "signing.password",
            "signing.secretKeyRingFile",
    };

    public DefaultSigningMethodConfiguration(Project project) {
        super(project, SIGNING_PROPS_PGP, new String[0]);
    }

    @Override
    public void configureSigning(SigningExtension extension) {
        // Nothing needs to be configured here, as this is the default configuration
    }

}
