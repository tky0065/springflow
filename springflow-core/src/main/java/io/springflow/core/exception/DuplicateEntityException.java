package io.springflow.core.exception;

/**
 * Exception thrown when attempting to create a duplicate entity.
 */
public class DuplicateEntityException extends RuntimeException {

    private final Class<?> entityClass;
    private final String field;
    private final Object value;

    public DuplicateEntityException(Class<?> entityClass, String field, Object value) {
        super(String.format("Entity %s with %s=%s already exists",
                entityClass.getSimpleName(), field, value));
        this.entityClass = entityClass;
        this.field = field;
        this.value = value;
    }

    public DuplicateEntityException(String message) {
        super(message);
        this.entityClass = null;
        this.field = null;
        this.value = null;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }
}
