package io.springflow.core.mapper;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Context for DTO mapping to handle circular references and depth tracking.
 * <p>
 * Uses IdentityHashMap to track visited objects by reference identity.
 * </p>
 */
public class MappingContext {
    private final Map<Object, Boolean> visited = new IdentityHashMap<>();
    private final DtoMappingConfig config;
    private int currentDepth = 0;

    public MappingContext() {
        this(DtoMappingConfig.DEFAULT);
    }

    public MappingContext(DtoMappingConfig config) {
        this.config = config != null ? config : DtoMappingConfig.DEFAULT;
    }

    /**
     * Mark an entity as being currently mapped.
     * @param entity the entity to track
     */
    public void enterEntity(Object entity) {
        if (config.isDetectCycles()) {
            visited.put(entity, Boolean.TRUE);
        }
    }

    /**
     * Mark an entity as finished being mapped.
     * @param entity the entity to remove from tracking
     */
    public void exitEntity(Object entity) {
        visited.remove(entity);
    }

    /**
     * Check if an entity is already being mapped in the current branch.
     * @param entity the entity to check
     * @return true if a cycle is detected
     */
    public boolean isBeingMapped(Object entity) {
        return config.isDetectCycles() && visited.containsKey(entity);
    }

    // Alias for isBeingMapped to match new implementation needs
    public boolean isVisited(Object entity) {
        return isBeingMapped(entity);
    }

    // Alias for enterEntity to match new implementation needs
    public void markVisited(Object entity) {
        enterEntity(entity);
    }

    public int getCurrentDepth() {
        return currentDepth;
    }

    public void incrementDepth() {
        currentDepth++;
    }

    public void decrementDepth() {
        currentDepth--;
    }

    public DtoMappingConfig getConfig() {
        return config;
    }

    public MappingContext fork() {
        MappingContext forked = new MappingContext(this.config);
        forked.currentDepth = this.currentDepth;
        // Note: fork usually means separate visited set for some mapping strategies, 
        // but here we might want to preserve it depending on use case.
        // For now, new context with same config as per original test.
        return forked;
    }
}
