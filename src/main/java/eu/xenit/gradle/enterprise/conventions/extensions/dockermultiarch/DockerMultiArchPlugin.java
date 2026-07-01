package eu.xenit.gradle.enterprise.conventions.extensions.dockermultiarch;

import eu.xenit.gradle.enterprise.conventions.BasePlugin;
import java.util.ServiceLoader;
import org.gradle.api.Project;

public class DockerMultiArchPlugin extends BasePlugin {

    @Override
    public void apply(Project project) {
        ServiceLoader.load(MultiArchApplier.class)
                .forEach(applier -> applier.accept(project));
    }
}
