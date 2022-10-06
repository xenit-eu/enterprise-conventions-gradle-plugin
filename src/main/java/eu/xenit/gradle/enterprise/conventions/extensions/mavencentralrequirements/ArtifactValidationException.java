package eu.xenit.gradle.enterprise.conventions.extensions.mavencentralrequirements;

public class ArtifactValidationException extends MavenCentralRequirementsCheckException {
    public ArtifactValidationException(String artifact) {
        super(String.format("Publication is missing required artifact '%s'", artifact));
    }

}
