package eu.xenit.gradle.enterprise.conventions.extensions.signing.internal;

import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Task;
import org.gradle.plugins.signing.Sign;

public class CheckSigningConfiguration implements Action<Task> {

    private final Sign sign;
    private final SigningMethodConfiguration signingKeyConfiguration;

    public CheckSigningConfiguration(Sign sign, SigningMethodConfiguration signingKeyConfiguration) {
        this.sign = sign;
        this.signingKeyConfiguration = signingKeyConfiguration;
    }

    @Override
    public void execute(Task task) {
        if (sign.getSignatory() == null) {
            throw new InvalidUserDataException(
                    "No signing configuration is enabled and signing is required. Provide " +
                            signingKeyConfiguration.getRequiredConfigs());
        }
    }
}
