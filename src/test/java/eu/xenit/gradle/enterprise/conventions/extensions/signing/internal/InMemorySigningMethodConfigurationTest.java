package eu.xenit.gradle.enterprise.conventions.extensions.signing.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.gradle.plugins.signing.SigningExtension;
import org.junit.Test;
import org.mockito.Mockito;

public class InMemorySigningMethodConfigurationTest {

    @Test
    public void disabledWithoutEnvironment() {
        Map<String, String> env = new HashMap<>();
        env.put("SIGNING_PRIVATE_KEY", "privkey");
        SigningMethodConfiguration signingMethodConfiguration = new InMemorySigningMethodConfiguration(env);
        assertFalse(signingMethodConfiguration.isEnabled());
        env.remove("SIGNING_PRIVATE_KEY");
        env.put("SIGNING_PASSWORD", "password");
        assertFalse(signingMethodConfiguration.isEnabled());
    }

    @Test
    public void enabledWithEnvironmentSimple() {
        Map<String, String> env = new HashMap<>();
        env.put("SIGNING_PRIVATE_KEY", "privkey");
        env.put("SIGNING_PASSWORD", "password");
        SigningMethodConfiguration signingMethodConfiguration = new InMemorySigningMethodConfiguration(env);

        assertTrue(signingMethodConfiguration.isEnabled());

        SigningExtension signingExtension = Mockito.mock(SigningExtension.class);

        signingMethodConfiguration.configureSigning(signingExtension);

        Mockito.verify(signingExtension).useInMemoryPgpKeys(null, "privkey", "password");
    }

    @Test
    public void enabledWithEnvironmentSubkey() {
        Map<String, String> env = new HashMap<>();
        env.put("SIGNING_PRIVATE_KEY", "privkey");
        env.put("SIGNING_SUBKEY_ID", "1234");
        env.put("SIGNING_PASSWORD", "password");
        SigningMethodConfiguration signingMethodConfiguration = new InMemorySigningMethodConfiguration(env);

        assertTrue(signingMethodConfiguration.isEnabled());

        SigningExtension signingExtension = Mockito.mock(SigningExtension.class);

        signingMethodConfiguration.configureSigning(signingExtension);

        Mockito.verify(signingExtension).useInMemoryPgpKeys("1234", "privkey", "password");
    }
}
