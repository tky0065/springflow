package io.springflow.core.metadata;

import io.springflow.annotations.AutoApi;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Metadata for a JPA entity.
 */
public record EntityMetadata(
    Class<?> entityClass,
    Class<?> idType,
    String entityName,
    String tableName,
    AutoApi autoApiConfig,
    List<FieldMetadata> fields
) {
    public EntityMetadata {
        if (fields == null) {
            fields = Collections.emptyList();
        }
    }

    public Optional<FieldMetadata> getIdField() {
        return fields.stream()
                .filter(FieldMetadata::isId)
                .findFirst();
    }
    
    public Optional<FieldMetadata> getFieldByName(String name) {
        return fields.stream()
                .filter(f -> f.name().equals(name))
                .findFirst();
    }
}
