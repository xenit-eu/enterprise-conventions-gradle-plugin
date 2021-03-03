package eu.xenit.gradle.enterprise.conventions.internal;

public final class StringConstants {

    public static final String SONATYPE_SNAPSHOTS_URL = "https://oss.sonatype.org/content/repositories/snapshots/";
    public static final String XENIT_BASE_URL;

    public static final String GRADLE_PROPERTIES_PREFIX = "eu.xenit.enterprise-conventions";

    static {
        XENIT_BASE_URL = System.getProperty("eu.xenit.gradle.enterprise.conventions.artifactory-override",
                "https://artifactory.xenit.eu/artifactory/");
    }

    private StringConstants() {
    }
}
