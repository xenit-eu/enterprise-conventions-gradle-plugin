package eu.xenit.gradle.enterprise.conventions.violations;

enum ViolationEnforceLevel {
    DISABLE(new DisabledViolationHandler()),
    ENFORCE(new FatalViolationHandler()),
    LOG(new LogOnlyViolationHandler());

    private final ViolationHandler enforcer;

    private ViolationEnforceLevel(ViolationHandler enforcer) {
        this.enforcer = enforcer;
    }

    public ViolationHandler getEnforcer() {
        return enforcer;
    }
}
