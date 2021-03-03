package eu.xenit.gradle.enterprise.conventions.violations;

import javax.annotation.Nonnull;

interface ViolationHandlerFactory {

    @Nonnull
    ViolationHandler createEnforcerForCategory(@Nonnull String category);
}
