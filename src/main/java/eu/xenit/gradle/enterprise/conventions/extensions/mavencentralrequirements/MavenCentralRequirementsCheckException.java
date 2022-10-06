package eu.xenit.gradle.enterprise.conventions.extensions.mavencentralrequirements;

import org.gradle.api.publish.maven.MavenPublication;

public class MavenCentralRequirementsCheckException extends RuntimeException {
    public MavenCentralRequirementsCheckException(MavenPublication publication, String message) {
        super(String.format("Publication '%s': %s", publication.getName(), message));
    }
}
