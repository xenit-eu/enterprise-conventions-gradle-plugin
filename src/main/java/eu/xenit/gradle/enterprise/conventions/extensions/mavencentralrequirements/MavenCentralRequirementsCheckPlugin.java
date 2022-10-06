package eu.xenit.gradle.enterprise.conventions.extensions.mavencentralrequirements;

import eu.xenit.gradle.enterprise.conventions.api.PluginApi;
import eu.xenit.gradle.enterprise.conventions.api.PublicApi;
import eu.xenit.gradle.enterprise.conventions.violations.ViolationHandler;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;

@PublicApi
public class MavenCentralRequirementsCheckPlugin implements Plugin<Project> {
    @PluginApi
    private final static String PLUGIN_ID = "eu.xenit.enterprise.ext.maven-central-requirements";

    @Override
    public void apply(Project project) {
        ViolationHandler violationHandler = ViolationHandler.fromProject(project, "publishing.publications");
        project.getTasks().withType(PublishToMavenRepository.class).configureEach(publishToMavenRepository -> {
            publishToMavenRepository.doFirst("Validate publication", (Action)new ValidatePublicationAction(violationHandler));
        });
    }

}
