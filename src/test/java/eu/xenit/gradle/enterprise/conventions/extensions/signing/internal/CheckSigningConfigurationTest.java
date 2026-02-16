package eu.xenit.gradle.enterprise.conventions.extensions.signing.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.xenit.gradle.enterprise.conventions.extensions.signing.AbstractSigningPluginSetup;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.gradle.plugins.signing.Sign;
import org.gradle.plugins.signing.SigningExtension;
import org.junit.jupiter.api.Test;

public class CheckSigningConfigurationTest extends AbstractSigningPluginSetup {

    @Test
    public void throwsOnNonExistingSignatory() {
        Project project = createProject(p -> {
        });

        Sign signTask = project.getTasks().withType(Sign.class).getByName("signMavenJavaPublication");

        CheckSigningConfiguration checkSigningConfiguration = new CheckSigningConfiguration(signTask,
                new MockSigningMethodConfiguration());

        InvalidUserDataException exception = assertThrows(InvalidUserDataException.class, () ->
                checkSigningConfiguration.execute(signTask)
        );
        assertThat(exception.getMessage(), containsString("No signing configuration is enabled and signing is required."));
    }

    @Test
    public void doesNotThrowOnExistingSignatory() {
        Project project = createProject(this::configureSigningWithPgp);

        Sign signTask = project.getTasks().withType(Sign.class).getByName("signMavenJavaPublication");

        CheckSigningConfiguration checkSigningConfiguration = new CheckSigningConfiguration(signTask,
                new MockSigningMethodConfiguration());

        checkSigningConfiguration.execute(signTask);
        assertNotNull(signTask.getSignatory());
    }

    private static class MockSigningMethodConfiguration implements SigningMethodConfiguration {

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public void configureSigning(SigningExtension extension) {

        }

        @Override
        public String getRequiredConfigs() {
            return "ABC";
        }

        @Override
        public String getOptionalConfigs() {
            return "DEF";
        }
    }
}
