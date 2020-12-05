package eu.xenit.gradle.enterprise.internal;

public final class StringConstants {

    public static final String SONATYPE_SNAPSHOTS_URL = "https://oss.sonatype.org/content/repositories/snapshots/";
    public static final String XENIT_BASE_URL;

    static {
        XENIT_BASE_URL = System.getProperty("eu.xenit.gradle.enterprise.artifactory-override",
                "https://artifactory.xenit.eu/artifactory/");
    }

    private StringConstants() {
    }
}
