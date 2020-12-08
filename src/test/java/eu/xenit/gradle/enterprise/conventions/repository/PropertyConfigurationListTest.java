package eu.xenit.gradle.enterprise.conventions.repository;

import static org.gradle.internal.impldep.org.junit.Assert.assertFalse;
import static org.gradle.internal.impldep.org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class PropertyConfigurationListTest {

    @Test
    public void containsHost() {
        Map<String, Object> propertiesMap = new HashMap<>();

        propertiesMap.put("some-property.example.com", "true");
        propertiesMap.put("my-prefix.example.org", "true");
        propertiesMap.put("my-prefix.www.example.com", "false");

        PropertyConfigurationList configurationList = new PropertyConfigurationList(propertiesMap, "my-prefix.");

        assertTrue(configurationList.containsHost(URI.create("http://example.org")));
        assertFalse(configurationList.containsHost(URI.create("http://www.example.org")));
        assertFalse(configurationList.containsHost(URI.create("http://example.com")));

    }
}
