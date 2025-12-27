package io.springflow.core.mapper;

import java.util.HashSet;
import java.util.Set;

/**
 * Context for tracking mapping state during entity-to-DTO conversion.
 * <p>
 * Maintains a stack of entities being mapped to detect and prevent circular references
 * in bidirectional relationships.
 * </p>
 * <p>
 * Example scenario:
 * </p>
 * <pre>{@code
 * Product {
 *     Category category;
 * }
 * Category {
 *     List<Product> products;  // Bidirectional - would cause infinite loop
 * }
 * }</pre>
 *
 * @since 0.4.0
 */
public class MappingContext {

    private final Set<Object> visitedEntities;
    private final DtoMappingConfig config;

    public MappingContext(DtoMappingConfig config) {
        this.visitedEntities = new HashSet<>();
        this.config = config;
    }

    /**
     * Check if an entity is currently being mapped (would cause a cycle).
     *
     * @param entity the entity to check
     * @return true if entity is already in the mapping stack
     */
    public boolean isBeingMapped(Object entity) {
        if (!config.isDetectCycles() || entity == null) {
            return false;
        }
        // Use System.identityHashCode to detect exact same object instance
        return visitedEntities.stream()
                .anyMatch(visited -> System.identityHashCode(visited) == System.identityHashCode(entity));
    }

    /**
     * Mark an entity as being mapped.
     *
     * @param entity the entity
     */
    public void enterEntity(Object entity) {
        if (config.isDetectCycles() && entity != null) {
            visitedEntities.add(entity);
        }
    }

    /**
     * Unmark an entity after mapping is complete.
     *
     * @param entity the entity
     */
    public void exitEntity(Object entity) {
        if (config.isDetectCycles() && entity != null) {
            visitedEntities.remove(entity);
        }
    }

    public DtoMappingConfig getConfig() {
        return config;
    }

    /**
     * Create a new context with the same configuration.
     * Used for nested mappings to avoid sharing visited set.
     */
    public MappingContext fork() {
        return new MappingContext(config);
    }
}
