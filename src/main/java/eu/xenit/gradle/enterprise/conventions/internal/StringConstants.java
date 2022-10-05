package eu.xenit.gradle.enterprise.conventions.internal;

import java.util.List;

public final class StringConstants {

    public static final List<String> SONATYPE_SNAPSHOTS_URLS = List.of(
            "https://oss.sonatype.org/content/repositories/snapshots/",
            "https://s01.oss.sonatype.org/content/repositories/snapshots/"
    );
    public static final String XENIT_BASE_URL;
    public static final String XENIT_REPO_BASE_URL = "https://repo.xenit.eu";
    public static final String XENIT_REPO_URL = XENIT_REPO_BASE_URL+"/basic/private/maven/";
    public static final String XENIT_REPO_PUBLISH_URL = "https://maven.cloudsmith.io/xenit/private/";

    public static final String GRADLE_PROPERTIES_PREFIX = "eu.xenit.enterprise-conventions";

    static {
        XENIT_BASE_URL = System.getProperty("eu.xenit.gradle.enterprise.conventions.artifactory-override",
                "https://artifactory.xenit.eu/artifactory/");
    }

    private StringConstants() {
    }
}
