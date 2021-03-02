package eu.xenit.gradle.enterprise.conventions.extensions.signing.internal;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.plugins.signing.SigningExtension;

public class SelectingSigningMethodConfiguration implements SigningMethodConfiguration {

    private static final Logger LOGGER = Logging.getLogger(SelectingSigningMethodConfiguration.class);

    private final List<? extends SigningMethodConfiguration> signingKeyConfigurations;

    public SelectingSigningMethodConfiguration(List<? extends SigningMethodConfiguration> signingKeyConfigurations) {
        this.signingKeyConfigurations = signingKeyConfigurations;
    }

    private Optional<SigningMethodConfiguration> selectConfiguration() {
        for (SigningMethodConfiguration signingKeyConfiguration : signingKeyConfigurations) {
            if (signingKeyConfiguration.isEnabled()) {
                LOGGER.debug("Selected signing method {}", signingKeyConfiguration.getName());
                return Optional.of(signingKeyConfiguration);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isEnabled() {
        return selectConfiguration().isPresent();
    }

    @Override
    public void configureSigning(SigningExtension extension) {
        selectConfiguration().ifPresent(config -> config.configureSigning(extension));
    }

    @Override
    public String getRequiredConfigs() {
        return selectConfiguration()
                .map(SigningMethodConfiguration::getRequiredConfigs)
                .orElseGet(() -> "one of " + signingKeyConfigurations.stream()
                        .map(SigningMethodConfiguration::getRequiredConfigs)
                        .map(configs -> "[" + configs + "]")
                        .collect(Collectors.joining(", or ")));
    }

    @Override
    public String getOptionalConfigs() {
        return selectConfiguration()
                .map(SigningMethodConfiguration::getOptionalConfigs)
                .orElse("depends on selected signing method");
    }

    @Override
    public String getName() {
        return selectConfiguration()
                .map(SigningMethodConfiguration::getName)
                .orElse("None");
    }
}
