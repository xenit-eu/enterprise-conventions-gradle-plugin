package eu.xenit.gradle.enterprise.conventions.extensions.mavencentralrequirements;

public class SignatureValidationException extends MavenCentralRequirementsCheckException {
    public SignatureValidationException(String artifactIdentifier, String signatureIdentifier) {
        super(String.format("Artifact '%s' must be signed, but signature '%s' is missing.", artifactIdentifier, signatureIdentifier));
    }

}
