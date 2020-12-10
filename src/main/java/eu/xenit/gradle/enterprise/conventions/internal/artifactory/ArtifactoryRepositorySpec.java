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
    private String baseUrl;

    public ArtifactoryRepositorySpec(String baseUrl, String key, RepositoryType type, String url) {
        this.baseUrl = baseUrl;
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        this.key = key;
        this.type = type;
        this.url = url;
    }

    static ArtifactoryRepositorySpec createFromJson(String baseUrl, JSONObject object) {
        return new ArtifactoryRepositorySpec(
                baseUrl,
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

    public String getProxyUrl() {
        return baseUrl + key;
    }
   
    @Override
    public String toString() {
        return "ArtifactoryRepositorySpec{" +
                "key='" + key + '\'' +
                ", type=" + type +
                ", url='" + url + '\'' +
                '}';
    }

}
