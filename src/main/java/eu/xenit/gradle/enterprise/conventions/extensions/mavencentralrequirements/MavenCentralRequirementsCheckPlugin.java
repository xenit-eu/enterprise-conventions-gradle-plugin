package eu.xenit.gradle.enterprise.conventions.extensions.mavencentralrequirements;


import static org.apache.commons.lang.StringUtils.capitalize;

import eu.xenit.gradle.enterprise.conventions.api.PluginApi;
import eu.xenit.gradle.enterprise.conventions.api.PublicApi;
import eu.xenit.gradle.enterprise.conventions.violations.ViolationHandler;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;
import org.gradle.api.publish.plugins.PublishingPlugin;

@PublicApi
public class MavenCentralRequirementsCheckPlugin implements Plugin<Project> {
    @PluginApi
    private static final String PLUGIN_ID = "eu.xenit.enterprise-conventions.ext.maven-central-requirements";

    @Override
    public void apply(Project project) {
        ViolationHandler violationHandler = ViolationHandler.fromProject(project, "publishing.publications");
        project.getTasks().withType(PublishToMavenRepository.class).configureEach(publishToMavenRepository ->
                publishToMavenRepository.doFirst(
                        "Validate publication",
                        new ValidatePublicationAction(publishToMavenRepository::getPublication, violationHandler)
                )
        );

        project.getPlugins().withType(MavenPublishPlugin.class, mavenPublishPlugin -> {
            var publishingExtension = project.getExtensions().getByType(PublishingExtension.class);

            publishingExtension.getPublications().all(publication -> {
                if(publication instanceof MavenPublication) {
                    project.getTasks()
                            .register("checkMavenCentralRequirementsFor" + capitalize(publication.getName()) + "Publication",
                                    CheckMavenCentralRequirements.class).configure(checkCentralRequirements -> {
                                checkCentralRequirements.setDescription(String.format(
                                        "Check that the '%s' publication conforms to the requirements to publish to Maven Central", publication.getName()));
                                checkCentralRequirements.setGroup(PublishingPlugin.PUBLISH_TASK_GROUP);
                                checkCentralRequirements.getPublication().set((MavenPublication) publication);
                            });
                }
            });
        });
        project.getTasks().register("checkMavenCentralRequirements").configure(task -> {
            task.setDescription("Check that all publications confirm to the requirements to publish to Maven Central");
            task.setGroup(PublishingPlugin.PUBLISH_TASK_GROUP);
            task.dependsOn(project.getTasks().withType(CheckMavenCentralRequirements.class));
        });
    }

}
