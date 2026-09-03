package eu.xenit.gradle.enterprise.conventions.extensions.spotless

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Groovy because of classloader reasons, see
 * {@link eu.xenit.gradle.enterprise.conventions.extensions.dockerimagelabels.GradleDockerPluginImageLabelsApplier}.
 * Nothing here is compiled against Spotless, so the project keeps full control of the Spotless version.
 */
class SpotlessConventionsApplier implements Action<Project> {

    private static final String SPOTLESS_PLUGIN_ID = "com.diffplug.spotless"
    private static final List<String> JAVA_DEFAULT_STEPS = ["removeUnusedImports", "expandWildcardImports"]

    @Override
    void execute(Project project) {
        project.pluginManager.withPlugin(SPOTLESS_PLUGIN_ID) {
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
