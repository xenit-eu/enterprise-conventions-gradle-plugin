package eu.xenit.gradle.enterprise.conventions.extensions.signing.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

public class GnupgSigningMethodConfigurationTest {

    @Test
    public void enabledByDefault() {
        Project project = ProjectBuilder.builder().build();
        SigningMethodConfiguration signingMethodConfiguration = new GnupgSigningMethodConfiguration(project);
        assertTrue(signingMethodConfiguration.isEnabled());
    }

    @Test
    public void disabledByConfiguration() {
        Project project = ProjectBuilder.builder().build();
        SigningMethodConfiguration signingMethodConfiguration = new GnupgSigningMethodConfiguration(project);
        project.getExtensions().getExtraProperties().set("signing.gnupg", false);
        assertFalse(signingMethodConfiguration.isEnabled());
    }

    @Test
    public void enabledByConfiguration() {
        Project project = ProjectBuilder.builder().build();
        SigningMethodConfiguration signingMethodConfiguration = new GnupgSigningMethodConfiguration(project);
        project.getExtensions().getExtraProperties().set("signing.gnupg", true);
        assertTrue(signingMethodConfiguration.isEnabled());
    }

}
