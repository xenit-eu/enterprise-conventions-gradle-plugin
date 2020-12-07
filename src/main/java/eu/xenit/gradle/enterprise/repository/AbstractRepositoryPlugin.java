package eu.xenit.gradle.enterprise.repository;

import eu.xenit.gradle.enterprise.internal.StringConstants;
import eu.xenit.gradle.enterprise.violations.ViolationHandler;
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

    public static final String REPOSITORY_BLOCK_PREFIX = "eu.xenit.enterprise.repository.block.";
    public static final String REPOSITORY_ALLOW_PREFIX = "eu.xenit.enterprise.repository.allow.";

    protected enum ValidationResult {
        ALLOWED(true),
        BLOCKED(true),
        NEUTRAL(false);

        private final boolean isFinal;

        private ValidationResult(boolean isFinal) {
            this.isFinal = isFinal;
        }

        public boolean isFinal() {
            return isFinal;
        }
    }

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
        ViolationHandler violationHandler = ViolationHandler.fromProject(project);

        project.getBuildscript().getRepositories()
                .all(repository -> validateRepository(repository, project, violationHandler));
        project.getRepositories().all(repository -> validateRepository(repository, project, violationHandler));
    }

    private void validateRepository(ArtifactRepository repository, Project project,
            ViolationHandler violationHandler) {
        if (repository instanceof MavenArtifactRepository) {
            validateRepository((MavenArtifactRepository) repository, project, violationHandler);
        }
    }

    protected ValidationResult validateRepository(MavenArtifactRepository repository,
            Project project, ViolationHandler violationHandler) {
        if ("file".equals(repository.getUrl().getScheme())) {
            LOGGER.debug("Allowing local repository: {}", repository.getUrl());
            return ValidationResult.ALLOWED;
        }

        if ("http".equals(repository.getUrl().getScheme())) {
            violationHandler.handleViolation(
                    new BlockedRepositoryException(repository.getUrl(), "HTTPS is required for repositories."));
            return ValidationResult.BLOCKED;
        }

        PropertyConfigurationList blockList = new PropertyConfigurationList(
                (Map<String, Object>) project.getProperties(),
                REPOSITORY_BLOCK_PREFIX);

        if (blockList.containsHost(repository.getUrl())) {
            LOGGER.debug("Blocking repository {} by property configuration", repository.getUrl());
            violationHandler.handleViolation(new BlockedRepositoryException(repository.getUrl(),
                    "Repository is blocked in property-based blocklist."));
            return ValidationResult.BLOCKED;
        }

        PropertyConfigurationList allowList = new PropertyConfigurationList(
                (Map<String, Object>) project.getProperties(),
                REPOSITORY_ALLOW_PREFIX);

        if (allowList.containsHost(repository.getUrl())) {
            LOGGER.debug("Allowing repository {} by property configuration", repository.getUrl());
            return ValidationResult.ALLOWED;
        }

        if (allowlist.contains(repository.getUrl())) {
            LOGGER.debug("Allowing explicitly allowlisted repository: {}", repository.getUrl());
            return ValidationResult.ALLOWED;
        }

        if (blocklist.containsKey(repository.getUrl())) {
            String reason = blocklist.get(repository.getUrl());
            violationHandler.handleViolation(new BlockedRepositoryException(repository.getUrl(),
                    "Explicitly blocklisted by eu.xenit.enterprise plugins: " + reason));
            return ValidationResult.BLOCKED;
        }

        return ValidationResult.NEUTRAL;
    }
}
