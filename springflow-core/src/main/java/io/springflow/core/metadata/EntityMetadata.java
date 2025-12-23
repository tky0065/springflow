package io.springflow.core.metadata;

import io.springflow.annotations.Auditable;
import io.springflow.annotations.AutoApi;
import io.springflow.annotations.SoftDelete;
import java.util.List;
import java.util.Optional;

/**
 * Metadata for a JPA entity scannned by SpringFlow.
 */
public record EntityMetadata(
    Class<?> entityClass,
    Class<?> idType,
    String entityName,
    String tableName,
    AutoApi autoApiConfig,
    SoftDelete softDeleteConfig,
    Auditable auditableConfig,
    List<FieldMetadata> fields
) {
    public EntityMetadata(Class<?> entityClass, Class<?> idType, String entityName, String tableName, 
                          AutoApi autoApiConfig, SoftDelete softDeleteConfig, List<FieldMetadata> fields) {
        this(entityClass, idType, entityName, tableName, autoApiConfig, softDeleteConfig, null, fields);
    }

    public EntityMetadata(Class<?> entityClass, Class<?> idType, String entityName, String tableName, 
                          AutoApi autoApiConfig, List<FieldMetadata> fields) {
        this(entityClass, idType, entityName, tableName, autoApiConfig, null, null, fields);
    }

    public Optional<FieldMetadata> getFieldByName(String name) {
        return fields.stream()
                .filter(f -> f.name().equals(name))
                .findFirst();
    }

    public Optional<FieldMetadata> getIdField() {
        return fields.stream()
                .filter(FieldMetadata::isId)
                .findFirst();
    }

    public boolean isSoftDeleteEnabled() {
        return softDeleteConfig != null;
    }

    public boolean isAuditable() {
        return auditableConfig != null;
    }
}
