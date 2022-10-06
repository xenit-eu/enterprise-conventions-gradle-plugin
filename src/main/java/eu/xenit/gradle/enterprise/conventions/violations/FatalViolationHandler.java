package eu.xenit.gradle.enterprise.conventions.violations;

import java.util.Objects;
import javax.annotation.Nonnull;

final class FatalViolationHandler implements ViolationHandler {

    private final String category;

    FatalViolationHandler(String category) {
        this.category = category;
    }

    @Override
    public void handleViolation(@Nonnull RuntimeException violation) {
        Objects.requireNonNull(violation);
        throw new FatalViolation(category, violation);
    }

    static final class Factory implements ViolationHandlerFactory {

        @Nonnull
        @Override
        public ViolationHandler createEnforcerForCategory(@Nonnull String category) {
            return new FatalViolationHandler(category);
        }
    }
}
