package eu.xenit.gradle.enterprise.repository;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ArtifactRepositoryContainer;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.artifacts.BaseRepositoryFactory;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

class AbstractRepositoryPlugin implements Plugin<Project> {

    private static final Map<URI, String> blocklist;
    private static final Set<URI> allowlist;

    private static final Logger LOGGER = Logging.getLogger(AbstractRepositoryPlugin.class);

    static {
        Map<URI, String> blocklistMap = new HashMap<>();
        Set<URI> allowlistSet = new HashSet<>();

        blocklistMap.put(URI.create("https://jcenter.bintray.com"),
                "JCenter does not verify groupid for artifacts: https://twitter.com/JakeWharton/status/1073102730443526144");
        blocklistMap.put(URI.create("https://jitpack.io"), "Jitpack builds artifacts from source");

        allowlistSet.add(URI.create(ArtifactRepositoryContainer.MAVEN_CENTRAL_URL));
        allowlistSet.add(URI.create(StringConstants.SONATYPE_SNAPSHOTS_URL));
        allowlistSet.add(URI.create(BaseRepositoryFactory.PLUGIN_PORTAL_DEFAULT_URL));

        withEndingSlash(blocklistMap);
        withEndingSlash(allowlistSet);
        blocklist = Collections.unmodifiableMap(blocklistMap);
        allowlist = Collections.unmodifiableSet(allowlistSet);
    }

    private static URI withEndingSlash(URI uri) {
        String path = uri.getPath();
        return URI.create(uri.toString() + "/");
    }

    static void withEndingSlash(Map<URI, String> map) {
        for (URI uri : new HashSet<>(map.keySet())) {
            map.put(withEndingSlash(uri), map.get(uri));
        }
    }

    private static void withEndingSlash(Set<URI> set) {
        for (URI uri : new HashSet<>(set)) {
            set.add(withEndingSlash(uri));
        }
    }

    @Override
    public void apply(Project project) {
        RepositoryHandlerExtensions.apply(project.getRepositories(), project);
        RepositoryHandlerExtensions.apply(project.getBuildscript().getRepositories(), project);

        project.getBuildscript().getRepositories().all(repository -> validateRepository(repository, project));
        project.getRepositories().all(repository -> validateRepository(repository, project));
    }

    private void validateRepository(ArtifactRepository repository, Project project) {
        if (repository instanceof MavenArtifactRepository) {
            validateRepository((MavenArtifactRepository) repository, project);
        }
    }

    protected boolean validateRepository(MavenArtifactRepository repository,
            Project project) {
        if (allowlist.contains(repository.getUrl())) {
            LOGGER.debug("Allowing explicitly allowlisted repository: {}", repository.getUrl());
            return true;
        }
        if ("file".equals(repository.getUrl().getScheme())) {
            LOGGER.debug("Allowing local repository: {}", repository.getUrl());
            return true;
        }
        if ("http".equals(repository.getUrl().getScheme())) {
            throw new BlockedRepositoryException(repository.getUrl(), "HTTPS is required for repositories.");
        }
        if (blocklist.containsKey(repository.getUrl())) {
            String reason = blocklist.get(repository.getUrl());
            throw new BlockedRepositoryException(repository.getUrl(),
                    "Explicitly blocklisted by eu.xenit.enterprise plugins: " + reason);
        }

        return false;
    }
}
