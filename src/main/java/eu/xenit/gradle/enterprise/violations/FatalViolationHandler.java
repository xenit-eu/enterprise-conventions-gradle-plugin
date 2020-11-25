package eu.xenit.gradle.enterprise.violations;

class FatalViolationHandler implements ViolationHandler {

    @Override
    public void handleViolation(RuntimeException violation) {
        throw violation;
    }
}
