package eu.xenit.gradle.enterprise.conventions.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to denote a class or method as part
 * of the public API that can be used by other Gradle plugins.
 * It means that this API will not change within a release in a way that would make it no longer compatible with an earlier version.
 * <p>
 * When a class is designated as public api, it means that the class name itself will not change,
 * it does not indicate anything about methods or fields inside the class. In fact, all non-annotated fields and methods are not public API.
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface PluginApi {

    /**
     * Indicates if the interface annotated with {@link PluginApi} may be subclassed by a different plugin.
     * <p>
     * Only interfaces or classes that are explicitly marked as suitable for subclassing may be implemented or extended by a different plugin.
     */
    boolean forSubclassing() default false;
}
