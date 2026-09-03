package eu.xenit.gradle.enterprise.conventions.extensions.spotless

import eu.xenit.gradle.enterprise.conventions.violations.ViolationHandler
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property

/**
 * Groovy because of classloader reasons, see
 * {@link eu.xenit.gradle.enterprise.conventions.extensions.dockerimagelabels.GradleDockerPluginImageLabelsApplier}.
 * Nothing here is compiled against Spotless, so the project keeps full control of the Spotless version.
 *
 * Spotless does not evaluate `spotless { java { ... } }` blocks immediately; it collects them as lazy
 * actions and executes them, in registration order, from an afterEvaluate that it registers the first time
 * a format is created (SpotlessExtension.format -> FormatExtension.lazyActions).
 *
 * That is what lets these defaults merge with the project's own configuration. Registering our action from
 * an afterEvaluate that was itself registered when this plugin was applied puts our action at the END of
 * that list, so it runs after everything the project configured and can skip the steps the project already
 * declared. Skipping is not just politeness: adding a step twice is a hard error in Spotless
 * ("Multiple steps with name '...'").
 *
 * The ordering only holds if this plugin is applied before the project configures the java format, which is
 * guaranteed when it is applied in settings.gradle. If it is applied later, our action is appended to a list
 * that has already been iterated and silently never runs; the check below turns that silent loss of
 * conventions into a policy violation.
 */
class SpotlessConventionsApplier implements Action<Project> {

    private static final String SPOTLESS_PLUGIN_ID = "com.diffplug.spotless"
    private static final String SPOTLESS_JAVA_TASK = "spotlessJava"
    private static final String VIOLATION_CATEGORY = "spotless"

    @Override
    void execute(Project project) {
        project.pluginManager.withPlugin(SPOTLESS_PLUGIN_ID) {
            def conventions = project.extensions.create(SpotlessConventionsExtension.NAME,
                    SpotlessConventionsExtension)
            // Spotless' java format infers its target from the java source sets, and fails without them.
            // Projects that use Spotless for non-java formats only must not get a java format forced on them.
            project.pluginManager.withPlugin("java") {
                configureJavaDefaults(project, conventions)
            }
        }
    }

    private static void configureJavaDefaults(Project project, SpotlessConventionsExtension conventions) {
        // Spotless registers the afterEvaluate that runs the lazy actions when it creates the java format,
        // which is also when it registers the spotlessJava task. If that task already exists here, its
        // afterEvaluate is ahead of the one we are about to register, and our defaults would be appended to
        // a list that is never iterated again. Checking the task names keeps the task itself unrealized.
        if (project.tasks.names.contains(SPOTLESS_JAVA_TASK)) {
            reportLateApplication(project)
            return
        }

        project.afterEvaluate {
            if (!conventions.enabled.get()) {
                project.logger.info("Spotless conventions are disabled for project '{}'", project.path)
                return
            }
            project.extensions.getByName("spotless").java({ javaExtension ->
                applyJavaDefaults(project, conventions, javaExtension)
            } as Action)
        }
    }

    private static void applyJavaDefaults(Project project, SpotlessConventionsExtension conventions,
            javaExtension) {
        addDefaultStep(project, javaExtension, conventions.java.removeUnusedImports,
                "removeUnusedImports", "removeUnusedImports")
        // Spotless names this step in lowercase, unlike the method that adds it.
        addDefaultStep(project, javaExtension, conventions.java.expandWildcardImports,
                "expandWildcardImports", "expandwildcardimports")
    }

    private static void addDefaultStep(Project project, javaExtension, Property<Boolean> enabled, String method,
            String stepName) {
        if (!enabled.get()) {
            project.logger.info("Spotless default '{}' is disabled for project '{}'", method, project.path)
            return
        }
        if (javaExtension.getExistingStepIdx(stepName) != -1) {
            project.logger.info("Spotless step '{}' is configured by project '{}' itself,"
                    + " not applying the default", method, project.path)
            return
        }
        if (!javaExtension.respondsTo(method)) {
            project.logger.warn("Spotless step '{}' is not supported by the Spotless version of project '{}',"
                    + " not applying the default", method, project.path)
            return
        }
        javaExtension."$method"()
    }

    private static void reportLateApplication(Project project) {
        ViolationHandler violationHandler = ViolationHandler.fromProject(project, VIOLATION_CATEGORY)
        // Reported once the whole build is configured, so the failure is not tangled up in the evaluation of
        // the build script that applied this plugin too late.
        project.gradle.projectsEvaluated {
            violationHandler.handleViolation(new IllegalStateException(
                    "Spotless conventions were not applied to project '${project.path}'. The conventions plugin"
                            + " was applied after the spotless java format was configured, so its defaults would"
                            + " be silently dropped. Apply the conventions plugin in settings.gradle, or above"
                            + " the spotless configuration in build.gradle.".toString()))
        }
    }
}
