package eu.xenit.gradle.enterprise.conventions.extensions.mavencentralrequirements;

import org.gradle.api.publish.maven.MavenPublication;

public class SignatureValidationException extends MavenCentralRequirementsCheckException {
    public SignatureValidationException(MavenPublication publication, String artifactIdentifier, String signatureIdentifier) {
        super(publication, String.format("Artifact '%s' must be signed, but signature '%s' is missing.", artifactIdentifier, signatureIdentifier));
    }

}
