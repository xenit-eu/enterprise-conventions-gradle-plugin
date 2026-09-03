package eu.xenit.gradle.enterprise.conventions.extensions.spotless

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Groovy because of classloader reasons, see
 * {@link eu.xenit.gradle.enterprise.conventions.extensions.dockerimagelabels.GradleDockerPluginImageLabelsApplier}.
 * Nothing here is compiled against Spotless, so the project keeps full control of the Spotless version.
 *
 * Spotless does not evaluate `spotless { java { ... } }` blocks immediately; it collects them as lazy
 * actions and executes them, in registration order, from an afterEvaluate that it registers the first time
 * a format is created (SpotlessExtension.format -> FormatExtension.lazyActions).
 *
 * The defaults are registered as soon as this plugin sees both the spotless and the java plugin, which is
 * before the build script body runs when the conventions plugin comes from settings.gradle. The project's
 * own configuration is therefore applied on top of the defaults. There is no opt-out of its own: a project
 * that wants different rules either declares steps that do not clash with the defaults, or calls Spotless'
 * own `clearSteps()` and declares its own set from scratch. Declaring a default step a second time is a hard
 * error in Spotless ("Multiple steps with name '...'").
 *
 * A default step the project's Spotless version does not have fails the build. Applying a reduced set of
 * steps is exactly the silent gap these conventions exist to prevent, so the only way out is upgrading
 * Spotless. The project's own `clearSteps()` cannot soften that either, because it runs after this action.
 */
class SpotlessConventionsApplier implements Action<Project> {

    private static final String SPOTLESS_PLUGIN_ID = "com.diffplug.spotless"
    private static final List<String> JAVA_DEFAULT_STEPS = ["removeUnusedImports", "expandWildcardImports"]

    @Override
    void execute(Project project) {
        project.pluginManager.withPlugin(SPOTLESS_PLUGIN_ID) {
            // Spotless' java format infers its target from the java source sets, and fails without them.
            // Projects that use Spotless for non-java formats only must not get a java format forced on them.
            project.pluginManager.withPlugin("java") {
                project.extensions.getByName("spotless").java({ javaExtension ->
                    JAVA_DEFAULT_STEPS.each { addDefaultStep(project, javaExtension, it) }
                } as Action)
            }
        }
    }

    private static void addDefaultStep(Project project, javaExtension, String method) {
        if (!javaExtension.respondsTo(method)) {
            throw new GradleException(
                    ("Spotless step '${method}' is not supported by the Spotless version of project"
                            + " '${project.path}'. Upgrade Spotless to a version that provides it; the"
                            + " conventions do not apply a reduced set of steps.").toString())
        }
        javaExtension."$method"()
    }
}
