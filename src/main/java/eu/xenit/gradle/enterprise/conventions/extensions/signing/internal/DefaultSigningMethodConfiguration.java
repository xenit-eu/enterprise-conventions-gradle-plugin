package eu.xenit.gradle.enterprise.conventions.extensions.signing.internal;

import java.util.Arrays;
import org.gradle.api.Project;
import org.gradle.plugins.signing.SigningExtension;

public class DefaultSigningMethodConfiguration implements SigningMethodConfiguration {

    private static final String[] SIGNING_PROPS_PGP = new String[]{
            "signing.keyId",
            "signing.password",
            "signing.secretKeyRingFile",
    };

    private final Project project;

    public DefaultSigningMethodConfiguration(Project project) {
        this.project = project;
    }

    @Override
    public boolean isEnabled() {
        return Arrays.stream(SIGNING_PROPS_PGP).allMatch(project::hasProperty);
    }

    @Override
    public void configureSigning(SigningExtension extension) {
        // Nothing needs to be configured here, as this is the default configuration
    }

    @Override
    public String getRequiredConfigs() {
        return "properties " + Arrays.toString(SIGNING_PROPS_PGP);
    }

    @Override
    public String getOptionalConfigs() {
        return "";
    }
}
