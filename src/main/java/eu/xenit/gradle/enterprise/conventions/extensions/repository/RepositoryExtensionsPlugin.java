package eu.xenit.gradle.enterprise.conventions.extensions.repository;

import eu.xenit.gradle.enterprise.conventions.api.PluginApi;
import eu.xenit.gradle.enterprise.conventions.api.PublicApi;
import eu.xenit.gradle.enterprise.conventions.internal.MultipleApplicationTargetsPlugin;
import eu.xenit.gradle.enterprise.conventions.internal.PropertyReader;
import eu.xenit.gradle.enterprise.conventions.internal.StringConstants;
import javax.inject.Inject;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.initialization.Settings;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.util.GradleVersion;

@PublicApi
public class RepositoryExtensionsPlugin implements
        MultipleApplicationTargetsPlugin {

    @PluginApi
    public static final String PLUGIN_ID = "eu.xenit.enterprise.ext.repository";

    private final ObjectFactory objectFactory;

    @Inject
    public RepositoryExtensionsPlugin(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Override
    public void apply(Project project) {
        RepositoryHandlerExtensions.apply(project.getRepositories(), project);
        RepositoryHandlerExtensions.apply(project.getBuildscript().getRepositories(), project);

        project.getPlugins().withType(MavenPublishPlugin.class, publishPlugin -> {
            PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
            PublishRepositoryHandlerExtensions.apply(publishing.getRepositories(), project);

            // Replace publishing target for xenit private repository, because the download URL does not support upload
            publishing.getRepositories().whenObjectAdded(artifactRepository -> {
                if(artifactRepository instanceof MavenArtifactRepository) {
                    MavenArtifactRepository mavenArtifactRepository = (MavenArtifactRepository) artifactRepository;
                    if(mavenArtifactRepository.getUrl().toString().startsWith(StringConstants.XENIT_REPO_URL)) {
                        mavenArtifactRepository.setUrl(StringConstants.XENIT_REPO_PUBLISH_URL);
                    }
                }
            });
        });
    }

    @Override
    public void apply(Settings settings) {
        // Supported starting from 6.8
        if(GradleVersion.current().compareTo(GradleVersion.version("6.8")) >= 0) {
            RepositoryHandler repositoryHandler = settings.getDependencyResolutionManagement().getRepositories();
            RepositoryHandlerExtensions.apply(repositoryHandler, PropertyReader.from(settings), objectFactory);
        }
    }
}
