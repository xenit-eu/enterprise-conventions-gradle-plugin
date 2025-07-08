package eu.xenit.gradle.enterprise.conventions.extensions.mavencentral.publish;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import lombok.ToString;
import lombok.Value;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.util.GradleVersion;
import org.jreleaser.gradle.plugin.JReleaserExtension;
import org.jreleaser.gradle.plugin.JReleaserPlugin;
import org.jreleaser.gradle.plugin.tasks.AbstractJReleaserTask;
import org.jreleaser.model.Active;

public class MavenCentralPublishRootProjectPlugin implements Plugin<Project> {
    private final DirectoryProperty stagingRepo;
    private final Set<TaskCollection<PublishToMavenRepository>> dependencies = new HashSet<>();
    private final Provider<Credentials> publishCredentials;

    static {
        if(GradleVersion.current().compareTo(GradleVersion.version("7.3.0")) < 0) {
            throw new RuntimeException("At least Gradle 7.3 is required for this plugin");
        }
    }

    @Value
    private static class Credentials {
        String username;

        @ToString.Exclude
        String password;
    }

    @Inject
    public MavenCentralPublishRootProjectPlugin(ObjectFactory objectFactory, ProviderFactory providers) {
        stagingRepo = objectFactory.directoryProperty();
        // Not using providers.credentials(), because that throws when credentials are not available
        // (even when checking isPresent() only)
        publishCredentials = providers.zip(
                providers.gradleProperty("mavenCentralPublishUsername").forUseAtConfigurationTime(),
                providers.gradleProperty("mavenCentralPublishPassword").forUseAtConfigurationTime(),
                Credentials::new
        ).forUseAtConfigurationTime();
    }

    public void registerPublication(Project project) {
        project.getPlugins().withType(MavenPublishPlugin.class, _plugin -> {
            var publishingExtension = project.getExtensions().getByType(PublishingExtension.class);

            // Snapshot Repository
            var snapshotRepo = publishingExtension.getRepositories().maven(snapshotRepository -> {
                snapshotRepository.setName("CentralSnapshots");
                snapshotRepository.setUrl("https://central.sonatype.com/repository/maven-snapshots/");
                snapshotRepository.credentials(creds -> {
                    if(publishCredentials.isPresent()) {
                        creds.setUsername(publishCredentials.get().getUsername());
                        creds.setPassword(publishCredentials.get().getPassword());
                    }
                });
            });

            // Release Repository
            var jreleaserStagingMavenRepo = publishingExtension.getRepositories().maven(stagingRepository -> {
                stagingRepository.setName("JReleaserStaging");
                stagingRepository.setUrl(stagingRepo);
            });

            project.getTasks().withType(PublishToMavenRepository.class).configureEach(publishTask -> {
                publishTask.onlyIf(t -> {
                    var isSnapshot = Objects.toString(project.getVersion()).endsWith("-SNAPSHOT");
                    if(Objects.equals(publishTask.getRepository(), snapshotRepo)) {
                        return isSnapshot;
                    }
                    if(Objects.equals(publishTask.getRepository(), jreleaserStagingMavenRepo)) {
                        return !isSnapshot;
                    }
                    return true;
                });

                // Configure JReleaser deploy (which performs the upload to maven central) to run after the publish tasks
                // so users can always use './gradlew publish' to publish their artifacts
                publishTask.finalizedBy(":jreleaserDeploy");
            });

            dependencies.add(project.getTasks().withType(PublishToMavenRepository.class)
                    .matching(task -> Objects.equals(task.getRepository(), jreleaserStagingMavenRepo)));
        });
    }

    @Override
    public void apply(Project project) {
        // Make sure that only one set of dependencies is published by jreleaser by generating a unique repository folder for every build
        stagingRepo.set(project.getLayout().getBuildDirectory().dir("jreleaser-staging-repo-"+ UUID.randomUUID()));
        project.getPlugins().apply(BasePlugin.class);
        project.getPlugins().apply(JReleaserPlugin.class);
        project.getPlugins().withType(JReleaserPlugin.class, _plugin -> {
            var jreleaserExtension = project.getExtensions().getByType(JReleaserExtension.class);

            jreleaserExtension.getDeploy()
                    .getMaven()
                    .getMavenCentral()
                    .register("release", mavenCentral -> {
                        mavenCentral.getActive().convention(Active.RELEASE);
                        mavenCentral.getUrl().set("https://central.sonatype.com/api/v1/publisher");
                        mavenCentral.getStagingRepositories().convention(
                                stagingRepo.map(dir -> List.of(dir.getAsFile().getPath()))
                        );
                        mavenCentral.getSign().convention(false);
                        mavenCentral.getApplyMavenCentralRules().convention(true);
                        mavenCentral.getUsername().set(publishCredentials.map(Credentials::getUsername));
                        mavenCentral.getPassword().set(publishCredentials.map(Credentials::getPassword));
                    });
            jreleaserExtension.getGitRootSearch().convention(true);

            jreleaserExtension.release(release -> {
                // This is just a placeholder release configuration that will not really do anything.
                // This is because JReleaser requires a release configuration to be able to deploy artifacts
                release.getGeneric().getSkipTag().convention(true);
                release.getGeneric().getSkipRelease().convention(true);
                release.getGeneric().getToken().convention("__UNSET__");
                release.getGeneric().getChangelog().getEnabled().convention(false);
            });
        });

        // Set up dependencies for all JReleaser tasks to require publication to the staging repo to be finished
        project.getTasks().withType(AbstractJReleaserTask.class, jReleaserTask -> {
            jReleaserTask.dependsOn(dependencies);
        });
    }
}
