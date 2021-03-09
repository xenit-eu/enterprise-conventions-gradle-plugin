package eu.xenit.gradle.enterprise.conventions.violations;

import java.util.Objects;
import javax.annotation.Nonnull;

final class DisabledViolationHandler implements ViolationHandler {

    @Override
    public void handleViolation(@Nonnull RuntimeException violation) {
        Objects.requireNonNull(violation);
        // Do nothing
    }

    static final class Factory implements ViolationHandlerFactory {

        private static final ViolationHandler INSTANCE = new DisabledViolationHandler();

        @Nonnull
        @Override
        public ViolationHandler createEnforcerForCategory(@Nonnull String category) {
            return INSTANCE;
        }
    }
}
