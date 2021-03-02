package eu.xenit.gradle.enterprise.conventions.extensions.signing.internal;

import java.util.Arrays;
import org.gradle.api.Project;
import org.gradle.plugins.signing.SigningExtension;

public class GnupgSigningMethodConfiguration implements SigningMethodConfiguration {

    private static final String PROP_GNUPG = "signing.gnupg";
    private static final String[] OPTIONAL_PROPS_GNUPG = new String[]{
            "signing.gnupg.executable",
            "signing.gnupg.useLegacyGpg",
            "signing.gnupg.homeDir",
            "signing.gnupg.optionsFile",
            "signing.gnupg.keyName",
            "signing.gnupg.passphrase",
    };

    private final Project project;

    public GnupgSigningMethodConfiguration(Project project) {
        this.project = project;
    }

    @Override
    public boolean isEnabled() {
        return !project.hasProperty(PROP_GNUPG) || Boolean.parseBoolean(project.property(PROP_GNUPG).toString());
    }

    @Override
    public void configureSigning(SigningExtension extension) {
        extension.useGpgCmd();
    }

    @Override
    public String getRequiredConfigs() {
        return "property " + PROP_GNUPG + " does not exist or is true";
    }

    @Override
    public String getOptionalConfigs() {
        return "properties " + Arrays.toString(OPTIONAL_PROPS_GNUPG);
    }
}
