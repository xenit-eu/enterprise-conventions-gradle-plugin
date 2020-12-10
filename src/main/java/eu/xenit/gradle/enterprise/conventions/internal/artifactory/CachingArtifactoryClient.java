package eu.xenit.gradle.enterprise.conventions.internal.artifactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.cache.CacheBuilder.LockTarget;
import org.gradle.cache.CacheRepository;
import org.gradle.cache.FileLockManager.LockMode;
import org.gradle.cache.PersistentCache;
import org.gradle.cache.PersistentIndexedCache;
import org.gradle.cache.PersistentIndexedCacheParameters;
import org.gradle.cache.internal.filelock.LockOptionsBuilder;

public class CachingArtifactoryClient implements ArtifactoryClient {

    private static final Logger LOGGER = Logging.getLogger(CachingArtifactoryClient.class);
    private static final int VALIDITY_SECONDS = 60 * 60 * 24 * 30; // 30 days
    private static final PersistentIndexedCacheParameters<String, RepositoriesAndDate> CACHE_PARAMETERS = PersistentIndexedCacheParameters
            .of("repositoriesMap", String.class, RepositoriesAndDate.class);

    private final ArtifactoryClient client;

    private final CacheRepository cacheRepository;
    private PersistentCache persistentCache;
    private PersistentIndexedCache<String, RepositoriesAndDate> repositoryMapCache;
    private final String cacheKey;
    private final boolean offline;

    public CachingArtifactoryClient(ArtifactoryClient client, CacheRepository cacheRepository, String cacheKey,
            boolean offline) {
        this.client = client;
        this.cacheRepository = cacheRepository;
        this.cacheKey = cacheKey;
        this.offline = offline;
    }

    private PersistentCache getPersistentCache() {
        if (persistentCache == null) {
            persistentCache = cacheRepository.cache("eu.xenit.gradle.enterprise.conventions")
                    .withCrossVersionCache(LockTarget.DefaultTarget)
                    .withLockOptions(LockOptionsBuilder.mode(LockMode.Exclusive))
                    .withDisplayName("eu.xenit.enterprise-conventions repository replacement cache")
                    .withProperties(Collections.singletonMap("cacheVersion", "3"))
                    .open();
        }
        return persistentCache;
    }

    private PersistentIndexedCache<String, RepositoriesAndDate> getIndexedCache() {
        if (repositoryMapCache == null) {
            PersistentCache cache = getPersistentCache();
            repositoryMapCache = cache.createCache(CACHE_PARAMETERS);
        }
        return repositoryMapCache;
    }


    @Override
    public List<ArtifactoryRepositorySpec> getRepositories() {
        PersistentCache cache = getPersistentCache();
        @Nullable
        List<ArtifactoryRepositorySpec> cachedRepositories = cache.useCache(() -> {
            RepositoriesAndDate repositoriesAndDate = null;
            try {
                repositoriesAndDate = getIndexedCache().get(this.cacheKey);
            } catch (Exception e) {
                LOGGER.error("Failed to load cached repository information", e);
            }
            if (repositoriesAndDate == null) {
                return null;
            }

            Instant now = Instant.now();
            Instant expiry = Instant.ofEpochSecond(repositoriesAndDate.getExpirySeconds());

            LOGGER.debug("Cache expires at {}", expiry);

            if (expiry.isAfter(now)) {
                LOGGER.debug("Cache is still valid for {}", Duration.between(now, expiry));
                return Collections.unmodifiableList(repositoriesAndDate.getRepositories());
            } else if (this.offline) {
                LOGGER.warn("Gradle is running in offline mode. Using stale cached repository information");
                return Collections.unmodifiableList(repositoriesAndDate.getRepositories());
            }
            return null;
        });
        if (cachedRepositories != null) {
            return cachedRepositories;
        }

        if (this.offline) {
            throw new GradleException("No cached replacement repositories available for offline mode");
        }

        LOGGER.info("Fetching repository information with client {}", client);
        List<ArtifactoryRepositorySpec> repositories = client.getRepositories();

        cache.useCache(() -> {
            RepositoriesAndDate repositoriesAndDate = new RepositoriesAndDate();
            repositoriesAndDate.setExpirySeconds(Instant.now().getEpochSecond() + VALIDITY_SECONDS);
            repositoriesAndDate.setRepositories(repositories);
            LOGGER.debug("Storing repositories {} into cache", repositories);
            try {
                getIndexedCache().put(this.cacheKey, repositoriesAndDate);
            } catch (Exception e) {
                LOGGER.error("Failed to store cached repository information", e);
            }
        });

        return Collections.unmodifiableList(repositories);
    }
}
