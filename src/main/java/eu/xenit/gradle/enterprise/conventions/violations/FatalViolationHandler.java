package eu.xenit.gradle.enterprise.conventions.violations;

class FatalViolationHandler implements ViolationHandler {

    @Override
    public void handleViolation(RuntimeException violation) {
        throw violation;
    }
}
