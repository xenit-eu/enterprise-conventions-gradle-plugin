package eu.xenit.gradle.enterprise.conventions.internal.artifactory;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.gradle.api.GradleException;
import org.gradle.cache.CacheBuilder;
import org.gradle.cache.CacheBuilder.LockTarget;
import org.gradle.cache.CacheRepository;
import org.gradle.cache.FileLockManager.LockMode;
import org.gradle.cache.PersistentCache;
import org.gradle.cache.PersistentIndexedCache;
import org.gradle.cache.PersistentIndexedCacheParameters;
import org.gradle.cache.internal.filelock.LockOptionsBuilder;

public class CachingArtifactoryClient implements ArtifactoryClient {

    private static final int VALIDITY_SECONDS = 60 * 60 * 24 * 30; // 30 days
    private static final PersistentIndexedCacheParameters<String, RepositoriesAndDate> CACHE_PARAMETERS = PersistentIndexedCacheParameters
            .of("repositoriesMap", String.class, RepositoriesAndDate.class);

    private final ArtifactoryClient client;

    private final CacheRepository cacheRepository;
    private final String cacheKey;
    private final boolean offline;

    public static class RepositoriesAndDate implements Serializable {

        private long expirySeconds;
        private List<ArtifactoryRepositorySpec> repositories;
    }

    public CachingArtifactoryClient(ArtifactoryClient client, CacheRepository cacheRepository, String cacheKey,
            boolean offline) {
        this.client = client;
        this.cacheRepository = cacheRepository;
        this.cacheKey = cacheKey;
        this.offline = offline;
    }

    private CacheBuilder createCacheBuilder() {
        return cacheRepository.cache("eu.xenit.gradle.enterprise")
                .withCrossVersionCache(LockTarget.DefaultTarget)
                .withLockOptions(LockOptionsBuilder.mode(LockMode.Exclusive))
                .withDisplayName("eu.xenit.enterprise repository replacement cache")
                .withProperties(Collections.singletonMap("cacheVersion", "1"));
    }


    @Override
    public List<ArtifactoryRepositorySpec> getRepositories() {
        CacheBuilder cacheBuilder = createCacheBuilder();
        try (PersistentCache cache = cacheBuilder.open()) {
            @Nullable
            List<ArtifactoryRepositorySpec> cachedRepositories = cache.useCache(() -> {
                PersistentIndexedCache<String, RepositoriesAndDate> repositoryMapCache = cache
                        .createCache(CACHE_PARAMETERS);
                RepositoriesAndDate repositoriesAndDate = repositoryMapCache.get(this.cacheKey);

                if (repositoriesAndDate != null && (repositoriesAndDate.expirySeconds < Instant.now().getEpochSecond()
                        || this.offline)) {
                    return Collections.unmodifiableList(repositoriesAndDate.repositories);
                }
                return null;
            });
            if (cachedRepositories != null) {
                return cachedRepositories;
            }
        }

        if (this.offline) {
            throw new GradleException("No cached replacement repositories available for offline mode.");
        }

        List<ArtifactoryRepositorySpec> repositories = client.getRepositories();

        try (PersistentCache cache = cacheBuilder.open()) {
            cache.useCache(() -> {
                PersistentIndexedCache<String, RepositoriesAndDate> repositoryMapCache = cache
                        .createCache(CACHE_PARAMETERS);
                RepositoriesAndDate repositoriesAndDate = new RepositoriesAndDate();
                repositoriesAndDate.expirySeconds = Instant.now().getEpochSecond() + VALIDITY_SECONDS;
                repositoriesAndDate.repositories = repositories;
                repositoryMapCache.put(this.cacheKey, repositoriesAndDate);
            });
        }

        return Collections.unmodifiableList(repositories);
    }
}
