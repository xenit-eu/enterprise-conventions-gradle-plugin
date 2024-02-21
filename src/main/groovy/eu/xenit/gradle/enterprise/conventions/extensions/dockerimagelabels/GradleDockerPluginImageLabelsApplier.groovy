package eu.xenit.gradle.enterprise.conventions.extensions.dockerimagelabels


import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider

class GradleDockerPluginImageLabelsApplier implements BuildContextInformationApplier {

    private static String OCI_PREFIX = "org.opencontainers.image."

    @Override
    void accept(Project project, Provider<BuildContextInformation> buildContextInformationProvider) {
        project.getPlugins().withId("com.bmuschko.docker-remote-api") { plugin ->
            def pluginLoader = new PluginClassLoader(plugin)

            Class<Task> dockerBuildImageClass = pluginLoader.loadClass("com.bmuschko.gradle.docker.tasks.image.DockerBuildImage")

            project.getTasks()
                    .withType(dockerBuildImageClass)
                    .configureEach {
                        labels.putAll(buildContextInformationProvider.map {
                            buildContextInformation ->
                                [
                                        (OCI_PREFIX + "source")  : buildContextInformation.getRepository(),
                                        (OCI_PREFIX + "revision"): buildContextInformation.getCommit()
                                ]
                        }.orElse([:]))
                        labels.put(OCI_PREFIX + "version", project.provider { project.getVersion().toString() })
                        labels.put(OCI_PREFIX + "title", project.name)
                        if (project.description != null) {
                            labels.put(OCI_PREFIX + "description", project.description)
                        }
                    }
        }
    }

}
