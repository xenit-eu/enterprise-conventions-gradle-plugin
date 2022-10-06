package eu.xenit.gradle.enterprise.conventions.extensions.mavencentralrequirements;

import org.gradle.api.publish.maven.MavenPublication;

public class ArtifactValidationException extends MavenCentralRequirementsCheckException {
    public ArtifactValidationException(MavenPublication publication, String artifact) {
        super(publication, String.format("missing required artifact '%s'",  artifact));
    }

}
