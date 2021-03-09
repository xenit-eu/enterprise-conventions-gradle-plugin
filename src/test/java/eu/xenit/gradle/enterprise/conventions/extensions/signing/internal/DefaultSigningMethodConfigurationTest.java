package eu.xenit.gradle.enterprise.conventions.extensions.signing.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

public class DefaultSigningMethodConfigurationTest {

    @Test
    public void enabledWhenAllPropertiesConfigured() {
        Project project = ProjectBuilder.builder().build();
        SigningMethodConfiguration signingMethodConfiguration = new DefaultSigningMethodConfiguration(project);
        assertFalse(signingMethodConfiguration.isEnabled());
        project.getExtensions().getExtraProperties().set("signing.keyId", "1234");
        assertFalse(signingMethodConfiguration.isEnabled());
        project.getExtensions().getExtraProperties().set("signing.password", "1234");
        assertFalse(signingMethodConfiguration.isEnabled());
        project.getExtensions().getExtraProperties().set("signing.secretKeyRingFile", "secring.gpg");
        assertTrue(signingMethodConfiguration.isEnabled());
    }

}
