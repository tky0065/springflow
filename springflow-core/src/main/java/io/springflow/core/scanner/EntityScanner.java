package io.springflow.core.scanner;

import io.springflow.annotations.AutoApi;
import jakarta.persistence.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Scans the classpath for JPA entities annotated with {@link AutoApi}.
 *
 * <p>This scanner discovers all entity classes that should have auto-generated
 * REST APIs. Results are cached for performance.
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * EntityScanner scanner = new EntityScanner();
 * List<Class<?>> entities = scanner.scanEntities("com.example.domain");
 * }</pre>
 *
 * @author SpringFlow
 * @since 0.1.0
 * @see AutoApi
 */
public class EntityScanner {

    private static final Logger log = LoggerFactory.getLogger(EntityScanner.class);

    /**
     * Cache of scanned entities by package.
     * Key: comma-separated list of base packages
     * Value: set of discovered entity classes
     */
    private final Map<String, Set<Class<?>>> cache = new ConcurrentHashMap<>();

    /**
     * Maximum number of cache entries to prevent memory issues.
     */
    private final int maxCacheSize;

    /**
     * Cache statistics for monitoring.
     */
    private final CacheStatistics statistics = new CacheStatistics();

    /**
     * Creates a scanner with default cache size (100).
     */
    public EntityScanner() {
        this(100);
    }

    /**
     * Creates a scanner with custom cache size.
     *
     * @param maxCacheSize maximum number of cache entries
     */
    public EntityScanner(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    /**
     * Scans specified packages for entities with {@link AutoApi} annotation.
     *
     * <p>Results are cached for subsequent calls with the same packages.
     *
     * @param basePackages packages to scan (supports sub-packages)
     * @return list of discovered entity classes
     * @throws ScanException if scanning fails
     */
    public List<Class<?>> scanEntities(String... basePackages) {
        if (basePackages == null || basePackages.length == 0) {
            log.warn("No base packages specified for entity scanning");
            return Collections.emptyList();
        }

        String cacheKey = String.join(",", basePackages);

        // Check cache first
        Set<Class<?>> cachedEntities = cache.get(cacheKey);
        if (cachedEntities != null) {
            statistics.recordCacheHit();
            log.debug("Returning {} cached entities for packages: {}",
                     cachedEntities.size(), cacheKey);
            return new ArrayList<>(cachedEntities);
        }

        statistics.recordCacheMiss();

        // Perform scan
        Set<Class<?>> discoveredEntities = new HashSet<>();

        try {
            for (String basePackage : basePackages) {
                log.debug("Scanning package: {}", basePackage);
                Set<Class<?>> packageEntities = scanPackage(basePackage);
                discoveredEntities.addAll(packageEntities);
                log.debug("Found {} entities in package {}",
                         packageEntities.size(), basePackage);
            }

            // Cache results
            cacheResults(cacheKey, discoveredEntities);

            log.info("Entity scan completed. Found {} entities with @AutoApi in packages: {}",
                    discoveredEntities.size(), cacheKey);

            return new ArrayList<>(discoveredEntities);

        } catch (Exception e) {
            log.error("Failed to scan entities in packages: {}", cacheKey, e);
            throw new ScanException("Entity scanning failed", e);
        }
    }

    /**
     * Scans a single package for entities.
     *
     * @param basePackage package to scan
     * @return set of discovered entities
     */
    private Set<Class<?>> scanPackage(String basePackage) {
        ClassPathScanningCandidateComponentProvider scanner = createScanner();
        Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);

        return candidates.stream()
                .map(BeanDefinition::getBeanClassName)
                .filter(Objects::nonNull)
                .map(this::loadClass)
                .filter(Objects::nonNull)
                .filter(this::isValidEntity)
                .collect(Collectors.toSet());
    }

    /**
     * Creates a configured classpath scanner.
     *
     * @return scanner with filters for @Entity and @AutoApi
     */
    private ClassPathScanningCandidateComponentProvider createScanner() {
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);

        // Filter for classes with both @Entity and @AutoApi
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(AutoApi.class));

        return scanner;
    }

    /**
     * Loads a class by name.
     *
     * @param className fully qualified class name
     * @return loaded class or null if loading fails
     */
    private Class<?> loadClass(String className) {
        try {
            return ClassUtils.forName(className, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            log.warn("Could not load class: {}", className, e);
            return null;
        }
    }

    /**
     * Validates that a class is a proper entity with @AutoApi.
     *
     * @param clazz class to validate
     * @return true if valid entity
     */
    private boolean isValidEntity(Class<?> clazz) {
        boolean hasEntity = clazz.isAnnotationPresent(Entity.class);
        boolean hasAutoApi = clazz.isAnnotationPresent(AutoApi.class);

        if (hasEntity && hasAutoApi) {
            log.debug("Valid entity found: {}", clazz.getName());
            return true;
        }

        if (hasEntity && !hasAutoApi) {
            log.trace("Entity without @AutoApi, skipping: {}", clazz.getName());
        }

        return false;
    }

    /**
     * Checks if a class has the {@link AutoApi} annotation.
     *
     * @param entityClass class to check
     * @return true if annotated with @AutoApi
     */
    public boolean hasAutoApiAnnotation(Class<?> entityClass) {
        return entityClass != null && entityClass.isAnnotationPresent(AutoApi.class);
    }

    /**
     * Caches scan results if cache size limit not exceeded.
     *
     * @param cacheKey cache key
     * @param entities entities to cache
     */
    private void cacheResults(String cacheKey, Set<Class<?>> entities) {
        if (cache.size() >= maxCacheSize) {
            log.warn("Cache size limit reached ({}). Clearing oldest entry.", maxCacheSize);
            // Simple eviction: remove first entry
            cache.keySet().stream().findFirst().ifPresent(cache::remove);
        }

        cache.put(cacheKey, new HashSet<>(entities));
        log.debug("Cached {} entities for key: {}", entities.size(), cacheKey);
    }

    /**
     * Clears the entity cache.
     */
    public void clearCache() {
        int size = cache.size();
        cache.clear();
        statistics.reset();
        log.info("Entity cache cleared. Removed {} entries.", size);
    }

    /**
     * Returns cache statistics for monitoring.
     *
     * @return cache statistics
     */
    public CacheStatistics getStatistics() {
        return statistics;
    }

    /**
     * Returns the current cache size.
     *
     * @return number of cached entries
     */
    public int getCacheSize() {
        return cache.size();
    }

    /**
     * Cache statistics for monitoring cache performance.
     */
    public static class CacheStatistics {
        private long hits = 0;
        private long misses = 0;

        void recordCacheHit() {
            hits++;
        }

        void recordCacheMiss() {
            misses++;
        }

        void reset() {
            hits = 0;
            misses = 0;
        }

        public long getHits() {
            return hits;
        }

        public long getMisses() {
            return misses;
        }

        public long getTotal() {
            return hits + misses;
        }

        public double getHitRate() {
            long total = getTotal();
            return total == 0 ? 0.0 : (double) hits / total;
        }

        @Override
        public String toString() {
            return String.format("CacheStatistics{hits=%d, misses=%d, hitRate=%.2f%%}",
                               hits, misses, getHitRate() * 100);
        }
    }
}
