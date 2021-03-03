package eu.xenit.gradle.enterprise.conventions.publish;

import eu.xenit.gradle.enterprise.conventions.extensions.signing.AutomaticSigningPlugin;
import eu.xenit.gradle.enterprise.conventions.violations.ViolationHandler;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

abstract class AbstractPublishPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().withType(MavenPublishPlugin.class, publishPlugin -> {
            PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
            validatePublishRepositories(project, publishing);
        });
        project.getPlugins().apply(AutomaticSigningPlugin.class);
    }

    private void validatePublishRepositories(Project project, PublishingExtension publishing) {
        ViolationHandler violationHandler = ViolationHandler.fromProject(project, "publishing.repository");
        publishing.getRepositories().all(repository -> {
            if (repository instanceof MavenArtifactRepository) {
                validatePublishRepository(violationHandler, (MavenArtifactRepository) repository);
            }
        });

    }

    protected abstract void validatePublishRepository(ViolationHandler violationHandler,
            MavenArtifactRepository repository);
}
