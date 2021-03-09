package eu.xenit.gradle.enterprise.conventions.extensions.signing.internal;

import java.util.Arrays;
import java.util.Map;
import org.gradle.plugins.signing.SigningExtension;

public class InMemorySigningMethodConfiguration implements SigningMethodConfiguration {

    private static final String[] SIGNING_ENV_IN_MEMORY = new String[]{
            "SIGNING_PRIVATE_KEY",
            "SIGNING_PASSWORD",
    };

    private static final String[] OPTIONAL_ENV_IN_MEMORY = new String[]{
            "SIGNING_SUBKEY_ID",
    };

    private final Map<String, String> environment;

    public InMemorySigningMethodConfiguration() {
        this(System.getenv());
    }

    public InMemorySigningMethodConfiguration(Map<String, String> environment) {
        this.environment = environment;
    }

    @Override
    public boolean isEnabled() {
        return Arrays.stream(SIGNING_ENV_IN_MEMORY).allMatch(environment::containsKey);
    }

    @Override
    public void configureSigning(SigningExtension extension) {
        extension.useInMemoryPgpKeys(
                environment.get(OPTIONAL_ENV_IN_MEMORY[0]),
                environment.get(SIGNING_ENV_IN_MEMORY[0]),
                environment.get(SIGNING_ENV_IN_MEMORY[1])
        );
    }

    @Override
    public String getRequiredConfigs() {
        return "environment variables " + Arrays.toString(SIGNING_ENV_IN_MEMORY);
    }

    @Override
    public String getOptionalConfigs() {
        return "environment variables " + Arrays.toString(OPTIONAL_ENV_IN_MEMORY);
    }
}
