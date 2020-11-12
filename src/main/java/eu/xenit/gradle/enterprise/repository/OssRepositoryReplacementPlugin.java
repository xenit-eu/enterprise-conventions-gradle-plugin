package eu.xenit.gradle.enterprise.repository;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

/**
 * Maintains a list of explicitly allowed and blocked maven repositories.
 * <p>
 * Explicitly blocked repositories will result in an exception when they are configured.
 * Explicitly allowed repositories are used as a base for allowed repositories in {@link InternalRepositoryReplacementPlugin}
 */
public class OssRepositoryReplacementPlugin implements Plugin<Project> {

    private static final Map<URI, String> blocklist;
    private static final Set<URI> allowlist;

    private static final Logger LOGGER = Logging.getLogger(OssRepositoryReplacementPlugin.class);

    static {
        Map<URI, String> blocklistMap = new HashMap<>();
        Set<URI> allowlistSet = new HashSet<>();

        blocklistMap.put(URI.create("https://jcenter.bintray.com"),
                "JCenter does not verify groupid for artifacts: https://twitter.com/JakeWharton/status/1073102730443526144");
        blocklistMap.put(URI.create("https://jitpack.io"), "Jitpack builds artifacts from source");

        allowlistSet.add(URI.create("https://repo.maven.apache.org/maven2")); // Maven central
        allowlistSet.add(URI.create("https://plugins.gradle.org/m2")); // Gradle plugin portal

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
        if (blocklist.containsKey(repository.getUrl())) {
            String reason = blocklist.get(repository.getUrl());
            throw new BlockedRepositoryException(repository.getUrl(),
                    "Explicitly blocklisted by eu.xenit.enterprise plugins: " + reason);
        }

        return false;
    }
}
