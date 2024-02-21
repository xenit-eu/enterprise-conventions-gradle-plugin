package eu.xenit.gradle.enterprise.conventions.extensions.dockerimagelabels

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider

/*
 * There is a good reason that this class is groovy: Classloader shenanigans.
 *
 * Using Java to do this is very difficult because the conventions plugin is loaded as a settings plugin;
 * the spring boot plugin is loaded as a project plugin.
 *
 * Settings and project plugins use different classloaders: the settings classloader is a parent of the project classloader.
 * Classes from the project classloader are not available directly in classes loaded by the settings classloader:
 * resolution of a referenced class always start in the classloader that has loaded the referencing class, and then go up the parent hierarchy.
 *
 * We already need to jump through hoops to get access to the task type; but we can only work with those classes via reflection,
 * never via a direct reference.
 * Groovy code is dynamic, and basically uses reflection (or equivalent) all over the place to access properties,
 * never referencing any of the plugin's classes directly.
 * Java code on the other hand would statically typed, and will always need to resort to reflective access to perform the same operations,
 * making the code quite difficult to read.
 */
class SpringBootDockerImageLabelsApplier implements BuildContextInformationApplier {

    void accept(Project project, Provider<BuildContextInformation> buildContextInformationProvider) {
        project.getPlugins().withId("org.springframework.boot") { plugin ->
            def pluginLoader = new PluginClassLoader(plugin)

            Class<Task> bootBuildImageClass = pluginLoader.loadClass("org.springframework.boot.gradle.tasks.bundling.BootBuildImage")

            project.getTasks()
                    .withType(bootBuildImageClass)
                    .configureEach {
                        environment.putAll(buildContextInformationProvider.map {
                            buildContextInformation ->
                                [
                                        "BP_OCI_SOURCE"  : buildContextInformation.getRepository(),
                                        "BP_OCI_REVISION": buildContextInformation.getCommit()
                                ]
                        }.orElse([:]))
                        if (project.description != null) {
                            environment.put("BP_OCI_DESCRIPTION", project.description)
                        }
                    }
        }
    }

}
