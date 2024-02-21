package eu.xenit.gradle.enterprise.conventions.extensions.dockerimagelabels

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider

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
