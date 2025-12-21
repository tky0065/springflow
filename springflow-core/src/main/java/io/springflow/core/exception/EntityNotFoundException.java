package io.springflow.core.exception;

/**
 * Exception thrown when an entity is not found in the database.
 */
public class EntityNotFoundException extends RuntimeException {

    private final Class<?> entityClass;
    private final Object id;

    public EntityNotFoundException(Class<?> entityClass, Object id) {
        super(String.format("Entity %s with id %s not found", entityClass.getSimpleName(), id));
        this.entityClass = entityClass;
        this.id = id;
    }

    public EntityNotFoundException(String message) {
        super(message);
        this.entityClass = null;
        this.id = null;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public Object getId() {
        return id;
    }
}
