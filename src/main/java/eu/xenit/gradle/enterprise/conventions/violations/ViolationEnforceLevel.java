package eu.xenit.gradle.enterprise.conventions.violations;

import javax.annotation.Nonnull;

enum ViolationEnforceLevel implements ViolationHandlerFactory {
    DISABLE(new DisabledViolationHandler.Factory()),
    ENFORCE(new FatalViolationHandler.Factory()),
    LOG(new LogOnlyViolationHandler.Factory());

    private final ViolationHandlerFactory factory;

    ViolationEnforceLevel(ViolationHandlerFactory factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public ViolationHandler createEnforcerForCategory(@Nonnull String category) {
        return factory.createEnforcerForCategory(category);
    }
}
