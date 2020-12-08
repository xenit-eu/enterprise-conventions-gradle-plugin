package eu.xenit.gradle.enterprise.conventions.internal.artifactory;

import java.io.Serializable;
import org.json.JSONObject;

public class ArtifactoryRepositorySpec implements Serializable {

    public enum RepositoryType {
        LOCAL,
        REMOTE,
        VIRTUAL
    }

    private String key;
    private RepositoryType type;
    private String url;

    public ArtifactoryRepositorySpec(String key, RepositoryType type, String url) {
        this.key = key;
        this.type = type;
        this.url = url;
    }

    static ArtifactoryRepositorySpec createFromJson(JSONObject object) {
        return new ArtifactoryRepositorySpec(
                object.getString("key"),
                RepositoryType.valueOf(object.getString("type")),
                object.getString("url")
        );
    }

    public String getKey() {
        return key;
    }

    public RepositoryType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }
}
