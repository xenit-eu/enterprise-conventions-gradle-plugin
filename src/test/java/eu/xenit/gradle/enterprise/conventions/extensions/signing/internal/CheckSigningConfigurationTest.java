package eu.xenit.gradle.enterprise.conventions.extensions.signing.internal;

import static org.junit.Assert.assertNotNull;

import eu.xenit.gradle.enterprise.conventions.extensions.signing.AbstractSigningPluginSetup;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.gradle.plugins.signing.Sign;
import org.gradle.plugins.signing.SigningExtension;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CheckSigningConfigurationTest extends AbstractSigningPluginSetup {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void throwsOnNonExistingSignatory() {
        Project project = createProject(p -> {
        });

        Sign signTask = project.getTasks().withType(Sign.class).getByName("signMavenJavaPublication");

        CheckSigningConfiguration checkSigningConfiguration = new CheckSigningConfiguration(signTask,
                new MockSigningMethodConfiguration());

        expectedException.expect(InvalidUserDataException.class);
        expectedException.expectMessage("No signing configuration is enabled and signing is required.");
        checkSigningConfiguration.execute(signTask);
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
