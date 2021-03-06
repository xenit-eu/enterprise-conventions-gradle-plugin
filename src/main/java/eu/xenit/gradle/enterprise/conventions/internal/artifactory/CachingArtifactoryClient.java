package eu.xenit.gradle.enterprise.conventions.internal.artifactory;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.cache.CacheBuilder;
import org.gradle.cache.CacheBuilder.LockTarget;
import org.gradle.cache.CacheRepository;
import org.gradle.cache.FileLockManager.LockMode;
import org.gradle.cache.PersistentCache;
import org.gradle.cache.PersistentIndexedCache;
import org.gradle.cache.PersistentIndexedCacheParameters;
import org.gradle.cache.internal.filelock.LockOptionsBuilder;
import org.gradle.util.GradleVersion;

public class CachingArtifactoryClient implements ArtifactoryClient {

    private static final Logger LOGGER = Logging.getLogger(CachingArtifactoryClient.class);
    private static final int VALIDITY_SECONDS = 60 * 60 * 24 * 30; // 30 days
    private static final PersistentIndexedCacheParameters<String, RepositoriesAndDate> CACHE_PARAMETERS = PersistentIndexedCacheParameters
            .of("repositoriesMap", String.class, RepositoriesAndDate.class);

    private final ArtifactoryClient client;

    private final CacheRepository cacheRepository;
    private final String cacheKey;
    private final boolean offline;

    public CachingArtifactoryClient(ArtifactoryClient client, CacheRepository cacheRepository, String cacheKey,
            boolean offline) {
        this.client = client;
        this.cacheRepository = cacheRepository;
        this.cacheKey = cacheKey;
        this.offline = offline;
    }

    private CacheBuilder createCacheBuilder() {
        return cacheRepository.cache("eu.xenit.gradle.enterprise.conventions")
                .withCrossVersionCache(LockTarget.DefaultTarget)
                .withLockOptions(LockOptionsBuilder.mode(LockMode.None).useCrossVersionImplementation())
                .withDisplayName("eu.xenit.enterprise-conventions repository replacement cache")
                .withProperties(Collections.singletonMap("cacheVersion", "3"));
    }

    @SneakyThrows
    private <K, V> V cacheGet(PersistentIndexedCache<K, V> cache) {
        if (GradleVersion.current().compareTo(GradleVersion.version("6.8")) < 0) {
            Method getMethod = cache.getClass().getMethod("get", Object.class);
            return (V) getMethod.invoke(cache, this.cacheKey);
        }
        Method getMethod = cache.getClass().getMethod("get", Object.class, Function.class);
        return (V) getMethod.invoke(cache, cacheKey, (Function) (__ -> null));
    }

    @Override
    public List<ArtifactoryRepositorySpec> getRepositories() {
        CacheBuilder cacheBuilder = createCacheBuilder();
        try (PersistentCache cache = cacheBuilder.open()) {
            @Nullable
            List<ArtifactoryRepositorySpec> cachedRepositories = cache.useCache(() -> {
                RepositoriesAndDate repositoriesAndDate = null;
                try {
                    PersistentIndexedCache<String, RepositoriesAndDate> repositoryMapCache = cache
                            .createCache(CACHE_PARAMETERS);
                    repositoriesAndDate = cacheGet(repositoryMapCache);
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
        }

        if (this.offline) {
            throw new GradleException("No cached replacement repositories available for offline mode");
        }

        LOGGER.info("Fetching repository information with client {}", client);
        List<ArtifactoryRepositorySpec> repositories = client.getRepositories();

        try (PersistentCache cache = cacheBuilder.open()) {
            cache.useCache(() -> {
                RepositoriesAndDate repositoriesAndDate = new RepositoriesAndDate();
                repositoriesAndDate.setExpirySeconds(Instant.now().getEpochSecond() + VALIDITY_SECONDS);
                repositoriesAndDate.setRepositories(repositories);
                LOGGER.debug("Storing repositories {} into cache", repositories);
                try {
                    PersistentIndexedCache<String, RepositoriesAndDate> repositoryMapCache = cache
                            .createCache(CACHE_PARAMETERS);
                    repositoryMapCache.put(this.cacheKey, repositoriesAndDate);
                } catch (Exception e) {
                    LOGGER.error("Failed to store cached repository information", e);
                }
            });
        }

        return Collections.unmodifiableList(repositories);
    }
}
