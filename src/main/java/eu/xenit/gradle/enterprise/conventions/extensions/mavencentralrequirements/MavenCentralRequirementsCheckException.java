package eu.xenit.gradle.enterprise.conventions.extensions.mavencentralrequirements;

public class MavenCentralRequirementsCheckException extends RuntimeException{

    public MavenCentralRequirementsCheckException(String message) {
        super(message);
    }

    public MavenCentralRequirementsCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public MavenCentralRequirementsCheckException(Throwable cause) {
        super(cause);
    }

    protected MavenCentralRequirementsCheckException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
