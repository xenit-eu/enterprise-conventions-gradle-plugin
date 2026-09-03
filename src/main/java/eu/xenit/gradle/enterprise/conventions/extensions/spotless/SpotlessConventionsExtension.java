package eu.xenit.gradle.enterprise.conventions.extensions.spotless;

import eu.xenit.gradle.enterprise.conventions.api.PluginApi;
import eu.xenit.gradle.enterprise.conventions.api.PublicApi;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

/**
 * Opt out of the Spotless conventions defaults.
 * <p>
 * Configuring a default step in the project's own {@code spotless} block already takes precedence over the
 * convention. This extension is for the case that cannot express: removing a default without replacing it.
 */
@PublicApi
public abstract class SpotlessConventionsExtension {

    @PluginApi
    public static final String NAME = "spotlessConventions";

    private final SpotlessJavaConventions java;

    @Inject
    protected SpotlessConventionsExtension(ObjectFactory objects) {
        java = objects.newInstance(SpotlessJavaConventions.class);
        getEnabled().convention(true);
    }

    /**
     * When false, no conventions defaults are configured at all, for any format.
     */
    @PluginApi
    public abstract Property<Boolean> getEnabled();

    @PluginApi
    public SpotlessJavaConventions getJava() {
        return java;
    }

    @PluginApi
    public void java(Action<? super SpotlessJavaConventions> action) {
        action.execute(java);
    }
}
