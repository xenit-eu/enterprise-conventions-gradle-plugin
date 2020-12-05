package eu.xenit.gradle.enterprise.repository;

import java.net.URI;
import java.util.Map;

class PropertyConfigurationList {

    private final Map<String, Object> properties;
    private final String prefix;

    PropertyConfigurationList(Map<String, Object> properties, String prefix) {
        this.properties = properties;
        this.prefix = prefix;
    }

    public boolean containsHost(URI uri) {
        return Boolean.parseBoolean(properties.getOrDefault(this.prefix + uri.getHost(), "false").toString());
    }
}
