package eu.xenit.gradle.enterprise.violations;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

class LogOnlyViolationHandler implements ViolationHandler {

    private static final Logger LOGGER = Logging.getLogger(LogOnlyViolationHandler.class);

    @Override
    public void handleViolation(RuntimeException violation) {
        LOGGER.warn(violation.getMessage(), violation);
    }
}
