package eu.xenit.gradle.enterprise.violations;

class DisabledViolationHandler implements ViolationHandler {

    @Override
    public void handleViolation(RuntimeException violation) {
        // Do nothing
    }
}
