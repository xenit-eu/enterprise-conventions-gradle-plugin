package eu.xenit.gradle.enterprise.conventions.extensions.signing.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

public class GnupgSigningMethodConfigurationTest {

    @Test
    public void disabledByDefault() {
        Project project = ProjectBuilder.builder().build();
        SigningMethodConfiguration signingMethodConfiguration = new GnupgSigningMethodConfiguration(project);
        assertFalse(signingMethodConfiguration.isEnabled());
    }

    @Test
    public void enabledByConfiguration() {
        Project project = ProjectBuilder.builder().build();
        SigningMethodConfiguration signingMethodConfiguration = new GnupgSigningMethodConfiguration(project);
        project.getExtensions().getExtraProperties().set("signing.gnupg.keyName", "abc");
        assertTrue(signingMethodConfiguration.isEnabled());
    }

}
