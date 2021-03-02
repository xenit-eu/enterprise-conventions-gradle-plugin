package eu.xenit.gradle.enterprise.conventions.extensions.signing.internal;

import org.gradle.api.Project;
import org.gradle.plugins.signing.SigningExtension;

public class GnupgSigningMethodConfiguration extends AbstractPropertyConfiguredSigningMethodConfiguration {

    private static final String[] REQUIRED_PROPS_GNUPG = new String[]{
            "signing.gnupg.keyName",
    };
    private static final String[] OPTIONAL_PROPS_GNUPG = new String[]{
            "signing.gnupg.executable",
            "signing.gnupg.useLegacyGpg",
            "signing.gnupg.homeDir",
            "signing.gnupg.optionsFile",
            "signing.gnupg.passphrase",
    };

    public GnupgSigningMethodConfiguration(Project project) {
        super(project, REQUIRED_PROPS_GNUPG, OPTIONAL_PROPS_GNUPG);
    }

    @Override
    public void configureSigning(SigningExtension extension) {
        extension.useGpgCmd();
    }
}
