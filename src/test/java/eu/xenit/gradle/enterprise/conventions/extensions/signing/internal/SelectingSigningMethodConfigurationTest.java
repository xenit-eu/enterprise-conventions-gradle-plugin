package eu.xenit.gradle.enterprise.conventions.extensions.signing.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.gradle.plugins.signing.SigningExtension;
import org.junit.Test;
import org.mockito.Mockito;

public class SelectingSigningMethodConfigurationTest {

    @Test
    public void selectWithNoneEnabled() {
        SigningMethodConfiguration signingMethodConfiguration = new SelectingSigningMethodConfiguration(Arrays.asList(
                new SigningMethodConfiguration() {
                    @Override
                    public boolean isEnabled() {
                        return false;
                    }

                    @Override
                    public void configureSigning(SigningExtension extension) {
                        throw new IllegalStateException("Signing method is not enabled.");
                    }

                    @Override
                    public String getRequiredConfigs() {
                        return "world domination";
                    }

                    @Override
                    public String getOptionalConfigs() {
                        return "evil doctor";
                    }
                }
        ));

        assertFalse(signingMethodConfiguration.isEnabled());
        assertEquals("None", signingMethodConfiguration.getName());
        assertEquals("one of [world domination]", signingMethodConfiguration.getRequiredConfigs());
        assertEquals("depends on selected signing method", signingMethodConfiguration.getOptionalConfigs());

        SigningExtension signingExtensionMock = Mockito.mock(SigningExtension.class);

        signingMethodConfiguration.configureSigning(signingExtensionMock);

        Mockito.verifyNoInteractions(signingExtensionMock);

    }

    @Test
    public void selectFirstEnabledOnly() {
        SigningMethodConfiguration signingMethodConfiguration = new SelectingSigningMethodConfiguration(Arrays.asList(
                new SigningMethodConfiguration() {
                    @Override
                    public boolean isEnabled() {
                        return true;
                    }

                    @Override
                    public void configureSigning(SigningExtension extension) {
                        extension.setRequired(true);
                    }

                    @Override
                    public String getRequiredConfigs() {
                        return "world domination";
                    }

                    @Override
                    public String getOptionalConfigs() {
                        return "evil doctor";
                    }
                },
                new SigningMethodConfiguration() {
                    @Override
                    public boolean isEnabled() {
                        return true;
                    }

                    @Override
                    public void configureSigning(SigningExtension extension) {
                        throw new IllegalStateException("Signing should not happen with this method");
                    }

                    @Override
                    public String getRequiredConfigs() {
                        return "space travel";
                    }

                    @Override
                    public String getOptionalConfigs() {
                        return "hyperdrive";
                    }
                }
        ));

        assertTrue(signingMethodConfiguration.isEnabled());
        assertEquals("world domination", signingMethodConfiguration.getRequiredConfigs());
        assertEquals("evil doctor", signingMethodConfiguration.getOptionalConfigs());

        SigningExtension signingExtensionMock = Mockito.mock(SigningExtension.class);

        signingMethodConfiguration.configureSigning(signingExtensionMock);

        Mockito.verify(signingExtensionMock).setRequired(true);
        Mockito.verifyNoMoreInteractions(signingExtensionMock);
    }

    @Test
    public void selectFirstEnabled() {
        SigningMethodConfiguration signingMethodConfiguration = new SelectingSigningMethodConfiguration(Arrays.asList(
                new SigningMethodConfiguration() {
                    @Override
                    public boolean isEnabled() {
                        return false;
                    }

                    @Override
                    public void configureSigning(SigningExtension extension) {
                        throw new IllegalStateException("Signing should not happen with this method");
                    }

                    @Override
                    public String getRequiredConfigs() {
                        return "world domination";
                    }

                    @Override
                    public String getOptionalConfigs() {
                        return "evil doctor";
                    }
                },
                new SigningMethodConfiguration() {
                    @Override
                    public boolean isEnabled() {
                        return true;
                    }

                    @Override
                    public void configureSigning(SigningExtension extension) {
                        extension.setRequired(true);
                    }

                    @Override
                    public String getRequiredConfigs() {
                        return "space travel";
                    }

                    @Override
                    public String getOptionalConfigs() {
                        return "hyperdrive";
                    }
                }
        ));

        assertTrue(signingMethodConfiguration.isEnabled());
        assertEquals("space travel", signingMethodConfiguration.getRequiredConfigs());
        assertEquals("hyperdrive", signingMethodConfiguration.getOptionalConfigs());

        SigningExtension signingExtensionMock = Mockito.mock(SigningExtension.class);

        signingMethodConfiguration.configureSigning(signingExtensionMock);

        Mockito.verify(signingExtensionMock).setRequired(true);
        Mockito.verifyNoMoreInteractions(signingExtensionMock);
    }
}
