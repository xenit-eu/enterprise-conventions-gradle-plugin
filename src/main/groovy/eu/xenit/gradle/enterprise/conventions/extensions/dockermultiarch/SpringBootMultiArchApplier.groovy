package eu.xenit.gradle.enterprise.conventions.extensions.dockermultiarch

import org.gradle.api.Project
import org.gradle.api.tasks.Exec

/*
 * Groovy for classloader reasons (see the docker-image-labels appliers): the Spring Boot plugin, and its
 * BootBuildImage task type, is a project plugin while this conventions plugin is a settings plugin. Their
 * classloaders differ, so BootBuildImage can only be touched dynamically, which Groovy does naturally.
 *
 * Buildpacks build a single platform per run, so a multi-arch image is produced by building each architecture
 * separately and combining the results. When the imagePlatform property is set, bootBuildImage is told to build
 * that platform and the built image is tagged with an architecture suffix (…:<tag>-amd64); combineImageManifest
 * then combines the per-arch tags into the base tag via `docker buildx imagetools create`.
 *
 * The suffix is applied in afterEvaluate so the project's own `bootBuildImage { imageName = ... }` has been
 * applied first. imagePlatform exists on BootBuildImage since Spring Boot 3.4, so it is set only when present;
 * on older Spring Boot only the tag suffix is applied.
 */
class SpringBootMultiArchApplier implements MultiArchApplier {

    static final String PLATFORM_PROPERTY = "imagePlatform"
    static final String COMBINE_TASK = "combineImageManifest"

    @Override
    void accept(Project project) {
        project.getPlugins().withId("org.springframework.boot") {
            configurePerArchBuild(project)
            registerCombineTask(project)
        }
    }

    private static void configurePerArchBuild(Project project) {
        def platform = project.findProperty(PLATFORM_PROPERTY)
        if (!platform) {
            return
        }
        def arch = archSuffix(platform.toString())
        project.afterEvaluate {
            def bootBuildImage = project.getTasks().findByName("bootBuildImage")
            if (bootBuildImage == null) {
                return
            }
            if (bootBuildImage.metaClass.getMetaProperty("imagePlatform") != null) {
                bootBuildImage.imagePlatform.set(platform.toString())
            }
            def base = bootBuildImage.imageName.get()
            bootBuildImage.imageName.set(base + "-" + arch)
        }
    }

    private static void registerCombineTask(Project project) {
        project.getTasks().register(COMBINE_TASK, Exec) { exec ->
            exec.group = "build"
            exec.description =
                    "Combine the per-architecture images (…-amd64, …-arm64) into one multi-arch manifest"
            exec.doFirst {
                def base = project.getTasks().getByName("bootBuildImage").imageName.get()
                exec.commandLine("docker", "buildx", "imagetools", "create",
                        "-t", base, base + "-amd64", base + "-arm64")
            }
        }
    }

    private static String archSuffix(String platform) {
        def parts = platform.split("/")
        return parts.length > 1 ? parts[1] : parts[0]
    }
}
