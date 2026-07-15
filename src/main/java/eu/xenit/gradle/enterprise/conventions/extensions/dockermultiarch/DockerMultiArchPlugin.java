package eu.xenit.gradle.enterprise.conventions.extensions.dockermultiarch;

import eu.xenit.gradle.enterprise.conventions.BasePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;

public class DockerMultiArchPlugin extends BasePlugin {

    @Override
    public void apply(Project project) {
        springBootApplier().execute(project);
    }

    // The Spring Boot-facing logic is a Groovy class (see its class comment for the classloader reasons).
    // Groovy sources compile after Java, so the class is instantiated by name instead of referenced directly.
    @SuppressWarnings("unchecked")
    private static Action<Project> springBootApplier() {
        try {
            return (Action<Project>) Class.forName(
                            DockerMultiArchPlugin.class.getPackageName() + ".SpringBootMultiArchApplier")
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not load SpringBootMultiArchApplier", e);
        }
    }
}
