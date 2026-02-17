package eu.xenit.gradle.enterprise.conventions.extensions.mavencentral.publish;

import java.util.Objects;
import javax.inject.Inject;
import lombok.ToString;
import lombok.Value;
import nmcp.NmcpAggregationExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.publish.plugins.PublishingPlugin;
import org.gradle.util.GradleVersion;

public class MavenCentralPublishRootProjectPlugin implements Plugin<Project> {

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
        // Not using providers.credentials(), because that throws when credentials are not available
        // (even when checking isPresent() only)
        publishCredentials = providers.zip(
                providers.gradleProperty("mavenCentralPublishUsername"),
                providers.gradleProperty("mavenCentralPublishPassword"),
                Credentials::new
        );
    }

    public void registerPublication(Project project) {
        project.getPlugins().withType(MavenPublishPlugin.class, _plugin -> {
            project.getTasks().named(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME).configure(publishTask -> {
                // Configure nmcp publish (which performs the upload to maven central) to run after the publish tasks
                // so users can always use './gradlew publish' to publish their artifacts
                if(this.isSnapshot(publishTask)) {
                    publishTask.finalizedBy(":publishAggregationToCentralSnapshots");
                } else {
                    publishTask.finalizedBy(":publishAggregationToCentralPortal");
                }
            });
        });
    }

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(BasePlugin.class);
        project.getPluginManager().apply("com.gradleup.nmcp.aggregation");
        project.getExtensions().configure(NmcpAggregationExtension.class, nmcpAgg ->
                nmcpAgg.centralPortal(centralPortal -> {
                    centralPortal.getUsername().set(publishCredentials.map(Credentials::getUsername));
                    centralPortal.getPassword().set(publishCredentials.map(Credentials::getPassword));
                })
        );

        project.allprojects(aProject -> {
            // nmcp ignores the projects that don't have its plugin applied
            var dep = project.getDependencies().create(aProject);
            project.getDependencies().add("nmcpAggregation", dep);
        });
    }

    private boolean isRelease(Task task) {
        return !isSnapshot(task);
    }

    private boolean isSnapshot(Task task) {
        return Objects.toString(task.getProject().getVersion()).endsWith("-SNAPSHOT");
    }
}
