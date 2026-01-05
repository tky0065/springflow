package io.springflow.core.utils;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;

import java.lang.reflect.Field;

/**
 * Utility class for working with JPA entities and metadata.
 */
public class EntityUtils {

    private EntityUtils() {
        // Utility class
    }

    /**
     * Extract the ID value from an entity using metadata.
     *
     * @param entity   the entity instance
     * @param metadata the entity metadata
     * @param <ID>     the ID type
     * @return the ID value
     */
    @SuppressWarnings("unchecked")
    public static <ID> ID getEntityId(Object entity, EntityMetadata metadata) {
        if (entity == null || metadata == null) {
            return null;
        }

        // Find the ID field from metadata
        FieldMetadata idField = metadata.fields().stream()
                .filter(FieldMetadata::isId)
                .findFirst()
                .orElse(null);

        if (idField == null) {
            throw new IllegalStateException("No ID field found for entity: " + entity.getClass().getName());
        }

        try {
            Field field = idField.field();
            field.setAccessible(true);
            return (ID) field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to extract ID from entity: " + entity.getClass().getName(), e);
        }
    }
}
