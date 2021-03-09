package eu.xenit.gradle.enterprise.conventions.violations;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

final class LogOnlyViolationHandler implements ViolationHandler {

    private static final Logger LOGGER = Logging.getLogger(LogOnlyViolationHandler.class);
    @Nonnull
    private final String category;

    private LogOnlyViolationHandler(@Nonnull String category) {
        this.category = category;
    }

    @Override
    public void handleViolation(@Nonnull RuntimeException violation) {
        Objects.requireNonNull(violation);
        LOGGER.warn("[{}] {}", category, violation.getMessage(), violation);
    }

    static final class Factory implements ViolationHandlerFactory {

        @Nonnull
        @Override
        public ViolationHandler createEnforcerForCategory(@Nonnull String category) {
            return new LogOnlyViolationHandler(category);
        }
    }
}
