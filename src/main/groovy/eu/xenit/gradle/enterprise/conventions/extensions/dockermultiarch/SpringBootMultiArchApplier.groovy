package eu.xenit.gradle.enterprise.conventions.extensions.dockermultiarch

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property

/*
 * Groovy for classloader reasons (see the docker-image-labels appliers): the Spring Boot plugin, and its
 * BootBuildImage task type, is a project plugin while this conventions plugin is a settings plugin. Their
 * classloaders differ, so BootBuildImage can only be touched dynamically, which Groovy does naturally.
 *
 * Buildpacks build a single platform per run, so multi-arch images are published as separate
 * per-architecture images: bootBuildLinuxAmd64Image and bootBuildLinuxArm64Image build the image configured
 * on bootBuildImage for that specific platform, with an architecture suffix on the image name and tags.
 * The amd64 task also carries the unsuffixed name and tags, so existing consumers of those keep working
 * while amd64 is built only once in CI. bootBuildImage itself stays untouched, as the host-architecture
 * build for local use.
 *
 * Requires Spring Boot >= 3.4 (imagePlatform): on older versions the tasks fail when they are used.
 */
class SpringBootMultiArchApplier implements Action<Project> {

    private static final List<String> ARCHITECTURES = ["amd64", "arm64"]

    @Override
    void execute(Project project) {
        project.plugins.withId("org.springframework.boot") { springBootPlugin ->
            project.plugins.withType(JavaPlugin) {
                registerPerArchTasks(project, springBootPlugin)
            }
        }
    }

    private static void registerPerArchTasks(Project project, springBootPlugin) {
        def bootBuildImageClass = Class.forName(
                "org.springframework.boot.gradle.tasks.bundling.BootBuildImage",
                false, springBootPlugin.getClass().getClassLoader())
        def bootBuildImage = project.tasks.named("bootBuildImage", bootBuildImageClass)
        ARCHITECTURES.each { String arch ->
            project.tasks.register("bootBuildLinux${arch.capitalize()}Image", bootBuildImageClass) { task ->
                task.description = "Builds the linux/${arch} OCI image for the application"
                task.archiveFile.set(bootBuildImage.flatMap { it.archiveFile })
                task.imagePlatform = "linux/${arch}"
                task.imageName = bootBuildImage.flatMap { source ->
                    source.imageName.map { "${it}-${arch}".toString() }
                }
                task.tags = bootBuildImage.flatMap { source ->
                    source.tags.map { tags -> tags.collect { "${it}-${arch}".toString() } }
                }.orElse([])
                if (arch == "amd64") {
                    task.tags.add(bootBuildImage.flatMap { it.imageName })
                    task.tags.addAll(bootBuildImage.flatMap { it.tags }.orElse([]))
                }
                task.environment.putAll(bootBuildImage.flatMap { it.environment }.orElse([:]))
                copyPublishRegistry(bootBuildImage.get().docker.publishRegistry, task.docker.publishRegistry)
            }
        }
    }

    private static void copyPublishRegistry(source, target) {
        ["url", "username", "password", "token", "email"].each { property ->
            if (source.hasProperty(property) && source."$property" instanceof Property) {
                target."$property".set(source."$property")
            }
        }
    }
}
