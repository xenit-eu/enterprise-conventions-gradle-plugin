package eu.xenit.gradle.enterprise.conventions.extensions.spotless;

import eu.xenit.gradle.enterprise.conventions.api.PluginApi;
import eu.xenit.gradle.enterprise.conventions.api.PublicApi;
import javax.inject.Inject;
import org.gradle.api.provider.Property;

/**
 * Per-step opt out for the defaults applied to the Spotless {@code java} format. Each property is named
 * after the Spotless method that adds the step.
 */
@PublicApi
public abstract class SpotlessJavaConventions {

    @Inject
    protected SpotlessJavaConventions() {
        getRemoveUnusedImports().convention(true);
        getExpandWildcardImports().convention(true);
    }

    @PluginApi
    public abstract Property<Boolean> getRemoveUnusedImports();

    @PluginApi
    public abstract Property<Boolean> getExpandWildcardImports();
}
