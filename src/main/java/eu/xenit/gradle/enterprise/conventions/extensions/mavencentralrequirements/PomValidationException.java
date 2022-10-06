package eu.xenit.gradle.enterprise.conventions.extensions.mavencentralrequirements;

import lombok.Getter;
import org.gradle.api.publish.maven.MavenPublication;

public class PomValidationException extends
        MavenCentralRequirementsCheckException {

    public enum ErrorType {
        ABSENT("required but is absent"),
        EMPTY("required but is empty"),
        INVALID("invalid");

        private final String description;

        private ErrorType(String description) {
            this.description = description;
        }
    }

    @Getter
    private final String name;

    @Getter
    private final ErrorType errorType;

    public PomValidationException(String name, ErrorType errorType) {
        super(String.format("Property '%s' is %s", name, errorType.description));
        this.name = name;
        this.errorType = errorType;
    }

    public PomValidationException(String name, ErrorType errorType, Throwable cause) {
        super(String.format("Property '%s' is %s: %s", name, errorType.description, cause.getMessage()));
        this.name = name;
        this.errorType = errorType;
        initCause(cause);
    }
}
