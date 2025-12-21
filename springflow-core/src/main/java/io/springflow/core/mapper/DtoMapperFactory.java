package io.springflow.core.mapper;

import io.springflow.core.metadata.EntityMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating and caching {@link DtoMapper} instances.
 * <p>
 * This factory creates one DtoMapper per entity type and caches them for reuse.
 * </p>
 */
public class DtoMapperFactory {

    private static final Logger log = LoggerFactory.getLogger(DtoMapperFactory.class);

    private final Map<Class<?>, DtoMapper<?, ?>> mapperCache = new ConcurrentHashMap<>();

    /**
     * Gets or creates a DtoMapper for the given entity class and metadata.
     *
     * @param entityClass the entity class
     * @param metadata    the entity metadata
     * @param <T>         the entity type
     * @param <ID>        the entity ID type
     * @return the DtoMapper instance
     */
    @SuppressWarnings("unchecked")
    public <T, ID> DtoMapper<T, ID> getMapper(Class<T> entityClass, EntityMetadata metadata) {
        return (DtoMapper<T, ID>) mapperCache.computeIfAbsent(entityClass, clazz -> {
            log.debug("Creating DtoMapper for entity: {}", entityClass.getSimpleName());
            return new EntityDtoMapper<>(entityClass, metadata);
        });
    }

    /**
     * Clears the mapper cache.
     * Useful for testing or reloading configurations.
     */
    public void clearCache() {
        log.debug("Clearing DtoMapper cache");
        mapperCache.clear();
    }

    /**
     * Gets the number of cached mappers.
     *
     * @return the cache size
     */
    public int getCacheSize() {
        return mapperCache.size();
    }
}
