package io.springflow.graphql.dataloader;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generic batch loader for entities to solve N+1 problem.
 * <p>
 * This loader batches multiple entity fetch requests into a single database query,
 * dramatically improving performance when loading related entities.
 * </p>
 *
 * <p>Example: Loading products for multiple categories</p>
 * <pre>
 * Instead of N+1 queries:
 *   SELECT * FROM Category WHERE id = 1
 *   SELECT * FROM Product WHERE category_id = 1  -- Query 1
 *   SELECT * FROM Product WHERE category_id = 2  -- Query 2
 *   ... (N more queries)
 *
 * DataLoader batches into 2 queries:
 *   SELECT * FROM Category WHERE id IN (1, 2, 3, ...)
 *   SELECT * FROM Product WHERE category_id IN (1, 2, 3, ...)
 * </pre>
 *
 * @param <T>  the entity type
 * @param <ID> the ID type
 * @author SpringFlow
 * @since 0.3.0
 */
@Slf4j
public class EntityBatchLoader<T, ID> {

    private final JpaRepository<T, ID> repository;
    private final EntityMetadata metadata;
    private final String entityName;

    public EntityBatchLoader(JpaRepository<T, ID> repository, EntityMetadata metadata, String entityName) {
        this.repository = repository;
        this.metadata = metadata;
        this.entityName = entityName;
    }

    /**
     * Batch load entities by IDs.
     * <p>
     * This method is called by GraphQL DataLoader with a batch of IDs to fetch.
     * Instead of N individual queries, it executes a single query with IN clause.
     * </p>
     *
     * @param ids list of entity IDs to load
     * @return Mono with list of entities in same order as IDs
     */
    public Mono<List<T>> load(List<ID> ids) {
        log.debug("Batch loading {} entities with {} IDs", entityName, ids.size());

        return Mono.fromCallable(() -> {
            // Fetch all entities in a single query using JpaRepository.findAllById()
            List<T> entities = repository.findAllById(ids);

            log.debug("Batch loaded {} {} entities", entities.size(), entityName);

            // Map entities by ID for quick lookup
            Map<ID, T> entityMap = entities.stream()
                    .collect(Collectors.toMap(
                            entity -> extractId(entity),
                            Function.identity()
                    ));

            // Return entities in the same order as requested IDs
            // (DataLoader requires maintaining order)
            return ids.stream()
                    .map(entityMap::get)
                    .collect(Collectors.toList());
        });
    }

    /**
     * Extracts the ID value from an entity using reflection.
     *
     * @param entity the entity
     * @return the ID value
     */
    @SuppressWarnings("unchecked")
    private ID extractId(T entity) {
        try {
            FieldMetadata idField = metadata.getIdField()
                    .orElseThrow(() -> new IllegalStateException("No ID field found for " + entityName));

            Field field = idField.field();
            field.setAccessible(true);
            return (ID) field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to extract ID from " + entityName, e);
        }
    }

    /**
     * Registers this batch loader with the GraphQL DataLoader registry.
     *
     * @param registry the BatchLoaderRegistry
     * @param loaderName the name for this loader (typically entity name)
     */
    public void register(BatchLoaderRegistry registry, String loaderName) {
        log.info("Registering DataLoader: {}", loaderName);
        registry.forTypePair(Object.class, Object.class)
                .withName(loaderName)
                .registerBatchLoader((ids, env) -> {
                    @SuppressWarnings("unchecked")
                    List<ID> typedIds = (List<ID>) ids;
                    return this.load(typedIds).flatMapMany(Flux::fromIterable);
                });
    }
}
