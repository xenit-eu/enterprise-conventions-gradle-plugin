package eu.xenit.gradle.enterprise.conventions.extensions.signing.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

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
