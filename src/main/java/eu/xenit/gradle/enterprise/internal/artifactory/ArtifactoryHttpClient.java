package eu.xenit.gradle.enterprise.internal.artifactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ArtifactoryHttpClient implements ArtifactoryClient {

    private final URI apiBase;
    private final HttpClient httpClient;

    public ArtifactoryHttpClient(URI apiBase, HttpClient httpClient) {
        this.apiBase = apiBase;
        this.httpClient = httpClient;
    }

    public List<ArtifactoryRepositorySpec> getRepositories0() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(apiBase.resolve("repositories"))
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
        try {
            if (response.statusCode() != 200) {
                throw new IOException(
                        "Failed to read response from " + request.method() + " " + request.uri()
                                + ": received status code "
                                + response
                                .statusCode());
            }

            JSONArray repositoriesJson = new JSONArray(new JSONTokener(response.body()));

            return repositoriesJson.toList().stream()
                    .map(item -> (JSONObject) item)
                    .map(ArtifactoryRepositorySpec::createFromJson)
                    .collect(Collectors.toList());
        } finally {
            response.body().close();
        }
    }

    @Override
    public List<ArtifactoryRepositorySpec> getRepositories() {
        try {
            return getRepositories0();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
