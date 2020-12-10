package eu.xenit.gradle.enterprise.conventions.internal.artifactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.json.JSONArray;
import org.json.JSONTokener;

public class ArtifactoryHttpClient implements ArtifactoryClient {

    private static final Logger LOGGER = Logging.getLogger(ArtifactoryHttpClient.class);

    private final URI apiBase;
    private final HttpClient httpClient;

    public ArtifactoryHttpClient(URI apiBase, HttpClient httpClient) {
        this.apiBase = apiBase;
        this.httpClient = httpClient;
    }

    public List<ArtifactoryRepositorySpec> getRepositories0() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(apiBase.resolve("api/repositories"))
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

            List<ArtifactoryRepositorySpec> repositories = new ArrayList<>(repositoriesJson.length());
            for (int i = 0; i < repositoriesJson.length(); i++) {
                repositories.add(ArtifactoryRepositorySpec
                        .createFromJson(apiBase.toString(), repositoriesJson.getJSONObject(i)));
            }

            LOGGER.debug("Received repositories from {}: {}", request, repositories);

            return repositories;
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

    @Override
    public String toString() {
        return "ArtifactoryHttpClient{" +
                "apiBase=" + apiBase +
                '}';
    }
}
