package eu.xenit.gradle.enterprise.conventions.extensions.spotless;

import eu.xenit.gradle.enterprise.conventions.BasePlugin;
import eu.xenit.gradle.enterprise.conventions.api.PluginApi;
import eu.xenit.gradle.enterprise.conventions.api.PublicApi;
import org.gradle.api.Action;
import org.gradle.api.Project;

@PublicApi
public class SpotlessConventionsPlugin extends BasePlugin {

    @PluginApi
    public static final String PLUGIN_ID = "eu.xenit.enterprise-conventions.ext.spotless";

    @Override
    public void apply(Project project) {
        conventionsApplier().execute(project);
    }

    // The Spotless-facing logic is a Groovy class (see its class comment for the classloader reasons).
    // Groovy sources compile after Java, so the class is instantiated by name instead of referenced directly.
    @SuppressWarnings("unchecked")
    private static Action<Project> conventionsApplier() {
        try {
            return (Action<Project>) Class.forName(
                            SpotlessConventionsPlugin.class.getPackageName() + ".SpotlessConventionsApplier")
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not load SpotlessConventionsApplier", e);
        }
    }
}
